"""接口全链路自检脚本（不开浏览器，直接打后端）。

覆盖本次改动：验证码接口 -> 带验证码登录 -> 拿 JWT token -> 拦截器 401 -> 修改密码 -> 删除用户。

依赖：pip install ddddocr
用法：先启动后端，再执行 python scripts/api_check.py
"""

import base64
import http.cookiejar
import json
import time
import urllib.error
import urllib.request

BASE = "http://localhost:3000"
ORIGIN = "http://localhost:5173"

jar = http.cookiejar.CookieJar()
opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar))

results = []


def call(method, path, body=None, token=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(BASE + path, data=data, method=method)
    req.add_header("Origin", ORIGIN)
    if data:
        req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("auth", token)
    try:
        with opener.open(req) as res:
            return res.status, res.read().decode(), dict(res.headers)
    except urllib.error.HTTPError as err:
        return err.code, err.read().decode(), dict(err.headers)


def preflight(path, method="POST"):
    """模拟浏览器的跨域预检请求。"""
    req = urllib.request.Request(BASE + path, method="OPTIONS")
    req.add_header("Origin", ORIGIN)
    req.add_header("Access-Control-Request-Method", method)
    req.add_header("Access-Control-Request-Headers", "auth,content-type")
    try:
        with opener.open(req) as res:
            return res.status, dict(res.headers)
    except urllib.error.HTTPError as err:
        return err.code, dict(err.headers)


def check(name, actual, expected):
    ok = actual == expected
    results.append((ok, name, actual, expected))
    print(f"{'[OK]  ' if ok else '[FAIL]'} {name}: 实际={actual!r} 期望={expected!r}")


def ocr_candidates(models, img_bytes):
    """两个识别模型一起上，同一张图多给几个候选，命中率高一些（实测单模型约 1/3）。"""
    guesses = []
    for model in models:
        guess = model.classification(img_bytes).strip()
        if guess and guess not in guesses:
            guesses.append(guess)
    return guesses


def fetch_captcha(models):
    """拉一张验证码，返回 (图片 dataURL, 候选字符列表)。"""
    status, text, _ = call("GET", "/user/getCode")
    if status != 200:
        raise SystemExit(f"获取验证码失败，HTTP {status}: {text[:200]}")
    image = text
    return image, ocr_candidates(models, base64.b64decode(text.split(",", 1)[1]))


def login_with_captcha(models, username, password, attempts=8):
    """验证码识别会偶尔出错，失败就换一张重试。"""
    for i in range(1, attempts + 1):
        _, guesses = fetch_captcha(models)
        for guess in guesses:
            status, text, _ = call(
                "POST", "/user/login", {"username": username, "password": password, "code": guess}
            )
            payload = json.loads(text)
            print(f"       第 {i} 次尝试：识别为 {guess!r} -> {payload.get('msg')}")
            if payload.get("success"):
                return payload
    return None


def main():
    from ddddocr import DdddOcr

    models = [DdddOcr(show_ad=False), DdddOcr(show_ad=False, beta=True)]
    username = f"vue_check_{int(time.time())}"
    password = "123456"
    new_password = "654321"

    print("=== 1. 注册（白名单接口，不需要 token）===")
    status, text, _ = call("POST", "/user/register", {"username": username, "password": password})
    check("注册返回 200", status, 200)
    check("注册成功", json.loads(text)["success"], True)

    status, text, _ = call("POST", "/user/register", {"username": username, "password": password})
    check("重复注册被拒", json.loads(text)["msg"], "用户名已经存在")

    print("\n=== 2. 参数校验（@NotBlank + @Valid）===")
    status, text, _ = call("POST", "/user/login", {"username": username, "password": password})
    check("登录不带 code -> 400", status, 400)
    check("提示验证码不能为空", json.loads(text)["msg"], "验证码不能为空")

    status, text, _ = call("POST", "/user/register", {"username": "onlyname"})
    check("注册不带 password -> 400", status, 400)
    check("提示密码不能为空", json.loads(text)["msg"], "密码不能为空")

    print("\n=== 3. 验证码接口 + CORS ===")
    image, guesses = fetch_captcha(models)
    check("验证码返回 data URL", image.startswith("data:image/jpeg;base64,"), True)
    print(f"       OCR 识别结果：{guesses}")
    check("下发了 JSESSIONID（验证码存在 session 里）", any(c.name == "JSESSIONID" for c in jar), True)

    status, _, headers = call("GET", "/user/getCode")
    check("CORS 放行 5173 来源", headers.get("Access-Control-Allow-Origin"), ORIGIN)
    check("CORS 允许带 cookie", headers.get("Access-Control-Allow-Credentials"), "true")

    status, headers = preflight("/user/login")
    check("白名单接口的预检请求放行", status, 200)
    status, headers = preflight("/user/modifyPassword")
    check("受保护接口的预检请求也要放行（否则浏览器直接 Failed to fetch）", status, 200)
    check("预检响应允许 auth 请求头", headers.get("Access-Control-Allow-Headers"), "auth, content-type")

    print("\n=== 4. 登录 ===")
    status, text, _ = call(
        "POST", "/user/login", {"username": username, "password": password, "code": "!!!!!"}
    )
    check("验证码错误被拒", json.loads(text)["msg"], "验证码错误")

    payload = login_with_captcha(models, username, password)
    check("登录成功", bool(payload), True)
    if not payload:
        raise SystemExit("登录没成功，后面的用例无法继续")

    user_id = payload["data"]["id"]
    token = payload["data"]["token"]
    check("登录返回 token", bool(token), True)
    check("登录返回用户 id", bool(user_id), True)
    print(f"       id={user_id} token={token[:24]}…")

    print("\n=== 5. 拦截器 ===")
    status, _, _ = call("POST", "/user/modifyPassword", {"id": user_id})
    check("不带 token 访问受保护接口 -> 401", status, 401)
    status, _, _ = call("POST", "/user/modifyPassword", {"id": user_id}, token="not-a-real-token")
    check("伪造 token -> 401", status, 401)

    print("\n=== 6. 带 token 的受保护接口 ===")
    status, text, _ = call(
        "POST",
        "/user/modifyPassword",
        {"id": user_id, "oldPassword": password, "newPassword": new_password},
        token=token,
    )
    check("修改密码成功", json.loads(text)["success"], True)

    payload = login_with_captcha(models, username, new_password)
    check("新密码可以登录", bool(payload), True)

    status, text, _ = call("POST", "/user/deleteUser", {"id": user_id}, token=token)
    check("删除用户成功", json.loads(text)["success"], True)

    # 验证码识别本身会失败，所以一直重试到“验证码通过”的那一次，再看真正的业务提示
    msg = "验证码错误"
    for _ in range(8):
        _, guesses = fetch_captcha(models)
        for guess in guesses:
            status, text, _ = call(
                "POST",
                "/user/login",
                {"username": username, "password": new_password, "code": guess},
            )
            msg = json.loads(text)["msg"]
            if msg != "验证码错误":
                break
        if msg != "验证码错误":
            break
    check("删除后无法登录", msg, "登录失败,用户名或密码错误")

    print("\n=== 结果 ===")
    failed = [r for r in results if not r[0]]
    print(f"共 {len(results)} 项，通过 {len(results) - len(failed)} 项，失败 {len(failed)} 项")
    for _, name, actual, expected in failed:
        print(f"  失败：{name}（实际={actual!r} 期望={expected!r}）")
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
