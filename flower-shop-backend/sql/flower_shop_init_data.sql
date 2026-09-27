SET NAMES utf8mb4;

INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `avatar`, `phone`, `email`, `role`, `shop_name`, `shop_desc`, `shop_logo`, `status`, `deleted`)
VALUES
  (1, 'admin', '$2a$10$M/cFq8HgLeaEWsDtwdFDXOmVAjDv7F1CRMaewpqLQDa4H.IUPUWq6', '系统管理员', NULL, '13800000001', 'admin@flowershop.com', 'ADMIN', NULL, NULL, NULL, 1, 0),
  (2, 'merchant_rose', '$2a$10$IgC0iIA6iub.tIo3DA972.UUHvqq0vRkb0sMZ7U4TL.unz5vU7JJG', '玫瑰花坊', NULL, '13800000002', 'merchant1@flowershop.com', 'MERCHANT', '玫瑰花坊', '专注节日鲜花与告白花束', '/uploads/shop/seed/lisi.jpg', 1, 0),
  (3, 'merchant_lily', '$2a$10$IgC0iIA6iub.tIo3DA972.UUHvqq0vRkb0sMZ7U4TL.unz5vU7JJG', '百合物语', NULL, '13800000003', 'merchant2@flowershop.com', 'MERCHANT', '百合物语', '主打清新礼赠与家庭花艺', '/uploads/shop/seed/wangwu.jpg', 1, 0),
  (4, 'merchant_mix', '$2a$10$IgC0iIA6iub.tIo3DA972.UUHvqq0vRkb0sMZ7U4TL.unz5vU7JJG', '混搭花舍', NULL, '13800000004', 'merchant3@flowershop.com', 'MERCHANT', '混搭花舍', '混搭花束与个性定制专家', '/uploads/shop/seed/lisi.jpg', 1, 0),
  (10, 'customer_li', '$2a$10$LGZdzIz1kPko/uGTrQ75mOViAboPDLuiG9XwN.zXK0UM3.kHsvSn.', '小李', NULL, '13800000010', 'customer1@flowershop.com', 'CUSTOMER', NULL, NULL, NULL, 1, 0),
  (11, 'customer_wang', '$2a$10$LGZdzIz1kPko/uGTrQ75mOViAboPDLuiG9XwN.zXK0UM3.kHsvSn.', '小王', NULL, '13800000011', 'customer2@flowershop.com', 'CUSTOMER', NULL, NULL, NULL, 1, 0),
  (12, 'customer_chen', '$2a$10$LGZdzIz1kPko/uGTrQ75mOViAboPDLuiG9XwN.zXK0UM3.kHsvSn.', '小陈', NULL, '13800000012', 'customer3@flowershop.com', 'CUSTOMER', NULL, NULL, NULL, 1, 0);

INSERT INTO `category` (`id`, `category_name`, `category_icon`, `sort`, `status`)
VALUES
  (1, '玫瑰花束', NULL, 1, 1),
  (2, '百合花束', NULL, 2, 1),
  (3, '混搭花束', NULL, 3, 1),
  (4, '永生花', NULL, 4, 1),
  (5, '礼盒花艺', NULL, 5, 1);

