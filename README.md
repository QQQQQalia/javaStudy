# javaStudy

基于 **Spring Boot + MyBatis-Plus + MySQL** 的用户注册 / 登录学习项目，用于练习 Web 接口开发、验证码、JWT 鉴权与前后端联调。

> 本项目会**持续更新**，新功能、接口和代码示例会不断补充，更新内容会记录在下方「更新记录」中。

## 技术栈

后端：

- Java 17
- Spring Boot 4.1.1（Jakarta EE，注意用 `jakarta.servlet.*`）
- MyBatis-Plus（`mybatis-plus-spring-boot4-starter` 3.5.17）
- MySQL 8
- JJWT 0.11.5（生成 / 校验 token）
- Kaptcha 2.3.2（验证码图片）
- spring-boot-starter-validation（`@Valid` / `@NotBlank` 参数校验）
- Lombok
- Maven

前端（`web/` 目录）：

- Vue 3 + Vite
- Vue Router（登录页 / 注册页 / 用户中心 / 接口自检台）

## 已实现功能

| 接口 | 说明 | 是否需要 token |
| --- | --- | --- |
| `GET /user/getCode` | 获取验证码图片（base64），答案存在 session 里 | 否 |
| `POST /user/register` | 用户注册 | 否 |
| `POST /user/login` | 用户登录，校验验证码，成功返回 token | 否 |
| `POST /user/modifyPassword` | 修改密码 | 是 |
| `POST /user/deleteUser` | 删除用户 | 是 |
| `POST /user/list` | 用户列表分页查询，支持按 id / 用户名 / 创建时间范围筛选 | 是 |

其他：

- 统一响应结构 `BaseVo<T>`：`{ "success": boolean, "msg": string, "data": T }`
- 分页响应结构 `ListVo<T>`：`{ "total": int, "list": T[], "pageNum": int, "pageSize": int, "hasMore": boolean }`
- JWT 鉴权：除上面三个白名单接口外，其余接口都要在请求头带 `auth: <token>`
- 参数校验：字段缺失或为空返回 `400` + 中文提示；业务失败返回 `200` + `success:false`

## 项目结构

```text
javaStudy
├── pom.xml
├── src
│   ├── main
│   │   ├── java/com/study/sprintbootwithsqldemo
│   │   │   ├── config       # 拦截器、CORS、验证码、全局异常处理等配置
│   │   │   ├── controller   # 接口层
│   │   │   ├── model        # DTO / Entity / VO
│   │   │   ├── repository   # MyBatis-Plus Mapper（BaseMapper）
│   │   │   ├── service      # 业务逻辑层
│   │   │   └── utils        # JWT、时间等工具类
│   │   └── resources
│   │       ├── application.yml
│   │       └── mapper       # 自定义 SQL 的 Mapper XML（走 MyBatis-Plus 默认的 mapper-locations）
│   └── test
├── web                      # Vue 3 前端（登录页 / 注册页 / 用户中心 / 接口自检台）
│   ├── src
│   ├── scripts/api_check.py # 不开浏览器的接口全链路自检脚本
│   └── package.json
└── mvnw / mvnw.cmd
```

> `mapper` 目录名和 Java 包名 `repository` 不一致是正常的：MyBatis-Plus 默认的
> `mapper-locations` 是 `classpath*:/mapper/**/*.xml`，匹配的是 **classpath 里的资源目录**，
> 跟接口所在的 Java 包名无关。XML 也可以改成和接口「同名同包」放法，两种都行。

> 自定义 SQL 的 XML **必须放在 `src/main/resources` 下**。`src/main/java` 里的 `.xml` 不会被打包
> 进 `target/classes`，运行时会报 `Invalid bound statement (not found)`。

## 本地运行

### 1. 准备数据库

启动本机 MySQL，创建数据库和用户表：

```sql
CREATE DATABASE IF NOT EXISTS test DEFAULT CHARACTER SET utf8mb4;
USE test;

CREATE TABLE IF NOT EXISTS `user` (
    `id`          VARCHAR(255) NOT NULL DEFAULT (UUID()),
    `username`    VARCHAR(255) NOT NULL,
    `password`    VARCHAR(255) NOT NULL,
    `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
```

### 2. 启动后端

按本机情况修改 `src/main/resources/application.yml` 里的数据库地址、账号、密码，然后：

```bash
# Windows
mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

启动成功后服务在 <http://localhost:3000>（端口写在 `application.yml` 的 `server.port`）。

### 3. 启动前端

```bash
cd web
npm install
npm run dev
```

浏览器打开 <http://localhost:5173>，未登录会自动跳到登录页。

> 前端端口必须是 **5173**：后端 CORS 只放行了 `http://localhost:5173`，换端口或用 `127.0.0.1` 打开都会被浏览器拦掉。

