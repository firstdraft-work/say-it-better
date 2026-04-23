import { mysqlTable, mysqlSchema, AnyMySqlColumn, primaryKey, unique, bigint, varchar, tinyint, datetime, text, json, int } from "drizzle-orm/mysql-core"
import { sql } from "drizzle-orm"

export const appUser = mysqlTable("app_user", {
	id: bigint({ mode: "number" }).autoincrement().notNull(),
	openId: varchar("open_id", { length: 64 }).notNull(),
	unionId: varchar("union_id", { length: 64 }),
	nickname: varchar({ length: 64 }),
	avatarUrl: varchar("avatar_url", { length: 255 }),
	status: tinyint().default(1).notNull(),
	createdAt: datetime("created_at", { mode: 'string'}).default(sql`(CURRENT_TIMESTAMP)`).notNull(),
	updatedAt: datetime("updated_at", { mode: 'string'}).default(sql`(CURRENT_TIMESTAMP)`).notNull(),
},
(table) => [
	primaryKey({ columns: [table.id], name: "app_user_id"}),
	unique("open_id").on(table.openId),
]);

export const communicationRecord = mysqlTable("communication_record", {
	id: bigint({ mode: "number" }).autoincrement().notNull(),
	userId: bigint("user_id", { mode: "number" }).notNull(),
	sourceType: varchar("source_type", { length: 16 }).notNull(),
	inputText: text("input_text"),
	asrText: text("asr_text"),
	normalizedText: text("normalized_text").notNull(),
	sceneCode: varchar("scene_code", { length: 32 }),
	sceneSource: varchar("scene_source", { length: 16 }).default('auto').notNull(),
	relationCode: varchar("relation_code", { length: 32 }),
	goalCode: varchar("goal_code", { length: 32 }),
	toneTags: json("tone_tags"),
	emotionLevel: tinyint("emotion_level").default(0).notNull(),
	providerCode: varchar("provider_code", { length: 32 }),
	modelName: varchar("model_name", { length: 64 }),
	promptVersion: varchar("prompt_version", { length: 32 }),
	status: varchar({ length: 16 }).default('success').notNull(),
	favorite: tinyint().default(0).notNull(),
	selectedVariantType: varchar("selected_variant_type", { length: 32 }),
	extraMeta: json("extra_meta"),
	createdAt: datetime("created_at", { mode: 'string'}).default(sql`(CURRENT_TIMESTAMP)`).notNull(),
	updatedAt: datetime("updated_at", { mode: 'string'}).default(sql`(CURRENT_TIMESTAMP)`).notNull(),
},
(table) => [
	primaryKey({ columns: [table.id], name: "communication_record_id"}),
]);

export const communicationVariant = mysqlTable("communication_variant", {
	id: bigint({ mode: "number" }).autoincrement().notNull(),
	recordId: bigint("record_id", { mode: "number" }).notNull(),
	variantType: varchar("variant_type", { length: 32 }).notNull(),
	title: varchar({ length: 32 }).notNull(),
	content: text().notNull(),
	styleTags: json("style_tags"),
	sortOrder: tinyint("sort_order").default(1).notNull(),
	ttsMediaId: bigint("tts_media_id", { mode: "number" }),
	createdAt: datetime("created_at", { mode: 'string'}).default(sql`(CURRENT_TIMESTAMP)`).notNull(),
},
(table) => [
	primaryKey({ columns: [table.id], name: "communication_variant_id"}),
]);

export const mediaAsset = mysqlTable("media_asset", {
	id: bigint({ mode: "number" }).autoincrement().notNull(),
	userId: bigint("user_id", { mode: "number" }).notNull(),
	recordId: bigint("record_id", { mode: "number" }),
	variantId: bigint("variant_id", { mode: "number" }),
	assetType: varchar("asset_type", { length: 16 }).notNull(),
	storageProvider: varchar("storage_provider", { length: 32 }).notNull(),
	bucketName: varchar("bucket_name", { length: 64 }),
	objectKey: varchar("object_key", { length: 255 }).notNull(),
	fileUrl: varchar("file_url", { length: 512 }).notNull(),
	mimeType: varchar("mime_type", { length: 64 }),
	durationMs: int("duration_ms").default(0),
	sizeBytes: bigint("size_bytes", { mode: "number" }),
	status: varchar({ length: 16 }).default('active').notNull(),
	createdAt: datetime("created_at", { mode: 'string'}).default(sql`(CURRENT_TIMESTAMP)`).notNull(),
},
(table) => [
	primaryKey({ columns: [table.id], name: "media_asset_id"}),
]);

export const userFeedback = mysqlTable("user_feedback", {
	id: bigint({ mode: "number" }).autoincrement().notNull(),
	userId: bigint("user_id", { mode: "number" }).notNull(),
	recordId: bigint("record_id", { mode: "number" }).notNull(),
	variantType: varchar("variant_type", { length: 32 }),
	actionType: varchar("action_type", { length: 32 }).notNull(),
	score: tinyint(),
	commentText: varchar("comment_text", { length: 255 }),
	createdAt: datetime("created_at", { mode: 'string'}).default(sql`(CURRENT_TIMESTAMP)`).notNull(),
},
(table) => [
	primaryKey({ columns: [table.id], name: "user_feedback_id"}),
]);
