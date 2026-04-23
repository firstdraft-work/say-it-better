package com.example.communicationoptimizer.repository;

import com.example.communicationoptimizer.config.AppMysqlProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
@ConditionalOnProperty(prefix = "app.storage", name = "mode", havingValue = "mysql")
public class MysqlConnectionFactory {

    private final AppMysqlProperties mysqlProperties;

    public MysqlConnectionFactory(AppMysqlProperties mysqlProperties) {
        this.mysqlProperties = mysqlProperties;
        try {
            Class.forName(mysqlProperties.getDriverClassName());
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("MySQL driver not found", exception);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                mysqlProperties.getUrl(),
                mysqlProperties.getUsername(),
                mysqlProperties.getPassword()
        );
    }
}
