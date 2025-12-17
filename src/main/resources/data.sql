-- 1. 初始化用户（admin密码：admin123，testuser密码：user123）
INSERT INTO users (username, password, role, enabled, violation_count)
VALUES ('admin', '$2a$12$r3v2rnPsCWCJtkcTvC0SCuAsb9xKahUGAZarNvmjSu3v1CLO23Ore', 'ROLE_ADMIN', true, 0);

INSERT INTO users (username, password, role, enabled, violation_count)
VALUES ('testuser', '$2a$10$8b9n8G7f6d5s4a3q2w1e0r9t8y7u6i5o4p3a2s1d0f9g8h7j6k', 'ROLE_USER', true, 0);

-- 2. 初始化书籍（不变）
INSERT INTO book (name, author, category, stock, publish, description, borrow_count)
VALUES
    ('Java编程思想', 'Bruce Eckel', '编程', 10, '机械工业出版社', 'Java经典教程', 0),
    ('Spring Boot实战', 'Craig Walls', '框架', 8, '人民邮电出版社', 'Spring Boot入门到精通', 0),
    ('MySQL必知必会', 'Ben Forta', '数据库', 5, '清华大学出版社', 'MySQL基础教程', 0),
    ('算法导论', 'Thomas H.Cormen', '算法', 3, '麻省理工学院出版社', '经典算法教材', 0);

-- 3. 初始化借阅记录（不变）
INSERT INTO borrow_records (user_id, book_id, borrow_time, is_returned)
VALUES (2, 1, DATEADD('DAY', -5, CURRENT_DATE), false);

INSERT INTO borrow_records (user_id, book_id, borrow_time, is_returned)
VALUES (2, 2, DATEADD('DAY', -10, CURRENT_DATE), false);