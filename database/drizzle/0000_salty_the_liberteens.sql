-- Current sql file was generated after introspecting the database
-- If you want to run this migration please uncomment this code before executing migrations
/*
CREATE TABLE `app_user` (
	`id` bigint AUTO_INCREMENT NOT NULL,
	`open_id` varchar(64) NOT NULL,
	`union_id` varchar(64),
	`nickname` varchar(64),
	`avatar_url` varchar(255),
	`status` tinyint NOT NULL DEFAULT 1,
	`created_at` datetime NOT NULL DEFAULT (CURRENT_TIMESTAMP),
	`updated_at` datetime NOT NULL DEFAULT (CURRENT_TIMESTAMP),
	CONSTRAINT `app_user_id` PRIMARY KEY(`id`),
	CONSTRAINT `open_id` UNIQUE(`open_id`)
);
--> statement-breakpoint
CREATE TABLE `communication_record` (
	`id` bigint AUTO_INCREMENT NOT NULL,
	`user_id` bigint NOT NULL,
	`source_type` varchar(16) NOT NULL,
	`input_text` text,
	`asr_text` text,
	`normalized_text` text NOT NULL,
	`scene_code` varchar(32),
	`scene_source` varchar(16) NOT NULL DEFAULT 'auto',
	`relation_code` varchar(32),
	`goal_code` varchar(32),
	`tone_tags` json,
	`emotion_level` tinyint NOT NULL DEFAULT 0,
	`provider_code` varchar(32),
	`model_name` varchar(64),
	`prompt_version` varchar(32),
	`status` varchar(16) NOT NULL DEFAULT 'success',
	`favorite` tinyint NOT NULL DEFAULT 0,
	`selected_variant_type` varchar(32),
	`extra_meta` json,
	`created_at` datetime NOT NULL DEFAULT (CURRENT_TIMESTAMP),
	`updated_at` datetime NOT NULL DEFAULT (CURRENT_TIMESTAMP),
	CONSTRAINT `communication_record_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `communication_variant` (
	`id` bigint AUTO_INCREMENT NOT NULL,
	`record_id` bigint NOT NULL,
	`variant_type` varchar(32) NOT NULL,
	`title` varchar(32) NOT NULL,
	`content` text NOT NULL,
	`style_tags` json,
	`sort_order` tinyint NOT NULL DEFAULT 1,
	`tts_media_id` bigint,
	`created_at` datetime NOT NULL DEFAULT (CURRENT_TIMESTAMP),
	CONSTRAINT `communication_variant_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `media_asset` (
	`id` bigint AUTO_INCREMENT NOT NULL,
	`user_id` bigint NOT NULL,
	`record_id` bigint,
	`variant_id` bigint,
	`asset_type` varchar(16) NOT NULL,
	`storage_provider` varchar(32) NOT NULL,
	`bucket_name` varchar(64),
	`object_key` varchar(255) NOT NULL,
	`file_url` varchar(512) NOT NULL,
	`mime_type` varchar(64),
	`duration_ms` int DEFAULT 0,
	`size_bytes` bigint DEFAULT 0,
	`status` varchar(16) NOT NULL DEFAULT 'active',
	`created_at` datetime NOT NULL DEFAULT (CURRENT_TIMESTAMP),
	CONSTRAINT `media_asset_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `user_feedback` (
	`id` bigint AUTO_INCREMENT NOT NULL,
	`user_id` bigint NOT NULL,
	`record_id` bigint NOT NULL,
	`variant_type` varchar(32),
	`action_type` varchar(32) NOT NULL,
	`score` tinyint,
	`comment_text` varchar(255),
	`created_at` datetime NOT NULL DEFAULT (CURRENT_TIMESTAMP),
	CONSTRAINT `user_feedback_id` PRIMARY KEY(`id`)
);

*/