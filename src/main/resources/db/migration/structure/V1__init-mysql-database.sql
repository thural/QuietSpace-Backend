-- Create database  quietspace if not exists

CREATE DATABASE IF NOT EXISTS quietspace;

-- Create chat table
CREATE TABLE `chat` (
  `id` varchar(36) NOT NULL,
  `create_date` datetime(6) NOT NULL,
  `update_date` datetime(6) DEFAULT NULL,
  `version` int DEFAULT NULL,
  `created_by` varchar(36) DEFAULT NULL,
  `updated_by` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create user table
CREATE TABLE `user` (
  `account_locked` bit(1) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `status_type` tinyint DEFAULT NULL,
  `version` int DEFAULT NULL,
  `create_date` datetime(6) NOT NULL,
  `date_of_birth` datetime(6) DEFAULT NULL,
  `update_date` datetime(6) DEFAULT NULL,
  `email` varchar(32) DEFAULT NULL,
  `username` varchar(32) DEFAULT NULL,
  `created_by` varchar(36) DEFAULT NULL,
  `id` varchar(36) NOT NULL,
  `updated_by` varchar(36) DEFAULT NULL,
  `firstname` varchar(255) DEFAULT NULL,
  `lastname` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` tinyint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ob8kqyqqgmefl0aco34akdtpe` (`email`),
  UNIQUE KEY `UK_sb8bbouer5wak8vyiiy4pf2bx` (`username`),
  CONSTRAINT `user_chk_1` CHECK ((`status_type` between 0 and 1)),
  CONSTRAINT `user_chk_2` CHECK ((`role` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create post table
CREATE TABLE `post` (
  `id` varchar(36) NOT NULL,
  `create_date` datetime(6) NOT NULL,
  `update_date` datetime(6) DEFAULT NULL,
  `version` int DEFAULT NULL,
  `text` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `user_id` varchar(36) DEFAULT NULL,
  `created_by` varchar(36) DEFAULT NULL,
  `updated_by` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK72mt33dhhs48hf9gcqrq4fxte` (`user_id`),
  CONSTRAINT `FK72mt33dhhs48hf9gcqrq4fxte` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create comment table
CREATE TABLE `comment` (
  `id` varchar(36) NOT NULL,
  `create_date` datetime(6) NOT NULL,
  `update_date` datetime(6) DEFAULT NULL,
  `version` int DEFAULT NULL,
  `parent_id` binary(16) DEFAULT NULL,
  `text` varchar(255) DEFAULT NULL,
  `post_id` varchar(36) DEFAULT NULL,
  `user_id` varchar(36) DEFAULT NULL,
  `created_by` varchar(36) DEFAULT NULL,
  `updated_by` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8kcum44fvpupyw6f5baccx25c` (`user_id`),
  KEY `FKs1slvnkuemjsq2kj4h3vhx7i1` (`post_id`),
  CONSTRAINT `FK8kcum44fvpupyw6f5baccx25c` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKs1slvnkuemjsq2kj4h3vhx7i1` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create follow table
CREATE TABLE `follow` (
  `id` varchar(36) NOT NULL,
  `create_date` datetime(6) NOT NULL,
  `update_date` datetime(6) DEFAULT NULL,
  `version` int DEFAULT NULL,
  `follower_id` varchar(36) DEFAULT NULL,
  `following_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmow2qk674plvwyb4wqln37svv` (`follower_id`),
  KEY `FKqme6uru2g9wx9iysttk542esm` (`following_id`),
  CONSTRAINT `FKmow2qk674plvwyb4wqln37svv` FOREIGN KEY (`follower_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKqme6uru2g9wx9iysttk542esm` FOREIGN KEY (`following_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create message table
CREATE TABLE `message` (
  `id` varchar(36) NOT NULL,
  `create_date` datetime(6) NOT NULL,
  `update_date` datetime(6) DEFAULT NULL,
  `version` int DEFAULT NULL,
  `is_seen` bit(1) DEFAULT NULL,
  `text` varchar(255) DEFAULT NULL,
  `chat_id` varchar(36) DEFAULT NULL,
  `recipient_id` varchar(36) DEFAULT NULL,
  `sender_id` varchar(36) DEFAULT NULL,
  `created_by` varchar(36) DEFAULT NULL,
  `updated_by` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcnj2qaf5yc36v2f90jw2ipl9b` (`sender_id`),
  KEY `FKiup8wew331d92o7u3k8d918o3` (`recipient_id`),
  KEY `FKmejd0ykokrbuekwwgd5a5xt8a` (`chat_id`),
  CONSTRAINT `FKcnj2qaf5yc36v2f90jw2ipl9b` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKiup8wew331d92o7u3k8d918o3` FOREIGN KEY (`recipient_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKmejd0ykokrbuekwwgd5a5xt8a` FOREIGN KEY (`chat_id`) REFERENCES `chat` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create notification table
CREATE TABLE `notification` (
  `id` varchar(36) NOT NULL,
  `create_date` datetime(6) NOT NULL,
  `update_date` datetime(6) DEFAULT NULL,
  `version` int DEFAULT NULL,
  `actor_id` binary(16) DEFAULT NULL,
  `content_id` binary(16) DEFAULT NULL,
  `content_type` enum('POST','COMMENT','MESSAGE') DEFAULT NULL,
  `is_seen` bit(1) DEFAULT NULL,
  `notification_type` enum('FOLLOW_REQUEST','POST_REACTION','COMMENT','COMMENT_REACTION','COMMENT_REPLY','REPOST') DEFAULT NULL,
  `user_id` binary(16) DEFAULT NULL,
  `created_by` varchar(36) DEFAULT NULL,
  `updated_by` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create poll table
CREATE TABLE `poll` (
  `id` varchar(36) NOT NULL,
  `create_date` datetime(6) NOT NULL,
  `update_date` datetime(6) DEFAULT NULL,
  `version` int DEFAULT NULL,
  `due_date` datetime(6) DEFAULT NULL,
  `post_id` varchar(36) DEFAULT NULL,
  `created_by` varchar(36) DEFAULT NULL,
  `updated_by` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_p245s3319v7hiqj4ca6inlb9f` (`post_id`),
  CONSTRAINT `FKj59dhk7k0402xqckwgwwp46tg` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create poll_option table
CREATE TABLE `poll_option` (
  `id` varchar(36) NOT NULL,
  `create_date` datetime(6) NOT NULL,
  `update_date` datetime(6) DEFAULT NULL,
  `version` int DEFAULT NULL,
  `label` varchar(255) DEFAULT NULL,
  `poll_id` varchar(36) DEFAULT NULL,
  `created_by` varchar(36) DEFAULT NULL,
  `updated_by` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK81hniv4vvdii51krao5f84hen` (`poll_id`),
  CONSTRAINT `FK81hniv4vvdii51krao5f84hen` FOREIGN KEY (`poll_id`) REFERENCES `poll` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create poll_option_votes table
CREATE TABLE `poll_option_votes` (
  `poll_option_id` varchar(36) NOT NULL,
  `votes` binary(16) DEFAULT NULL,
  KEY `FK6t2onukcbcqwf6o9trdwfqkwk` (`poll_option_id`),
  CONSTRAINT `FK6t2onukcbcqwf6o9trdwfqkwk` FOREIGN KEY (`poll_option_id`) REFERENCES `poll_option` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create reaction table
CREATE TABLE `reaction` (
  `id` varchar(36) NOT NULL,
  `create_date` datetime(6) NOT NULL,
  `update_date` datetime(6) DEFAULT NULL,
  `version` int DEFAULT NULL,
  `content_id` binary(16) DEFAULT NULL,
  `content_type` enum('POST','COMMENT','MESSAGE') DEFAULT NULL,
  `reaction_type` enum('LIKE','DISLIKE') DEFAULT NULL,
  `user_id` binary(16) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `created_by` varchar(36) DEFAULT NULL,
  `updated_by` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create token table
CREATE TABLE `token` (
  `version` int DEFAULT NULL,
  `create_date` datetime(6) NOT NULL,
  `expire_date` datetime(6) DEFAULT NULL,
  `update_date` datetime(6) DEFAULT NULL,
  `validate_date` datetime(6) DEFAULT NULL,
  `created_by` varchar(36) DEFAULT NULL,
  `id` varchar(36) NOT NULL,
  `updated_by` varchar(36) DEFAULT NULL,
  `user_id` varchar(36) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_pddrhgwxnms2aceeku9s2ewy5` (`token`),
  KEY `FKe32ek7ixanakfqsdaokm4q9y2` (`user_id`),
  CONSTRAINT `FKe32ek7ixanakfqsdaokm4q9y2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create user_chat table
CREATE TABLE `user_chat` (
  `chat_id` varchar(36) NOT NULL,
  `user_id` varchar(36) NOT NULL,
  KEY `FK60ku9ru56yadhj2tu35hshsms` (`chat_id`),
  KEY `FKfw0o0kaepbdntrg31qkbj3en7` (`user_id`),
  CONSTRAINT `FK60ku9ru56yadhj2tu35hshsms` FOREIGN KEY (`chat_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKfw0o0kaepbdntrg31qkbj3en7` FOREIGN KEY (`user_id`) REFERENCES `chat` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create user_followings table
CREATE TABLE `user_followings` (
  `user_id` varchar(36) NOT NULL,
  `followings_id` varchar(36) NOT NULL,
  KEY `FK1f1kxtjhmrvlvrhqmuwf9r7ls` (`followings_id`),
  KEY `FKj2a8435v8kbuogf5d8aaudfrp` (`user_id`),
  CONSTRAINT `FK1f1kxtjhmrvlvrhqmuwf9r7ls` FOREIGN KEY (`followings_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKj2a8435v8kbuogf5d8aaudfrp` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create user_roles table
CREATE TABLE `user_roles` (
  `roles` tinyint DEFAULT NULL,
  `user_id` varchar(36) NOT NULL,
  KEY `FK55itppkw3i07do3h7qoclqd4k` (`user_id`),
  CONSTRAINT `FK55itppkw3i07do3h7qoclqd4k` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `user_roles_chk_1` CHECK ((`roles` between 0 and 2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;