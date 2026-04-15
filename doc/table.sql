

create table system_notice
(
    id          bigint auto_increment
        primary key,
    receiver_id varchar(36)                           not null comment '接收者用户ID',
    sender_id   varchar(36)                           null comment '发送者ID(系统通知可为null)',
    type        varchar(20)                           not null comment '通知类型: SYSTEM_NOTICE/GROUP_INVITE/FRIEND_INVITE',
    title       varchar(200)                          not null comment '标题',
    content     text                                  null comment '内容',
    related_id  varchar(64)                           null comment '关联业务ID(群邀请=chatRoomId, 好友邀请=senderId)',
    extra_data  varchar(500)                          null comment '扩展数据JSON(如群名/头像等)',
    status      varchar(20) default 'UNREAD'          not null comment '状态: UNREAD/READ/ACCEPTED/REJECTED/EXPIRED',
    expired_at  datetime                              null comment '过期时间(邀请类有效)',
    created_at  datetime    default CURRENT_TIMESTAMP not null,
    updated_at  datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
)
    comment '系统通知';

create index idx_receiver_status
    on system_notice (receiver_id, status);

create index idx_type_status
    on system_notice (type, status);

create table users
(
    id            varchar(32)                           not null comment '用户ID'
        primary key,
    email         varchar(255)                          not null comment '登录邮箱',
    password_hash char(60)                              not null comment 'BCrypt加密密码',
    nickname      varchar(50)                           not null comment '昵称',
    avatar        varchar(255)                          null comment '头像URL',
    status        tinyint(1)  default 0                 null comment '账户状态(0:禁用，1:启用)',
    last_login_at datetime                              null comment '最后登录时间',
    created_at    datetime    default CURRENT_TIMESTAMP null comment '注册时间',
    updated_at    datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '资料更新时间',
    online_status tinyint(1)  default 0                 null comment '在线状态(0离线 1在线)',
    description   varchar(50) default '木都没有喔'      not null comment '个性签名',
    constraint email
        unique (email),
    constraint nickname
        unique (nickname)
)
    comment '用户表' collate = utf8mb4_unicode_ci;

create table chat_room
(
    id          bigint auto_increment comment '聊天室ID（主键，自增）'
        primary key,
    name        varchar(100)                         null comment '聊天室名称（群聊必填，私聊/公共可不填）',
    type        enum ('PUBLIC', 'PRIVATE', 'GROUP')  not null comment '聊天室类型：PUBLIC=公共聊天室，PRIVATE=一对一私聊，GROUP=多人群聊',
    creator_id  varchar(32)                          null comment '创建者ID（外键关联user.id，群聊必填，公共聊天室可为NULL）',
    created_at  timestamp  default CURRENT_TIMESTAMP not null comment '聊天室创建时间',
    description varchar(255)                         null comment '群聊描述（仅GROUP类型有效）',
    avatar_url  varchar(255)                         null comment '群聊头像URL（仅GROUP类型有效）',
    is_active   tinyint(1) default 1                 not null comment '是否激活（1=正常，0=软删除，保留历史数据）',
    is_pin      tinyint    default 0                 not null comment '是否置顶（0否，1置顶）',
    constraint fk_chatroom_creator
        foreign key (creator_id) references users (id)
            on delete set null
)
    comment '聊天室表（公共/私聊/群聊容器）' collate = utf8mb4_unicode_ci;

create index idx_creator_id
    on chat_room (creator_id)
    comment '按创建者查询索引（如群主管理的群聊）';

create index idx_type
    on chat_room (type)
    comment '按类型查询索引（如筛选所有群聊）';

create table message
(
    id           bigint auto_increment comment '消息ID（主键，自增）'
        primary key,
    chat_room_id bigint                                                        not null comment '聊天室ID（外键关联chat_room.id）',
    sender_id    varchar(32)                                                   not null comment '发送者ID（外键关联user.id）',
    content      text                                                          not null comment '消息内容（文本/表情/图片URL等，JSON序列化）',
    content_type enum ('TEXT', 'IMAGE', 'FILE', 'ANNOUNCEMENT') default 'TEXT' not null comment '内容类型：ANNOUNCEMENT=公告（仅ADMIN可发）',
    sent_at      bigint                                                        not null comment '发送时间',
    is_deleted   tinyint(1)                                     default 0      not null comment '是否删除（1=逻辑删除，0=正常）',
    file_name    varchar(255)                                                  null comment '文件名',
    file_size    bigint                                                        null comment '文件大小(字节)',
    constraint fk_message_chatroom
        foreign key (chat_room_id) references chat_room (id)
            on delete cascade,
    constraint fk_message_sender
        foreign key (sender_id) references users (id)
            on delete cascade
)
    comment '消息表（UUID主键/外键，含公告类型）' collate = utf8mb4_unicode_ci;

