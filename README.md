# Bloomy 花漾集·多商家鲜花商城

这是一套可本地运行的三端鲜花商城源码，包含买家商城、商家后台和中文管理后台。页面数据来自 Spring Boot API 与 MySQL，购物车、订单、库存、评价、轮播图和公告均已前后端打通。

## 快速开始

1. 先阅读 `运行文档.md`，安装 JDK 17+ 、Maven 3.8+ 、Node.js 20.19+/22.12+ 、MySQL 8 和 Redis 6+。
2. 把 `.env.example` 复制为 `.env`，填写本机 MySQL 密码，并更换 `JWT_SECRET`。
3. macOS 双击 `环境检测.command`，Windows 双击 `环境检测.bat`。
4. 检测通过后双击对应的 `一键启动`脚本。首次启动会安装依赖、运行测试、构建前后端，并仅在空数据库中导入演示数据。
5. 打开 `http://127.0.0.1:5302`。

## 演示账号

| 角色 | 账号 | 密码 | 登录后入口 |
|---|---|---|---|
| 买家 | `customer_li` | `customer123` | `/mall` |
| 商家 | `merchant_rose` | `merchant123` | `/merchant/products` |
| 管理员 | `admin` | `admin123` | `/admin/dashboard` |

## 交付内容

- Vue 3 + TypeScript + Vite 前端源码
- Spring Boot 3 + MyBatis-Plus 后端源码
- MySQL 14 张表的安全初始化 SQL 与演示数据
- macOS / Windows 环境检测、一键启动和一键停止脚本
- 中文运行、操作、数据库、API 与项目详解文档
- 原始课设报告参考版（与当前增强版不完全同步，详见 `论文资料/README.md`）

## 边界说明

- 支付为毕设/课设演示用模拟支付，未对接微信、支付宝或聚合支付。
- 项目是本地演示与二次开发源码，未包含线上商用部署、域名、短信和实名认证。
- 上传只允许 5 MB 以内 JPG/JPEG/PNG/WEBP，且必须登录。
- 请勿在公网环境继续使用默认账号和默认 JWT 密钥。
