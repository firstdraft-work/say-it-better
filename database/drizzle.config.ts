import { defineConfig } from "drizzle-kit";

export default defineConfig({
  schema: "./src/schema.ts",
  out: "./drizzle",
  dialect: "mysql",
  dbCredentials: {
    url: "mysql://root@127.0.0.1:3306/communication_optimizer",
  },
});
