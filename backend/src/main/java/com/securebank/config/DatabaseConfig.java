package com.securebank.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    public DataSource dataSource() {
        // Resolve canonical database path consistently:
        // Always prefer the backend/database/ directory if running from project root,
        // or database/ directory if running from backend folder.
        Path targetDbDir;
        if (Files.exists(Paths.get("backend/database"))) {
            targetDbDir = Paths.get("backend/database").toAbsolutePath().normalize();
        } else if (Files.exists(Paths.get("database"))) {
            targetDbDir = Paths.get("database").toAbsolutePath().normalize();
        } else {
            targetDbDir = Paths.get("database").toAbsolutePath().normalize();
        }

        try {
            Files.createDirectories(targetDbDir);
        } catch (Exception e) {
            logger.error("Failed to create database directory: {}", e.getMessage());
        }

        Path dbFilePath = targetDbDir.resolve("securebank.db");
        String sqliteUrl = "jdbc:sqlite:" + dbFilePath.toString().replace("\\", "/");
        logger.info("Connecting SQLite database at: {}", sqliteUrl);

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setUrl(sqliteUrl);
        return dataSource;
    }
}
