package com.example.passenger_service.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestPostgreSQLInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    // Объявляем контейнер как статический, чтобы он запускался один раз для всех тестов
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // Если контейнер ещё не запущен, запускаем его
        if (!postgresContainer.isRunning()) {
            postgresContainer.start();
        }
        // Устанавливаем свойства, полученные из контейнера, в окружение Spring
        TestPropertyValues.of(
                "spring.datasource.url=" + postgresContainer.getJdbcUrl(),
                "spring.datasource.username=" + postgresContainer.getUsername(),
                "spring.datasource.password=" + postgresContainer.getPassword(),
                "spring.datasource.driver-class-name=org.postgresql.Driver",
                "spring.jpa.hibernate.ddl-auto=update",
                "spring.liquibase.change-log=classpath:db/migration/db2.xml"
        ).applyTo(applicationContext.getEnvironment());
    }
}
