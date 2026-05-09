# 企业文档云盘系统 (Enterprise Document Cloud Drive)

基于 Spring Boot 构建的轻量级、高安全性的企业内部文档云盘系统后端服务。该系统提供了完整的员工与部门生命周期管理、多层级文件目录操作、细粒度权限隔离，以及基于 AOP 的无侵入式系统操作审计功能。

## 技术栈 (Tech Stack)
* **核心框架:** Java 8+, Spring Boot, Spring Web
* **持久层:** Spring Data JPA, MyBatis-Plus (视复杂查询场景混用)
* **数据安全 & 鉴权:** JWT (JSON Web Token), MD5 Hash 加密
* **架构解耦:** Spring AOP (面向切面编程)

## 核心特性 (Core Features)

### 1. 无状态鉴权与动态安全拦截
* **JWT 令牌机制**: 采用无状态的 HMAC SHA-256 签名算法签发 Token，设定 2 小时严格生命周期。
* **实时状态拦截**: 自定义 `JwtInterceptor`，解析 Token 后实时校验数据库中的用户状态，彻底阻断离职员工 (`status == 0`) 在 Token 未过期时的越权访问风险。
* **按需放行**: 通过 `WebConfig` 精准配置白名单，隔离 Swagger/Knife4j 接口文档与 Spring Boot Actuator 监控端点。

### 2. 组织架构与用户生命周期管理
* **层级部门管理**: 支持最高 3 级的部门树形嵌套。内置严谨的防呆设计（如：禁止将上级部门设为自己、同级不可重名、含有员工或子部门时禁止删除）。
* **数据状态同步**: 当部门更名或员工调岗时，系统会自动触发级联更新，维护部门人数 (`userNum`) 计数器与员工冗余字段的一致性。
* **安全退出机制**: 提供专用的离职接口与账号注销流程，离职前强制校验交接状态（名下是否还有文件资产）。

### 3. 核心云盘与文件 I/O 系统
* **多级目录树**: 文件夹支持最高 5 级深度限制，有效防止无限递归导致的性能雪崩；文件夹移动与重命名均包含严格的边界冲突校验。
* **三级权限隔离**: 底层基于 `AuthType` 枚举，实现资源级别的可见性控制：**私有**（仅创建者）、**部门可见**（同部门在职员工）、**全员可见**。文件移动时自动继承目标文件夹的权限体系。
* **安全的物理落地**:
  * 文件上传按照 `yyyy/MM/dd` 格式动态生成服务器磁盘目录。
  * 物理文件采用 `UUID` 重新命名以杜绝覆盖风险，原文件名安全存入数据库。
  * 严格的后缀白名单校验与单文件 100MB 阈值熔断。

### 4. 无侵入式日志审计 (Audit Log)
* 引入自定义注解 `@AutoLog(value="操作", module="模块")`，结合 Spring AOP (`LogAspect`) 实现业务代码的零侵入审计。
* 全方位溯源：自动解析 HTTP 头部抓取真实 IP，提取操作人信息，并捕获最多 500 字符的入参快照与异常栈信息落地入库。
* 基于 JPA `Specification` 实现了动态条件拼接，支持多维度的日志高频检索。

### 5. 健壮的数据交互层 (DTO & Validation)
* **防线前移**: 结合 JSR-303 规范（`@NotBlank`, `@NotNull`, `@Size` 等）对 `CreateReq` 进行严格入参校验，拦截非法脏数据。
* **统一响应体**: 所有 API 统一通过 `Result<T>` 与 `PageResult<T>` 泛型类包装，提供标准化的状态码与前端交互协议。

## 本地运行与部署 (Getting Started)

### 1. 环境准备
* JDK 1.8 或更高版本
* MySQL 5.7 或 8.0
* Maven 3.6+

### 2. 核心配置修改
请在 `application.yml` 或 `application-local.yml` 中修改以下配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/file_manage?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: your_password

# 配置文件上传的物理根路径（请确保有读写权限）
file:
  upload-dir: C:/file_storage/
