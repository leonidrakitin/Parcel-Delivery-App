--liquibase formatted sql
--changeset leonid.rakitin:1
insert into roles (name) values ('ROLE_USER'), ('ROLE_ADMIN'), ('ROLE_COURIER');

--changeset leonid.rakitin:2
insert into users (username,email,password,role_id) values ('admin','admin@admin.com','$2a$10$P93BL8RB7ntjoqAKZ120p.W30yEq/P7h76asBHnRvx3gARfw4RlSa', 2);