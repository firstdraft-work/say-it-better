package com.example.communicationoptimizer.config;

import com.example.communicationoptimizer.repository.MysqlConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
@ConditionalOnProperty(prefix = "app.storage", name = "mode", havingValue = "mysql")
public class MysqlSchemaInitializer {

    private final AppMysqlProperties mysqlProperties;
    private final MysqlConnectionFactory connectionFactory;

    public MysqlSchemaInitializer(AppMysqlProperties mysqlProperties, MysqlConnectionFactory connectionFactory) {
        this.mysqlProperties = mysqlProperties;
        this.connectionFactory = connectionFactory;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeSchemaIfNeeded() {
        if (!mysqlProperties.isAutoInitSchema()) {
            return;
        }

        String sqlScript = loadSchemaSql();
        String[] statements = sqlScript.split(";");

        try (Connection connection = connectionFactory.getConnection()) {
            for (String rawStatement : statements) {
                String statementText = rawStatement.trim();
                if (statementText.isEmpty()) {
                    continue;
                }
                try (Statement statement = connection.createStatement()) {
                    statement.execute(statementText);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to initialize MySQL schema", exception);
        }
    }

    private String loadSchemaSql() {
        try {
            ClassPathResource resource = new ClassPathResource("db/mysql/schema.sql");
            byte[] bytes = resource.getInputStream().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load MySQL schema resource", exception);
        }
    }
}
