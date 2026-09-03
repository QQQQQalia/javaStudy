# javaStudy

基于 **Spring Boot + MyBatis + MySQL** 的用户注册 / 登录学习项目，用于练习 Web 接口开发与数据库交互。

> 本项目会**持续更新**，新功能、接口和代码示例会不断补充，更新内容会记录在下方「更新记录」中。

## 技术栈

- Java 17
- Spring Boot 4.1.1
- MyBatis（`mybatis-spring-boot-starter` 4.1.0）
- MySQL 8
- Lombok
- Maven

## 已实现功能

- 用户注册：`POST /user/register`
- 用户登录：`POST /user/login`
- 统一响应结构 `BaseVo<T>`：`{ "success": boolean, "msg": string, "data": T }`

## 项目结构

```text
javaStudy
├── pom.xml
├── src
│   ├── main
│   │   ├── java/com/study/sprintbootwithsqldemo
│   │   │   ├── config       # 配置类
│   │   │   ├── controller   # 接口层
│   │   │   ├── model        # DTO / Entity / VO
│   │   │   ├── repository   # MyBatis Mapper
│   │   │   ├── service      # 业务逻辑层
│   │   │   └── util         # 工具类
│   │   └── resources
│   │       └── application.yml
│   └── test
└── mvnw / mvnw.cmd
```

## 本地运行

1. 启动本机 MySQL，并创建数据库和用户表（示例）：

```sql
CREATE DATABASE IF NOT EXISTS test DEFAULT CHARACTER SET utf8mb4;
USE test;

CREATE TABLE IF NOT EXISTS `user` (
    `id`          INT          NOT NULL AUTO_INCREMENT,
    `username`    VARCHAR(50)  NOT NULL UNIQUE,
    `password`    VARCHAR(100) NOT NULL,
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
```

2. 根据本机情况修改 `src/main/resources/application.yml` 中的数据库地址、账号、密码。
3. 启动项目：

```bash
# Windows
mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

4. 启动成功后访问：<http://localhost:3000>

## 接口示例

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
  "password": "123456"
}
```

成功响应：

```json
{
  "success": true,
  "msg": "成功",
  "data": { "id": 1, "username": "tom" }
}
```

## 更新记录

- 2026-09-04：初始化项目并上传，完成用户注册 / 登录接口（Spring Boot + MyBatis + MySQL）。

后续的每次代码更新都会追加到这里。
