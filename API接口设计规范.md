# Bloomy API 实现索引

> 本文档按当前代码整理。请求与响应的完整字段以运行后的 Knife4j `http://127.0.0.1:5303/doc.html` 为准。

## 通用约定

- 基础路径：`/api`
- 鉴权：`Authorization: Bearer <token>`
- 成功业务码：`200`
- 上传：`multipart/form-data`，限 5 MB，仅 JPG/JPEG/PNG/WEBP
- 公开接口：登录、注册、找回密码、字典、分类、商城首页、商品与评价查询
- 文件上传不是公开接口，必须登录

## 认证 `/api/auth`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/register` | 买家/商家注册 |
| POST | `/login` | 登录并返回 JWT |
| POST | `/forgot-password` | 用注册手机号重置密码 |
| POST | `/logout` | 退出登录 |
| GET | `/user-info` | 当前用户信息 |

## 公共 `/api/common`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/dict/{dictType}` | 包装、丝带等字典 |
| GET | `/categories` | 启用中的商品分类 |
| POST | `/upload` | 登录用户上传图片，`type` 支持 avatar/product/card/banner/shop/review/misc |

## 买家 `/api/customer`

| 模块 | 接口 |
|---|---|
| 商城 | `GET /home`、`GET /products`、`GET /products/{id}`、`GET /products/{id}/reviews` |
| 购物车 | `POST /cart/add`、`GET /cart/list`、`PUT /cart/{id}`、`DELETE /cart/{id}`、`PATCH /cart/{id}/selected`、`PATCH /cart/select-all` |
| 地址 | `GET/POST /addresses`、`PUT/DELETE /addresses/{id}`、`PATCH /addresses/{id}/default` |
| 订单 | `POST /orders/confirm`、`POST /orders/submit`、`POST /orders/{orderNo}/pay`、`GET /orders`、`GET /orders/{orderNo}`、`POST /orders/{orderNo}/cancel`、`POST /orders/{orderNo}/confirm-receive` |
| 评价 | `POST /reviews` |
| 定制 | `GET /card-templates` |
| 个人 | `GET/PUT /profile`、`PUT /profile/password`、`POST /profile/avatar` |

## 商家 `/api/merchant`

| 模块 | 接口 |
|---|---|
| 工作台 | `GET /workspace` |
| 商品 | `GET/POST /products`、`GET/PUT/DELETE /products/{id}`、`PATCH /products/{id}/status` |
| 订单 | `GET /orders`、`GET /orders/{orderNo}`、`POST /orders/{orderNo}/accept`、`POST /orders/{orderNo}/reject`、`POST /orders/{orderNo}/ship` |
| 库存 | `GET /inventory`、`POST /inventory/adjust` |
| 配送 | `GET /delivery`、`PATCH /delivery/{deliveryId}/status` |
| 评价 | `GET /reviews`、`POST /reviews/{id}/reply` |
| 统计 | `GET /statistics/sales` |
| 店铺 | `GET/PUT /shop-info` |

## 管理员 `/api/admin`

| 模块 | 接口 |
|---|---|
| 仪表盘 | `GET /dashboard` |
| 商品 | `GET /products`、`PUT/DELETE /products/{id}`、`PATCH /products/{id}/status` |
| 订单 | `GET /orders`、`GET /orders/{orderNo}` |
| 用户 | `GET /users`、`PATCH /users/{id}/status`、`DELETE /users/{id}` |
| 商家 | `GET /merchants`、`POST /merchants/{id}/audit`、`PATCH /merchants/{id}/status` |
| 分类 | `GET /categories`、`POST /categories`、`PUT/DELETE /categories/{id}` |
| 公告 | `GET/POST /notices`、`PUT/DELETE /notices/{id}`、`PATCH /notices/{id}/status` |
| 轮播图 | `GET/POST /banners`、`PUT/DELETE /banners/{id}`、`PATCH /banners/{id}/status` |
| 系统 | `GET/DELETE /system/logs`、`GET /system/dict-types`、`GET /system/dict-types/{id}/data` |