## 接口示例

### 获取验证码

```http
GET /user/getCode
```

返回一串 `data:image/jpeg;base64,...`，前端直接塞给 `<img>` 的 `src` 即可。验证码文字存在 session 里，所以**登录必须和获取验证码用同一个浏览器会话**（带上 `JSESSIONID` cookie）。

### 注册

```http
POST /user/register
Content-Type: application/json

{
  "username": "tom",
  "password": "123456"
}
```

成功响应：

```json
{ "success": true, "msg": "成功", "data": null }
```

### 登录

```http
POST /user/login
Content-Type: application/json

{
  "username": "tom",
  "password": "123456",
  "code": "a5xc"
}
```

`code` 是验证码图片上的字符。成功响应：

```json
{
  "success": true,
  "msg": "成功",
  "data": {
    "id": "0b2c9f1e-...",
    "username": "tom",
    "token": "eyJ0ZXN0IjoiYXNkMTIzIiwiYWxnIjoiSFMyNTYifQ..."
  }
}
```

失败提示有：`验证码错误`、`登录失败,用户名或密码错误`（用户不存在和密码错误统一成一句，避免被用来试探某个用户名是否注册过）。

### 修改密码 / 删除用户（需要 token）

这两个接口要带登录时拿到的 token：

```http
POST /user/modifyPassword
Content-Type: application/json
auth: eyJ0ZXN0IjoiYXNkMTIzIiwiYWxnIjoiSFMyNTYifQ...

{
  "id": "0b2c9f1e-...",
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

```http
POST /user/deleteUser
Content-Type: application/json
auth: eyJ0ZXN0IjoiYXNkMTIzIiwiYWxnIjoiSFMyNTYifQ...

{
  "id": "0b2c9f1e-..."
}
```

不带 token 或 token 无效返回 `401`。

### 用户列表（需要 token）

```http
POST /user/list
Content-Type: application/json
auth: eyJ0ZXN0IjoiYXNkMTIzIiwiYWxnIjoiSFMyNTYifQ...

{
  "pageNum": 1,
  "pageSize": 3,
  "username": "tom",
  "id": "",
  "startTime": "2026-09-01 00:00:00",
  "endTime": "2026-09-30 23:59:59"
}
```

`pageNum` / `pageSize` 必填；`id`、`username` 为模糊匹配，`startTime` / `endTime` 按 `create_time`
做范围筛选（都传时间字符串即可），四个筛选条件都是可选的，不传就查全部。

成功响应：

```json
{
  "success": true,
  "msg": "成功",
  "data": {
    "total": 10,
    "list": [
      {
        "id": "0b2c9f1e-...",
        "username": "tom",
        "password": "123456",
        "createTime": "2026-09-10 20:31:00",
        "updateTime": null
      }
    ],
    "pageNum": 1,
    "pageSize": 3,
    "hasMore": true
  }
}
```

> 已知待改进（尚未处理）：`list` 里目前直接返回 `User` 实体，`password` 会一并返回，建议后续换成
> 只含 id / username / createTime 的 VO；`total` 目前是**全表数量**，不随筛选条件变化，会让前端
> 分页条算错，建议让统计 SQL 带上同样的筛选条件。

### 参数校验失败

请求字段缺失或为空时返回 `400`：

```json
{ "success": false, "msg": "验证码不能为空", "data": null }
```

## 接口自检脚本

不开浏览器就能跑完整个后端链路（验证码识别、登录、401、改密、删号、跨域预检）：

```bash
cd web
pip install ddddocr
python scripts/api_check.py
```

## 更新记录

- 2026-09-04：初始化项目并上传，完成用户注册 / 登录接口（Spring Boot + MyBatis + MySQL）。
- 2026-09-05：新增修改密码、删除用户接口。
- 2026-09-06：持久层接入 MyBatis-Plus，使用 BaseMapper 重构 Repository 与 Service。
- 2026-09-13：新增验证码接口、登录验证码校验、JWT 生成与鉴权拦截器（含 CORS 携带 cookie）；登录密码错误提示统一为「用户名或密码错误」；新增 `@Valid` 参数校验与全局异常处理；新增 `web` 目录的 Vue 3 前端（登录页 / 注册页 / 用户中心）与接口自检脚本。
- 2026-09-17：新增用户列表分页查询接口 `POST /user/list`（支持 id / 用户名 / 创建时间范围筛选）；自定义 SQL 的 Mapper XML 移到 `src/main/resources/mapper/`，修复打包运行时的 `Invalid bound statement (not found)` 报错；`User` 实体字段 `create_time` / `update_time` 改为驼峰 `createTime` / `updateTime`。

后续的每次代码更新都会追加到这里。