INSERT INTO `product` (`id`, `merchant_id`, `category_id`, `product_no`, `product_name`, `main_image`, `images`, `price`, `original_price`, `stock`, `sales`, `flower_language`, `material_desc`, `detail_content`, `support_customize`, `status`, `deleted`)
VALUES
  (1, 2, 1, 'FSR202603310001', '11朵红玫瑰告白花束', '/uploads/product/seed/rose_bouquet.jpg', '["/uploads/product/seed/rose_bouquet.jpg","/uploads/product/seed/flower_box.jpg"]', 199.00, 239.00, 120, 56, '热烈真诚的爱意', '红玫瑰11朵，尤加利叶，韩式包装', '<p>适合告白与纪念日送礼。</p>', 1, 1, 0),
  (2, 2, 1, 'FSR202603310002', '19朵香槟玫瑰花束', '/uploads/product/seed/rose_bouquet.jpg', '["/uploads/product/seed/rose_bouquet.jpg","/uploads/product/seed/tulip_mix.jpg"]', 269.00, 319.00, 80, 38, '温柔且坚定的喜欢', '香槟玫瑰19朵，洋桔梗，双层雾面纸', '<p>气质香槟色系，适合生日赠礼。</p>', 1, 1, 0),
  (3, 3, 2, 'FSL202603310003', '白百合轻奢花束', '/uploads/product/seed/lily_mix.jpg', '["/uploads/product/seed/lily_mix.jpg","/uploads/product/seed/flower_box.jpg"]', 219.00, 259.00, 60, 24, '纯洁与守护', '白百合6枝，满天星，浅米色包装', '<p>简洁大方，适合长辈和商务赠礼。</p>', 1, 1, 0),
  (4, 3, 2, 'FSL202603310004', '粉百合温柔花礼', '/uploads/product/seed/lily_mix.jpg', '["/uploads/product/seed/lily_mix.jpg","/uploads/product/seed/tulip_mix.jpg"]', 239.00, 289.00, 66, 29, '温暖与治愈', '粉百合8枝，洋甘菊，浅粉包装', '<p>适合探望、生日和日常惊喜。</p>', 1, 1, 0),
  (5, 4, 3, 'FSM202603310005', '向日葵混搭元气花束', '/uploads/product/seed/sunflower.jpg', '["/uploads/product/seed/sunflower.jpg","/uploads/product/seed/rose_bouquet.jpg"]', 189.00, 229.00, 90, 44, '向阳而生', '向日葵3枝，喷泉草，白玫瑰', '<p>适合毕业、升职和鼓励场景。</p>', 1, 1, 0),
  (6, 4, 3, 'FSM202603310006', '郁金香春日混搭花束', '/uploads/product/seed/tulip_mix.jpg', '["/uploads/product/seed/tulip_mix.jpg","/uploads/product/seed/sunflower.jpg"]', 259.00, 299.00, 70, 32, '新的开始', '郁金香10枝，小雏菊，云雾纸', '<p>春日高级感花礼，支持贺卡定制。</p>', 1, 1, 0),
  (7, 2, 4, 'FSE202603310007', '永生玫瑰玻璃罩', '/uploads/product/seed/flower_box.jpg', '["/uploads/product/seed/flower_box.jpg","/uploads/product/seed/rose_bouquet.jpg"]', 329.00, 399.00, 50, 18, '久伴不凋零', '进口永生玫瑰，透明玻璃罩底座', '<p>适合纪念日与长期保存展示。</p>', 0, 1, 0),
  (8, 3, 5, 'FSB202603310008', '生日礼盒花艺', '/uploads/product/seed/flower_box.jpg', '["/uploads/product/seed/flower_box.jpg","/uploads/product/seed/lily_mix.jpg"]', 299.00, 359.00, 75, 21, '满满仪式感', '鲜花礼盒搭配小夜灯与丝带', '<p>礼盒形式更适合生日和庆典。</p>', 1, 1, 0),
  (9, 4, 3, 'FSM202603310009', '毕业季蓝白混搭花束', '/uploads/product/seed/tulip_mix.jpg', '["/uploads/product/seed/tulip_mix.jpg","/uploads/product/seed/lily_mix.jpg"]', 209.00, 249.00, 48, 16, '青春与祝福', '蓝绣球，白玫瑰，桔梗', '<p>毕业拍照与送礼均适合。</p>', 1, 1, 0),
  (10, 2, 1, 'FSR202603310010', '33朵红玫瑰豪华款', '/uploads/product/seed/rose_bouquet.jpg', '["/uploads/product/seed/rose_bouquet.jpg","/uploads/product/seed/flower_box.jpg"]', 399.00, 459.00, 35, 12, '炽热且郑重的爱', '红玫瑰33朵，进口包装纸，皇冠配饰', '<p>适合周年纪念和求婚场景。</p>', 1, 0, 0);