create table chat_room_member
(
    id                   bigint auto_increment comment '成员关系ID（主键，自增）'
        primary key,
    chat_room_id         bigint                                                      not null comment '聊天室ID（外键关联chat_room.id）',
    user_id              varchar(32)                                                 not null comment '用户ID（外键关联user.id）',
    role                 enum ('OWNER', 'ADMIN', 'MEMBER') default 'MEMBER'          not null comment '成员角色（公共聊天室中ADMIN用户自动获管理权）',
    joined_at            timestamp                         default CURRENT_TIMESTAMP not null comment '加入时间',
    last_read_message_id bigint                                                      null comment '最后阅读消息ID（外键关联message.id）',
    is_muted             tinyint(1)                        default 0                 not null comment '是否禁言（1=禁言，0=正常）',
    is_visible           tinyint(1)                        default 1                 not null comment '是否可见（1=可见，0=隐藏）',
    room_name            varchar(50)                                                 not null comment '聊天室昵称',
    constraint uk_chatroom_user
        unique (chat_room_id, user_id),
    constraint fk_member_chatroom
        foreign key (chat_room_id) references chat_room (id)
            on delete cascade,
    constraint fk_member_last_read
        foreign key (last_read_message_id) references message (id)
            on delete set null,
    constraint fk_member_user
        foreign key (user_id) references users (id)
            on delete cascade
)
    comment '聊天室成员关系表（UUID主键/外键）' collate = utf8mb4_unicode_ci;

create index idx_last_read
    on chat_room_member (last_read_message_id)
    comment '按最后阅读消息ID查询索引';

create index idx_user_id
    on chat_room_member (user_id)
    comment '按用户ID查询索引（获取用户所有房间）';

create definer = root@localhost trigger trg_chat_room_member_set_room_name
    before insert
    on chat_room_member
    for each row
BEGIN
    DECLARE user_nickname VARCHAR(100); -- 声明局部变量

    -- 若插入时未指定room_name（或为NULL），则取users.nickname赋值
    IF NEW.room_name IS NULL OR NEW.room_name = '' THEN
        -- 使用SELECT INTO局部变量
SELECT nickname INTO user_nickname
FROM users
WHERE id = NEW.user_id; -- 关联条件：user_id对应users.id

-- 将局部变量赋值给NEW.room_name
IF user_nickname IS NOT NULL THEN
            SET NEW.room_name = user_nickname;
ELSE
            SET NEW.room_name = '未知用户'; -- 或保留NULL，根据业务定
END IF;
END IF;
END;

create index idx_chat_room_time
    on message (chat_room_id, sent_at)
    comment '按聊天室+时间排序（查历史消息）';

create index idx_sender
    on message (sender_id)
    comment '按发送者查询（查用户发件箱）';

create table user_contacts
(
    id         bigint unsigned auto_increment comment '自增主键ID'
        primary key,
    user_id    varchar(32)                        not null comment '自身用户ID',
    contact_id varchar(32)                        not null comment '联系人用户ID',
    alias      varchar(50)                        null comment '联系人备注名',
    created_at datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at datetime                           null on update CURRENT_TIMESTAMP comment '最后更新时间',
    constraint uk_user_contact
        unique (user_id, contact_id),
    constraint fk_user_contacts_contact_id
        foreign key (contact_id) references users (id)
            on delete cascade,
    constraint fk_user_contacts_user_id
        foreign key (user_id) references users (id)
            on delete cascade
)
    comment '用户联系人表' collate = utf8mb4_unicode_ci;

create index idx_contact_id
    on user_contacts (contact_id);

create index idx_created_at
    on user_contacts (created_at);

create index idx_user_id
    on user_contacts (user_id);

create index idx_email
    on users (email);

create index idx_nickname
    on users (nickname);

create index idx_online_status
    on users (online_status);

