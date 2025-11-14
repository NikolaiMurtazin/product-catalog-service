package integration;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import util.ConfigLoader;
import util.LiquibaseRunner;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * "Родительский" класс для ВСЕХ интеграционных тестов.
 * <p>
 * Он отвечает за:
 * 1. Запуск Docker-контейнера с Postgres (один раз на все тесты).
 * 2. "Подмену" настроек в ConfigLoader, чтобы ConnectionManager
 * подключался к тестовой, а не к рабочей БД.
 * 3. Запуск миграций Liquibase на этой тестовой БД.
 */
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:18-alpine")
                    .withDatabaseName("test_db")
                    .withUsername("test_user")
                    .withPassword("test_password");

    @BeforeAll
    static void setup() throws Exception {
        postgresContainer.start();

        Properties testProperties = new Properties();
        testProperties.setProperty("db.url", postgresContainer.getJdbcUrl());
        testProperties.setProperty("db.user", postgresContainer.getUsername());
        testProperties.setProperty("db.password", postgresContainer.getPassword());
        testProperties.setProperty("db.schema.app", "app_schema"); // Схемы те же
        testProperties.setProperty("db.schema.liquibase", "liquibase_schema");

        Field field = ConfigLoader.class.getDeclaredField("properties");
        field.setAccessible(true);
        field.set(null, testProperties);

        LiquibaseRunner.runMigrations();
    }
}