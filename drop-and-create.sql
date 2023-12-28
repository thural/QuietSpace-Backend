
    alter table comment_entity 
       drop 
       foreign key FK5q5av5arkm3of9b5n493p992p;

    alter table comment_entity 
       drop 
       foreign key FK7u6osru73338guaca8ukops8l;

    alter table comment_like_entity 
       drop 
       foreign key FKj55wbqr3a1ufm4yy08hgtru66;

    alter table comment_like_entity 
       drop 
       foreign key FKipio9fffk4twwfg7gv9216cqx;

    alter table post_entity 
       drop 
       foreign key FK2jmp42lmrw2f3ljd16f1re3c8;

    alter table post_like_entity 
       drop 
       foreign key FK8qcsj5xi5upx9qmwwtaqbuhsb;

    alter table post_like_entity 
       drop 
       foreign key FKhuvd6ht79ssstumg83f1dfayy;

    drop table if exists comment_entity;

    drop table if exists comment_like_entity;

    drop table if exists post_entity;

    drop table if exists post_like_entity;

    drop table if exists user_entity;

    create table comment_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table comment_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        comment_id varchar(36),
        id varchar(36) not null,
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table post_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table post_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table user_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        role varchar(16),
        email varchar(32),
        username varchar(32),
        id varchar(36) not null,
        password varchar(255),
        primary key (id)
    ) engine=InnoDB;

    alter table user_entity 
       add constraint UK_4xad1enskw4j1t2866f7sodrx unique (email);

    alter table user_entity 
       add constraint UK_2jsk4eakd0rmvybo409wgwxuw unique (username);

    alter table comment_entity 
       add constraint FK5q5av5arkm3of9b5n493p992p 
       foreign key (post_id) 
       references post_entity (id);

    alter table comment_entity 
       add constraint FK7u6osru73338guaca8ukops8l 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_like_entity 
       add constraint FKj55wbqr3a1ufm4yy08hgtru66 
       foreign key (comment_id) 
       references comment_entity (id);

    alter table comment_like_entity 
       add constraint FKipio9fffk4twwfg7gv9216cqx 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_entity 
       add constraint FK2jmp42lmrw2f3ljd16f1re3c8 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_like_entity 
       add constraint FK8qcsj5xi5upx9qmwwtaqbuhsb 
       foreign key (post_id) 
       references post_entity (id);

    alter table post_like_entity 
       add constraint FKhuvd6ht79ssstumg83f1dfayy 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_entity 
       drop 
       foreign key FK5q5av5arkm3of9b5n493p992p;

    alter table comment_entity 
       drop 
       foreign key FK7u6osru73338guaca8ukops8l;

    alter table comment_like_entity 
       drop 
       foreign key FKj55wbqr3a1ufm4yy08hgtru66;

    alter table comment_like_entity 
       drop 
       foreign key FKipio9fffk4twwfg7gv9216cqx;

    alter table post_entity 
       drop 
       foreign key FK2jmp42lmrw2f3ljd16f1re3c8;

    alter table post_like_entity 
       drop 
       foreign key FK8qcsj5xi5upx9qmwwtaqbuhsb;

    alter table post_like_entity 
       drop 
       foreign key FKhuvd6ht79ssstumg83f1dfayy;

    drop table if exists comment_entity;

    drop table if exists comment_like_entity;

    drop table if exists post_entity;

    drop table if exists post_like_entity;

    drop table if exists user_entity;

    create table comment_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table comment_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        comment_id varchar(36),
        id varchar(36) not null,
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table post_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table post_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table user_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        role varchar(16),
        email varchar(32),
        username varchar(32),
        id varchar(36) not null,
        password varchar(255),
        primary key (id)
    ) engine=InnoDB;

    alter table user_entity 
       add constraint UK_4xad1enskw4j1t2866f7sodrx unique (email);

    alter table user_entity 
       add constraint UK_2jsk4eakd0rmvybo409wgwxuw unique (username);

    alter table comment_entity 
       add constraint FK5q5av5arkm3of9b5n493p992p 
       foreign key (post_id) 
       references post_entity (id);

    alter table comment_entity 
       add constraint FK7u6osru73338guaca8ukops8l 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_like_entity 
       add constraint FKj55wbqr3a1ufm4yy08hgtru66 
       foreign key (comment_id) 
       references comment_entity (id);

    alter table comment_like_entity 
       add constraint FKipio9fffk4twwfg7gv9216cqx 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_entity 
       add constraint FK2jmp42lmrw2f3ljd16f1re3c8 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_like_entity 
       add constraint FK8qcsj5xi5upx9qmwwtaqbuhsb 
       foreign key (post_id) 
       references post_entity (id);

    alter table post_like_entity 
       add constraint FKhuvd6ht79ssstumg83f1dfayy 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_entity 
       drop 
       foreign key FK5q5av5arkm3of9b5n493p992p;

    alter table comment_entity 
       drop 
       foreign key FK7u6osru73338guaca8ukops8l;

    alter table comment_like_entity 
       drop 
       foreign key FKj55wbqr3a1ufm4yy08hgtru66;

    alter table comment_like_entity 
       drop 
       foreign key FKipio9fffk4twwfg7gv9216cqx;

    alter table post_entity 
       drop 
       foreign key FK2jmp42lmrw2f3ljd16f1re3c8;

    alter table post_like_entity 
       drop 
       foreign key FK8qcsj5xi5upx9qmwwtaqbuhsb;

    alter table post_like_entity 
       drop 
       foreign key FKhuvd6ht79ssstumg83f1dfayy;

    drop table if exists comment_entity;

    drop table if exists comment_like_entity;

    drop table if exists post_entity;

    drop table if exists post_like_entity;

    drop table if exists user_entity;

    create table comment_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table comment_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        comment_id varchar(36),
        id varchar(36) not null,
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table post_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table post_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table user_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        role varchar(16),
        email varchar(32),
        username varchar(32),
        id varchar(36) not null,
        password varchar(255),
        primary key (id)
    ) engine=InnoDB;

    alter table user_entity 
       add constraint UK_4xad1enskw4j1t2866f7sodrx unique (email);

    alter table user_entity 
       add constraint UK_2jsk4eakd0rmvybo409wgwxuw unique (username);

    alter table comment_entity 
       add constraint FK5q5av5arkm3of9b5n493p992p 
       foreign key (post_id) 
       references post_entity (id);

    alter table comment_entity 
       add constraint FK7u6osru73338guaca8ukops8l 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_like_entity 
       add constraint FKj55wbqr3a1ufm4yy08hgtru66 
       foreign key (comment_id) 
       references comment_entity (id);

    alter table comment_like_entity 
       add constraint FKipio9fffk4twwfg7gv9216cqx 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_entity 
       add constraint FK2jmp42lmrw2f3ljd16f1re3c8 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_like_entity 
       add constraint FK8qcsj5xi5upx9qmwwtaqbuhsb 
       foreign key (post_id) 
       references post_entity (id);

    alter table post_like_entity 
       add constraint FKhuvd6ht79ssstumg83f1dfayy 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_entity 
       drop 
       foreign key FK5q5av5arkm3of9b5n493p992p;

    alter table comment_entity 
       drop 
       foreign key FK7u6osru73338guaca8ukops8l;

    alter table comment_like_entity 
       drop 
       foreign key FKj55wbqr3a1ufm4yy08hgtru66;

    alter table comment_like_entity 
       drop 
       foreign key FKipio9fffk4twwfg7gv9216cqx;

    alter table post_entity 
       drop 
       foreign key FK2jmp42lmrw2f3ljd16f1re3c8;

    alter table post_like_entity 
       drop 
       foreign key FK8qcsj5xi5upx9qmwwtaqbuhsb;

    alter table post_like_entity 
       drop 
       foreign key FKhuvd6ht79ssstumg83f1dfayy;

    drop table if exists comment_entity;

    drop table if exists comment_like_entity;

    drop table if exists post_entity;

    drop table if exists post_like_entity;

    drop table if exists user_entity;

    create table comment_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table comment_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        comment_id varchar(36),
        id varchar(36) not null,
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table post_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table post_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table user_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        role varchar(16),
        email varchar(32),
        username varchar(32),
        id varchar(36) not null,
        password varchar(255),
        primary key (id)
    ) engine=InnoDB;

    alter table user_entity 
       add constraint UK_4xad1enskw4j1t2866f7sodrx unique (email);

    alter table user_entity 
       add constraint UK_2jsk4eakd0rmvybo409wgwxuw unique (username);

    alter table comment_entity 
       add constraint FK5q5av5arkm3of9b5n493p992p 
       foreign key (post_id) 
       references post_entity (id);

    alter table comment_entity 
       add constraint FK7u6osru73338guaca8ukops8l 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_like_entity 
       add constraint FKj55wbqr3a1ufm4yy08hgtru66 
       foreign key (comment_id) 
       references comment_entity (id);

    alter table comment_like_entity 
       add constraint FKipio9fffk4twwfg7gv9216cqx 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_entity 
       add constraint FK2jmp42lmrw2f3ljd16f1re3c8 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_like_entity 
       add constraint FK8qcsj5xi5upx9qmwwtaqbuhsb 
       foreign key (post_id) 
       references post_entity (id);

    alter table post_like_entity 
       add constraint FKhuvd6ht79ssstumg83f1dfayy 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_entity 
       drop 
       foreign key FK5q5av5arkm3of9b5n493p992p;

    alter table comment_entity 
       drop 
       foreign key FK7u6osru73338guaca8ukops8l;

    alter table comment_like_entity 
       drop 
       foreign key FKj55wbqr3a1ufm4yy08hgtru66;

    alter table comment_like_entity 
       drop 
       foreign key FKipio9fffk4twwfg7gv9216cqx;

    alter table post_entity 
       drop 
       foreign key FK2jmp42lmrw2f3ljd16f1re3c8;

    alter table post_like_entity 
       drop 
       foreign key FK8qcsj5xi5upx9qmwwtaqbuhsb;

    alter table post_like_entity 
       drop 
       foreign key FKhuvd6ht79ssstumg83f1dfayy;

    drop table if exists comment_entity;

    drop table if exists comment_like_entity;

    drop table if exists post_entity;

    drop table if exists post_like_entity;

    drop table if exists user_entity;

    create table comment_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table comment_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        comment_id varchar(36),
        id varchar(36) not null,
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table post_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table post_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table user_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        role varchar(16),
        email varchar(32),
        username varchar(32),
        id varchar(36) not null,
        password varchar(255),
        primary key (id)
    ) engine=InnoDB;

    alter table user_entity 
       add constraint UK_4xad1enskw4j1t2866f7sodrx unique (email);

    alter table user_entity 
       add constraint UK_2jsk4eakd0rmvybo409wgwxuw unique (username);

    alter table comment_entity 
       add constraint FK5q5av5arkm3of9b5n493p992p 
       foreign key (post_id) 
       references post_entity (id);

    alter table comment_entity 
       add constraint FK7u6osru73338guaca8ukops8l 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_like_entity 
       add constraint FKj55wbqr3a1ufm4yy08hgtru66 
       foreign key (comment_id) 
       references comment_entity (id);

    alter table comment_like_entity 
       add constraint FKipio9fffk4twwfg7gv9216cqx 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_entity 
       add constraint FK2jmp42lmrw2f3ljd16f1re3c8 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_like_entity 
       add constraint FK8qcsj5xi5upx9qmwwtaqbuhsb 
       foreign key (post_id) 
       references post_entity (id);

    alter table post_like_entity 
       add constraint FKhuvd6ht79ssstumg83f1dfayy 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_entity 
       drop 
       foreign key FK5q5av5arkm3of9b5n493p992p;

    alter table comment_entity 
       drop 
       foreign key FK7u6osru73338guaca8ukops8l;

    alter table comment_like_entity 
       drop 
       foreign key FKj55wbqr3a1ufm4yy08hgtru66;

    alter table comment_like_entity 
       drop 
       foreign key FKipio9fffk4twwfg7gv9216cqx;

    alter table post_entity 
       drop 
       foreign key FK2jmp42lmrw2f3ljd16f1re3c8;

    alter table post_like_entity 
       drop 
       foreign key FK8qcsj5xi5upx9qmwwtaqbuhsb;

    alter table post_like_entity 
       drop 
       foreign key FKhuvd6ht79ssstumg83f1dfayy;

    drop table if exists comment_entity;

    drop table if exists comment_like_entity;

    drop table if exists post_entity;

    drop table if exists post_like_entity;

    drop table if exists user_entity;

    create table comment_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table comment_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        comment_id varchar(36),
        id varchar(36) not null,
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table post_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table post_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table user_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        role varchar(16),
        email varchar(32),
        username varchar(32),
        id varchar(36) not null,
        password varchar(255),
        primary key (id)
    ) engine=InnoDB;

    alter table user_entity 
       add constraint UK_4xad1enskw4j1t2866f7sodrx unique (email);

    alter table user_entity 
       add constraint UK_2jsk4eakd0rmvybo409wgwxuw unique (username);

    alter table comment_entity 
       add constraint FK5q5av5arkm3of9b5n493p992p 
       foreign key (post_id) 
       references post_entity (id);

    alter table comment_entity 
       add constraint FK7u6osru73338guaca8ukops8l 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_like_entity 
       add constraint FKj55wbqr3a1ufm4yy08hgtru66 
       foreign key (comment_id) 
       references comment_entity (id);

    alter table comment_like_entity 
       add constraint FKipio9fffk4twwfg7gv9216cqx 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_entity 
       add constraint FK2jmp42lmrw2f3ljd16f1re3c8 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_like_entity 
       add constraint FK8qcsj5xi5upx9qmwwtaqbuhsb 
       foreign key (post_id) 
       references post_entity (id);

    alter table post_like_entity 
       add constraint FKhuvd6ht79ssstumg83f1dfayy 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_entity 
       drop 
       foreign key FK5q5av5arkm3of9b5n493p992p;

    alter table comment_entity 
       drop 
       foreign key FK7u6osru73338guaca8ukops8l;

    alter table comment_like_entity 
       drop 
       foreign key FKj55wbqr3a1ufm4yy08hgtru66;

    alter table comment_like_entity 
       drop 
       foreign key FKipio9fffk4twwfg7gv9216cqx;

    alter table post_entity 
       drop 
       foreign key FK2jmp42lmrw2f3ljd16f1re3c8;

    alter table post_like_entity 
       drop 
       foreign key FK8qcsj5xi5upx9qmwwtaqbuhsb;

    alter table post_like_entity 
       drop 
       foreign key FKhuvd6ht79ssstumg83f1dfayy;

    drop table if exists comment_entity;

    drop table if exists comment_like_entity;

    drop table if exists post_entity;

    drop table if exists post_like_entity;

    drop table if exists user_entity;

    create table comment_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table comment_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        comment_id varchar(36),
        id varchar(36) not null,
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table post_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table post_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table user_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        role varchar(16),
        email varchar(32),
        username varchar(32),
        id varchar(36) not null,
        password varchar(255),
        primary key (id)
    ) engine=InnoDB;

    alter table user_entity 
       add constraint UK_4xad1enskw4j1t2866f7sodrx unique (email);

    alter table user_entity 
       add constraint UK_2jsk4eakd0rmvybo409wgwxuw unique (username);

    alter table comment_entity 
       add constraint FK5q5av5arkm3of9b5n493p992p 
       foreign key (post_id) 
       references post_entity (id);

    alter table comment_entity 
       add constraint FK7u6osru73338guaca8ukops8l 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_like_entity 
       add constraint FKj55wbqr3a1ufm4yy08hgtru66 
       foreign key (comment_id) 
       references comment_entity (id);

    alter table comment_like_entity 
       add constraint FKipio9fffk4twwfg7gv9216cqx 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_entity 
       add constraint FK2jmp42lmrw2f3ljd16f1re3c8 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_like_entity 
       add constraint FK8qcsj5xi5upx9qmwwtaqbuhsb 
       foreign key (post_id) 
       references post_entity (id);

    alter table post_like_entity 
       add constraint FKhuvd6ht79ssstumg83f1dfayy 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_entity 
       drop 
       foreign key FK5q5av5arkm3of9b5n493p992p;

    alter table comment_entity 
       drop 
       foreign key FK7u6osru73338guaca8ukops8l;

    alter table comment_like_entity 
       drop 
       foreign key FKj55wbqr3a1ufm4yy08hgtru66;

    alter table comment_like_entity 
       drop 
       foreign key FKipio9fffk4twwfg7gv9216cqx;

    alter table post_entity 
       drop 
       foreign key FK2jmp42lmrw2f3ljd16f1re3c8;

    alter table post_like_entity 
       drop 
       foreign key FK8qcsj5xi5upx9qmwwtaqbuhsb;

    alter table post_like_entity 
       drop 
       foreign key FKhuvd6ht79ssstumg83f1dfayy;

    drop table if exists comment_entity;

    drop table if exists comment_like_entity;

    drop table if exists post_entity;

    drop table if exists post_like_entity;

    drop table if exists user_entity;

    create table comment_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table comment_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        comment_id varchar(36),
        id varchar(36) not null,
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table post_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table post_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table user_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        role varchar(16),
        email varchar(32),
        username varchar(32),
        id varchar(36) not null,
        password varchar(255),
        primary key (id)
    ) engine=InnoDB;

    alter table user_entity 
       add constraint UK_4xad1enskw4j1t2866f7sodrx unique (email);

    alter table user_entity 
       add constraint UK_2jsk4eakd0rmvybo409wgwxuw unique (username);

    alter table comment_entity 
       add constraint FK5q5av5arkm3of9b5n493p992p 
       foreign key (post_id) 
       references post_entity (id);

    alter table comment_entity 
       add constraint FK7u6osru73338guaca8ukops8l 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_like_entity 
       add constraint FKj55wbqr3a1ufm4yy08hgtru66 
       foreign key (comment_id) 
       references comment_entity (id);

    alter table comment_like_entity 
       add constraint FKipio9fffk4twwfg7gv9216cqx 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_entity 
       add constraint FK2jmp42lmrw2f3ljd16f1re3c8 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_like_entity 
       add constraint FK8qcsj5xi5upx9qmwwtaqbuhsb 
       foreign key (post_id) 
       references post_entity (id);

    alter table post_like_entity 
       add constraint FKhuvd6ht79ssstumg83f1dfayy 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_entity 
       drop 
       foreign key FK5q5av5arkm3of9b5n493p992p;

    alter table comment_entity 
       drop 
       foreign key FK7u6osru73338guaca8ukops8l;

    alter table comment_like_entity 
       drop 
       foreign key FKj55wbqr3a1ufm4yy08hgtru66;

    alter table comment_like_entity 
       drop 
       foreign key FKipio9fffk4twwfg7gv9216cqx;

    alter table post_entity 
       drop 
       foreign key FK2jmp42lmrw2f3ljd16f1re3c8;

    alter table post_like_entity 
       drop 
       foreign key FK8qcsj5xi5upx9qmwwtaqbuhsb;

    alter table post_like_entity 
       drop 
       foreign key FKhuvd6ht79ssstumg83f1dfayy;

    drop table if exists comment_entity;

    drop table if exists comment_like_entity;

    drop table if exists post_entity;

    drop table if exists post_like_entity;

    drop table if exists user_entity;

    create table comment_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table comment_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        comment_id varchar(36),
        id varchar(36) not null,
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table post_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table post_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table user_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        role varchar(16),
        email varchar(32),
        username varchar(32),
        id varchar(36) not null,
        password varchar(255),
        primary key (id)
    ) engine=InnoDB;

    alter table user_entity 
       add constraint UK_4xad1enskw4j1t2866f7sodrx unique (email);

    alter table user_entity 
       add constraint UK_2jsk4eakd0rmvybo409wgwxuw unique (username);

    alter table comment_entity 
       add constraint FK5q5av5arkm3of9b5n493p992p 
       foreign key (post_id) 
       references post_entity (id);

    alter table comment_entity 
       add constraint FK7u6osru73338guaca8ukops8l 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_like_entity 
       add constraint FKj55wbqr3a1ufm4yy08hgtru66 
       foreign key (comment_id) 
       references comment_entity (id);

    alter table comment_like_entity 
       add constraint FKipio9fffk4twwfg7gv9216cqx 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_entity 
       add constraint FK2jmp42lmrw2f3ljd16f1re3c8 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_like_entity 
       add constraint FK8qcsj5xi5upx9qmwwtaqbuhsb 
       foreign key (post_id) 
       references post_entity (id);

    alter table post_like_entity 
       add constraint FKhuvd6ht79ssstumg83f1dfayy 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_entity 
       drop 
       foreign key FK5q5av5arkm3of9b5n493p992p;

    alter table comment_entity 
       drop 
       foreign key FK7u6osru73338guaca8ukops8l;

    alter table comment_like_entity 
       drop 
       foreign key FKj55wbqr3a1ufm4yy08hgtru66;

    alter table comment_like_entity 
       drop 
       foreign key FKipio9fffk4twwfg7gv9216cqx;

    alter table post_entity 
       drop 
       foreign key FK2jmp42lmrw2f3ljd16f1re3c8;

    alter table post_like_entity 
       drop 
       foreign key FK8qcsj5xi5upx9qmwwtaqbuhsb;

    alter table post_like_entity 
       drop 
       foreign key FKhuvd6ht79ssstumg83f1dfayy;

    drop table if exists comment_entity;

    drop table if exists comment_like_entity;

    drop table if exists post_entity;

    drop table if exists post_like_entity;

    drop table if exists user_entity;

    create table comment_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table comment_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        comment_id varchar(36),
        id varchar(36) not null,
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table post_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table post_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table user_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        role varchar(16),
        email varchar(32),
        username varchar(32),
        id varchar(36) not null,
        password varchar(255),
        primary key (id)
    ) engine=InnoDB;

    alter table user_entity 
       add constraint UK_4xad1enskw4j1t2866f7sodrx unique (email);

    alter table user_entity 
       add constraint UK_2jsk4eakd0rmvybo409wgwxuw unique (username);

    alter table comment_entity 
       add constraint FK5q5av5arkm3of9b5n493p992p 
       foreign key (post_id) 
       references post_entity (id);

    alter table comment_entity 
       add constraint FK7u6osru73338guaca8ukops8l 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_like_entity 
       add constraint FKj55wbqr3a1ufm4yy08hgtru66 
       foreign key (comment_id) 
       references comment_entity (id);

    alter table comment_like_entity 
       add constraint FKipio9fffk4twwfg7gv9216cqx 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_entity 
       add constraint FK2jmp42lmrw2f3ljd16f1re3c8 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_like_entity 
       add constraint FK8qcsj5xi5upx9qmwwtaqbuhsb 
       foreign key (post_id) 
       references post_entity (id);

    alter table post_like_entity 
       add constraint FKhuvd6ht79ssstumg83f1dfayy 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_entity 
       drop 
       foreign key FK5q5av5arkm3of9b5n493p992p;

    alter table comment_entity 
       drop 
       foreign key FK7u6osru73338guaca8ukops8l;

    alter table comment_like_entity 
       drop 
       foreign key FKj55wbqr3a1ufm4yy08hgtru66;

    alter table comment_like_entity 
       drop 
       foreign key FKipio9fffk4twwfg7gv9216cqx;

    alter table post_entity 
       drop 
       foreign key FK2jmp42lmrw2f3ljd16f1re3c8;

    alter table post_like_entity 
       drop 
       foreign key FK8qcsj5xi5upx9qmwwtaqbuhsb;

    alter table post_like_entity 
       drop 
       foreign key FKhuvd6ht79ssstumg83f1dfayy;

    drop table if exists comment_entity;

    drop table if exists comment_like_entity;

    drop table if exists post_entity;

    drop table if exists post_like_entity;

    drop table if exists user_entity;

    create table comment_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table comment_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        comment_id varchar(36),
        id varchar(36) not null,
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table post_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table post_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table user_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        role varchar(16),
        email varchar(32),
        username varchar(32),
        id varchar(36) not null,
        password varchar(255),
        primary key (id)
    ) engine=InnoDB;

    alter table user_entity 
       add constraint UK_4xad1enskw4j1t2866f7sodrx unique (email);

    alter table user_entity 
       add constraint UK_2jsk4eakd0rmvybo409wgwxuw unique (username);

    alter table comment_entity 
       add constraint FK5q5av5arkm3of9b5n493p992p 
       foreign key (post_id) 
       references post_entity (id);

    alter table comment_entity 
       add constraint FK7u6osru73338guaca8ukops8l 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_like_entity 
       add constraint FKj55wbqr3a1ufm4yy08hgtru66 
       foreign key (comment_id) 
       references comment_entity (id);

    alter table comment_like_entity 
       add constraint FKipio9fffk4twwfg7gv9216cqx 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_entity 
       add constraint FK2jmp42lmrw2f3ljd16f1re3c8 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_like_entity 
       add constraint FK8qcsj5xi5upx9qmwwtaqbuhsb 
       foreign key (post_id) 
       references post_entity (id);

    alter table post_like_entity 
       add constraint FKhuvd6ht79ssstumg83f1dfayy 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_entity 
       drop 
       foreign key FK5q5av5arkm3of9b5n493p992p;

    alter table comment_entity 
       drop 
       foreign key FK7u6osru73338guaca8ukops8l;

    alter table comment_like_entity 
       drop 
       foreign key FKj55wbqr3a1ufm4yy08hgtru66;

    alter table comment_like_entity 
       drop 
       foreign key FKipio9fffk4twwfg7gv9216cqx;

    alter table post_entity 
       drop 
       foreign key FK2jmp42lmrw2f3ljd16f1re3c8;

    alter table post_like_entity 
       drop 
       foreign key FK8qcsj5xi5upx9qmwwtaqbuhsb;

    alter table post_like_entity 
       drop 
       foreign key FKhuvd6ht79ssstumg83f1dfayy;

    drop table if exists comment_entity;

    drop table if exists comment_like_entity;

    drop table if exists post_entity;

    drop table if exists post_like_entity;

    drop table if exists user_entity;

    create table comment_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table comment_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        comment_id varchar(36),
        id varchar(36) not null,
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table post_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table post_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table user_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        role varchar(16),
        email varchar(32),
        username varchar(32),
        id varchar(36) not null,
        password varchar(255),
        primary key (id)
    ) engine=InnoDB;

    alter table user_entity 
       add constraint UK_4xad1enskw4j1t2866f7sodrx unique (email);

    alter table user_entity 
       add constraint UK_2jsk4eakd0rmvybo409wgwxuw unique (username);

    alter table comment_entity 
       add constraint FK5q5av5arkm3of9b5n493p992p 
       foreign key (post_id) 
       references post_entity (id);

    alter table comment_entity 
       add constraint FK7u6osru73338guaca8ukops8l 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_like_entity 
       add constraint FKj55wbqr3a1ufm4yy08hgtru66 
       foreign key (comment_id) 
       references comment_entity (id);

    alter table comment_like_entity 
       add constraint FKipio9fffk4twwfg7gv9216cqx 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_entity 
       add constraint FK2jmp42lmrw2f3ljd16f1re3c8 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_like_entity 
       add constraint FK8qcsj5xi5upx9qmwwtaqbuhsb 
       foreign key (post_id) 
       references post_entity (id);

    alter table post_like_entity 
       add constraint FKhuvd6ht79ssstumg83f1dfayy 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_entity 
       drop 
       foreign key FK5q5av5arkm3of9b5n493p992p;

    alter table comment_entity 
       drop 
       foreign key FK7u6osru73338guaca8ukops8l;

    alter table comment_like_entity 
       drop 
       foreign key FKj55wbqr3a1ufm4yy08hgtru66;

    alter table comment_like_entity 
       drop 
       foreign key FKipio9fffk4twwfg7gv9216cqx;

    alter table post_entity 
       drop 
       foreign key FK2jmp42lmrw2f3ljd16f1re3c8;

    alter table post_like_entity 
       drop 
       foreign key FK8qcsj5xi5upx9qmwwtaqbuhsb;

    alter table post_like_entity 
       drop 
       foreign key FKhuvd6ht79ssstumg83f1dfayy;

    drop table if exists comment_entity;

    drop table if exists comment_like_entity;

    drop table if exists post_entity;

    drop table if exists post_like_entity;

    drop table if exists user_entity;

    create table comment_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table comment_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        comment_id varchar(36),
        id varchar(36) not null,
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table post_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        user_id varchar(36),
        text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table post_like_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        id varchar(36) not null,
        post_id varchar(36),
        user_id varchar(36),
        primary key (id)
    ) engine=InnoDB;

    create table user_entity (
        version integer,
        create_date datetime(6),
        update_date datetime(6),
        role varchar(16),
        email varchar(32),
        username varchar(32),
        id varchar(36) not null,
        password varchar(255),
        primary key (id)
    ) engine=InnoDB;

    alter table user_entity 
       add constraint UK_4xad1enskw4j1t2866f7sodrx unique (email);

    alter table user_entity 
       add constraint UK_2jsk4eakd0rmvybo409wgwxuw unique (username);

    alter table comment_entity 
       add constraint FK5q5av5arkm3of9b5n493p992p 
       foreign key (post_id) 
       references post_entity (id);

    alter table comment_entity 
       add constraint FK7u6osru73338guaca8ukops8l 
       foreign key (user_id) 
       references user_entity (id);

    alter table comment_like_entity 
       add constraint FKj55wbqr3a1ufm4yy08hgtru66 
       foreign key (comment_id) 
       references comment_entity (id);

    alter table comment_like_entity 
       add constraint FKipio9fffk4twwfg7gv9216cqx 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_entity 
       add constraint FK2jmp42lmrw2f3ljd16f1re3c8 
       foreign key (user_id) 
       references user_entity (id);

    alter table post_like_entity 
       add constraint FK8qcsj5xi5upx9qmwwtaqbuhsb 
       foreign key (post_id) 
       references post_entity (id);

    alter table post_like_entity 
       add constraint FKhuvd6ht79ssstumg83f1dfayy 
       foreign key (user_id) 
       references user_entity (id);