INSERT INTO `address` (`id`, `user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail_address`, `is_default`)
VALUES
  (1, 10, '李明', '13811110001', '上海市', '上海市', '浦东新区', '世纪大道100号1栋1001室', 1),
  (2, 11, '王芳', '13811110002', '江苏省', '南京市', '鼓楼区', '中山路188号6栋502室', 1),
  (3, 12, '陈晨', '13811110003', '浙江省', '杭州市', '西湖区', '文三路66号2栋903室', 1);

INSERT INTO `card_template` (`id`, `template_name`, `template_image`, `scene_type`, `sort`, `status`)
VALUES
  (1, '生日祝福卡', '/uploads/templates/card_rose.jpg', 'birthday', 1, 1),
  (2, '告白心意卡', '/uploads/templates/card_sunflower.jpg', 'valentine', 2, 1),
  (3, '婚礼祝福卡', '/uploads/templates/card_green.jpg', 'wedding', 3, 1);

INSERT INTO `banner` (`id`, `title`, `image_url`, `link_url`, `sort`, `status`)
VALUES
  (1, '春季新品上新', '/uploads/banners/banner1.jpg', '/customer/products/6', 1, 1),
  (2, '告白季热销玫瑰', '/uploads/banners/banner2.jpg', '/customer/products/1', 2, 1),
  (3, '毕业季花礼推荐', '/uploads/banners/banner3.jpg', '/customer/products/9', 3, 1);

INSERT INTO `notice` (`id`, `title`, `content`, `status`, `create_by`)
VALUES
  (1, '春季新品上市通知', '本周上新多款春日混搭花束，欢迎选购。', 1, 1),
  (2, '清明配送时效提醒', '节日期间订单较多，建议提前1天预约配送。', 1, 1);

INSERT INTO `cart_item` (`id`, `user_id`, `product_id`, `quantity`, `selected`)
VALUES
  (1, 10, 1, 1, 1),
  (2, 10, 5, 2, 1),
  (3, 11, 3, 1, 0);

