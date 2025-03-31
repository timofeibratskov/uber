package com.example.driver_service.config

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.PostgreSQLContainer

object TestPostgresContainer {
    val container: PostgreSQLContainer<out PostgreSQLContainer<*>> = PostgreSQLContainer("postgres:15-alpine").apply {
        withDatabaseName("testdb")
        withUsername("test")
        withPassword("test")
    }

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            // Запускаем контейнер перед получением портов
            if (!container.isRunning) {
                container.start()
            }
            TestPropertyValues.of(
                "spring.datasource.url=${container.jdbcUrl}",
                "spring.datasource.username=${container.username}",
                "spring.datasource.password=${container.password}",
                "spring.datasource.driver-class-name=org.postgresql.Driver",
                "spring.jpa.hibernate.ddl-auto=update",
                "spring.liquibase.change-log=classpath:db/migration/db1.yml"
            ).applyTo(applicationContext.environment)
        }
    }
}