INSERT INTO `order_info` (`id`, `order_no`, `user_id`, `merchant_id`, `total_amount`, `pay_amount`, `order_status`, `pay_type`, `pay_time`, `receiver_name`, `receiver_phone`, `receiver_address`, `delivery_time`, `remark`, `cancel_reason`, `create_time`, `update_time`, `deleted`)
VALUES
  (1, 'FS202603310001', 10, 2, 199.00, 199.00, 4, 3, '2026-03-20 10:10:00', '李明', '13811110001', '上海市上海市浦东新区世纪大道100号1栋1001室', '2026-03-20 18:00:00', '请尽快配送', NULL, '2026-03-20 09:55:00', '2026-03-20 19:30:00', 0),
  (2, 'FS202603310002', 11, 3, 219.00, 219.00, 4, 3, '2026-03-20 11:10:00', '王芳', '13811110002', '江苏省南京市鼓楼区中山路188号6栋502室', '2026-03-20 19:00:00', NULL, NULL, '2026-03-20 10:45:00', '2026-03-20 20:10:00', 0),
  (3, 'FS202603310003', 12, 4, 189.00, 189.00, 3, 3, '2026-03-21 09:15:00', '陈晨', '13811110003', '浙江省杭州市西湖区文三路66号2栋903室', '2026-03-21 17:30:00', '门口联系', NULL, '2026-03-21 09:00:00', '2026-03-21 15:00:00', 0),
  (4, 'FS202603310004', 10, 2, 269.00, 269.00, 2, 3, '2026-03-22 08:30:00', '李明', '13811110001', '上海市上海市浦东新区世纪大道100号1栋1001室', '2026-03-22 17:00:00', NULL, NULL, '2026-03-22 08:10:00', '2026-03-22 12:20:00', 0),
  (5, 'FS202603310005', 11, 4, 259.00, 259.00, 1, 3, '2026-03-22 12:05:00', '王芳', '13811110002', '江苏省南京市鼓楼区中山路188号6栋502室', '2026-03-23 18:00:00', NULL, NULL, '2026-03-22 11:50:00', '2026-03-22 12:05:00', 0),
  (6, 'FS202603310006', 12, 2, 329.00, 329.00, 4, 3, '2026-03-23 10:00:00', '陈晨', '13811110003', '浙江省杭州市西湖区文三路66号2栋903室', '2026-03-23 18:30:00', NULL, NULL, '2026-03-23 09:30:00', '2026-03-23 20:00:00', 0),
  (7, 'FS202603310007', 10, 3, 299.00, 299.00, 4, 3, '2026-03-23 13:05:00', '李明', '13811110001', '上海市上海市浦东新区世纪大道100号1栋1001室', '2026-03-23 20:00:00', '生日礼物', NULL, '2026-03-23 12:40:00', '2026-03-23 21:00:00', 0),
  (8, 'FS202603310008', 11, 4, 209.00, 209.00, 4, 3, '2026-03-24 10:05:00', '王芳', '13811110002', '江苏省南京市鼓楼区中山路188号6栋502室', '2026-03-24 18:00:00', NULL, NULL, '2026-03-24 09:45:00', '2026-03-24 19:10:00', 0),
  (9, 'FS202603310009', 12, 2, 199.00, 199.00, 4, 3, '2026-03-24 11:15:00', '陈晨', '13811110003', '浙江省杭州市西湖区文三路66号2栋903室', '2026-03-24 20:00:00', NULL, NULL, '2026-03-24 10:50:00', '2026-03-24 20:50:00', 0),
  (10, 'FS202603310010', 10, 4, 189.00, 189.00, 5, NULL, NULL, '李明', '13811110001', '上海市上海市浦东新区世纪大道100号1栋1001室', '2026-03-25 18:00:00', NULL, '主动取消', '2026-03-25 09:30:00', '2026-03-25 09:50:00', 0),
  (11, 'FS202603310011', 11, 2, 399.00, 399.00, 4, 3, '2026-03-25 10:00:00', '王芳', '13811110002', '江苏省南京市鼓楼区中山路188号6栋502室', '2026-03-25 19:00:00', NULL, NULL, '2026-03-25 09:20:00', '2026-03-25 21:00:00', 0),
  (12, 'FS202603310012', 12, 3, 239.00, 239.00, 4, 3, '2026-03-26 09:10:00', '陈晨', '13811110003', '浙江省杭州市西湖区文三路66号2栋903室', '2026-03-26 18:00:00', NULL, NULL, '2026-03-26 08:55:00', '2026-03-26 19:40:00', 0),
  (13, 'FS202603310013', 10, 4, 259.00, 259.00, 4, 3, '2026-03-26 10:05:00', '李明', '13811110001', '上海市上海市浦东新区世纪大道100号1栋1001室', '2026-03-26 19:00:00', NULL, NULL, '2026-03-26 09:35:00', '2026-03-26 20:05:00', 0),
  (14, 'FS202603310014', 11, 2, 199.00, 199.00, 4, 3, '2026-03-27 09:40:00', '王芳', '13811110002', '江苏省南京市鼓楼区中山路188号6栋502室', '2026-03-27 18:00:00', NULL, NULL, '2026-03-27 09:00:00', '2026-03-27 19:05:00', 0),
  (15, 'FS202603310015', 12, 3, 219.00, 219.00, 4, 3, '2026-03-27 10:20:00', '陈晨', '13811110003', '浙江省杭州市西湖区文三路66号2栋903室', '2026-03-27 19:30:00', NULL, NULL, '2026-03-27 09:50:00', '2026-03-27 21:20:00', 0),
  (16, 'FS202603310016', 10, 4, 209.00, 209.00, 3, 3, '2026-03-28 09:35:00', '李明', '13811110001', '上海市上海市浦东新区世纪大道100号1栋1001室', '2026-03-28 18:30:00', NULL, NULL, '2026-03-28 09:10:00', '2026-03-28 16:10:00', 0),
  (17, 'FS202603310017', 11, 2, 269.00, 269.00, 2, 3, '2026-03-29 09:50:00', '王芳', '13811110002', '江苏省南京市鼓楼区中山路188号6栋502室', '2026-03-29 18:00:00', NULL, NULL, '2026-03-29 09:20:00', '2026-03-29 11:00:00', 0),
  (18, 'FS202603310018', 12, 2, 329.00, 329.00, 1, 3, '2026-03-29 11:05:00', '陈晨', '13811110003', '浙江省杭州市西湖区文三路66号2栋903室', '2026-03-30 18:00:00', NULL, NULL, '2026-03-29 10:45:00', '2026-03-29 11:05:00', 0),
  (19, 'FS202603310019', 10, 3, 239.00, 239.00, 0, NULL, NULL, '李明', '13811110001', '上海市上海市浦东新区世纪大道100号1栋1001室', '2026-03-31 18:00:00', '待确认支付', NULL, '2026-03-31 09:00:00', '2026-03-31 09:00:00', 0),
  (20, 'FS202603310020', 11, 4, 189.00, 189.00, 0, NULL, NULL, '王芳', '13811110002', '江苏省南京市鼓楼区中山路188号6栋502室', '2026-03-31 20:00:00', '待支付测试单', NULL, '2026-03-31 10:00:00', '2026-03-31 10:00:00', 0);

INSERT INTO `order_item` (`id`, `order_id`, `product_id`, `product_name`, `product_image`, `price`, `quantity`, `subtotal`)
VALUES
  (1, 1, 1, '11朵红玫瑰告白花束', '/uploads/product/seed/rose_bouquet.jpg', 199.00, 1, 199.00),
  (2, 2, 3, '白百合轻奢花束', '/uploads/product/seed/lily_mix.jpg', 219.00, 1, 219.00),
  (3, 3, 5, '向日葵混搭元气花束', '/uploads/product/seed/sunflower.jpg', 189.00, 1, 189.00),
  (4, 4, 2, '19朵香槟玫瑰花束', '/uploads/product/seed/rose_bouquet.jpg', 269.00, 1, 269.00),
  (5, 5, 6, '郁金香春日混搭花束', '/uploads/product/seed/tulip_mix.jpg', 259.00, 1, 259.00),
  (6, 6, 7, '永生玫瑰玻璃罩', '/uploads/product/seed/flower_box.jpg', 329.00, 1, 329.00),
  (7, 7, 8, '生日礼盒花艺', '/uploads/product/seed/flower_box.jpg', 299.00, 1, 299.00),
  (8, 8, 9, '毕业季蓝白混搭花束', '/uploads/product/seed/tulip_mix.jpg', 209.00, 1, 209.00),
  (9, 9, 1, '11朵红玫瑰告白花束', '/uploads/product/seed/rose_bouquet.jpg', 199.00, 1, 199.00),
  (10, 10, 5, '向日葵混搭元气花束', '/uploads/product/seed/sunflower.jpg', 189.00, 1, 189.00),
  (11, 11, 10, '33朵红玫瑰豪华款', '/uploads/product/seed/rose_bouquet.jpg', 399.00, 1, 399.00),
  (12, 12, 4, '粉百合温柔花礼', '/uploads/product/seed/lily_mix.jpg', 239.00, 1, 239.00),
  (13, 13, 6, '郁金香春日混搭花束', '/uploads/product/seed/tulip_mix.jpg', 259.00, 1, 259.00),
  (14, 14, 1, '11朵红玫瑰告白花束', '/uploads/product/seed/rose_bouquet.jpg', 199.00, 1, 199.00),
  (15, 15, 3, '白百合轻奢花束', '/uploads/product/seed/lily_mix.jpg', 219.00, 1, 219.00),
  (16, 16, 9, '毕业季蓝白混搭花束', '/uploads/product/seed/tulip_mix.jpg', 209.00, 1, 209.00),
  (17, 17, 2, '19朵香槟玫瑰花束', '/uploads/product/seed/rose_bouquet.jpg', 269.00, 1, 269.00),
  (18, 18, 7, '永生玫瑰玻璃罩', '/uploads/product/seed/flower_box.jpg', 329.00, 1, 329.00),
  (19, 19, 4, '粉百合温柔花礼', '/uploads/product/seed/lily_mix.jpg', 239.00, 1, 239.00),
  (20, 20, 5, '向日葵混搭元气花束', '/uploads/product/seed/sunflower.jpg', 189.00, 1, 189.00);

INSERT INTO `order_customize` (`id`, `order_item_id`, `card_template_id`, `card_content`, `wrapping_type`, `ribbon_color`, `extra_note`)
VALUES
  (1, 1, 2, '愿我们的故事一直盛开。', 'korean', 'red', '请附上店铺祝福'),
  (2, 4, 1, '生日快乐，天天开心。', 'giftbox', 'pink', '礼品袋一起配送'),
  (3, 7, 1, '生日快乐，愿你万事顺心。', 'giftbox', 'gold', '附加小夜灯'),
  (4, 13, 3, '新婚幸福，百年好合。', 'korean', 'white', '请用婚礼卡片'),
  (5, 19, 1, '支付后请尽快安排。', 'kraft', 'blue', '测试待支付订单');

INSERT INTO `delivery` (`id`, `order_id`, `delivery_man`, `delivery_phone`, `delivery_status`, `shipped_time`, `delivered_time`, `remark`)
VALUES
  (1, 1, '配送员张三', '13910000001', 2, '2026-03-20 14:00:00', '2026-03-20 19:20:00', '已签收'),
  (2, 2, '配送员李四', '13910000002', 2, '2026-03-20 15:10:00', '2026-03-20 20:00:00', '已签收'),
  (3, 3, '配送员王五', '13910000003', 1, '2026-03-21 14:20:00', NULL, '配送中'),
  (4, 6, '配送员赵六', '13910000004', 2, '2026-03-23 14:10:00', '2026-03-23 19:50:00', '已签收'),
  (5, 7, '配送员张华', '13910000005', 2, '2026-03-23 15:20:00', '2026-03-23 20:50:00', '生日单已完成'),
  (6, 8, '配送员刘洋', '13910000006', 2, '2026-03-24 14:30:00', '2026-03-24 19:05:00', '已签收'),
  (7, 16, '配送员杨林', '13910000007', 1, '2026-03-28 14:00:00', NULL, '配送中'),
  (8, 11, '配送员周舟', '13910000008', 2, '2026-03-25 14:30:00', '2026-03-25 20:50:00', '高价值订单');

INSERT INTO `review` (`id`, `order_item_id`, `user_id`, `product_id`, `rating`, `content`, `images`, `reply_content`, `reply_time`)
VALUES
  (1, 1, 10, 1, 5, '花束很新鲜，包装精致，配送也准时。', '["/uploads/product/seed/rose_bouquet.jpg"]', '感谢您的喜欢，期待再次为您服务。', '2026-03-21 10:00:00'),
  (2, 2, 11, 3, 5, '百合很香，适合送长辈。', NULL, '感谢支持，欢迎下次选购。', '2026-03-21 11:00:00'),
  (3, 7, 10, 8, 4, '礼盒不错，朋友很喜欢。', '["/uploads/product/seed/flower_box.jpg"]', NULL, NULL),
  (4, 11, 11, 10, 5, '效果很震撼，纪念日非常满意。', NULL, NULL, NULL);
