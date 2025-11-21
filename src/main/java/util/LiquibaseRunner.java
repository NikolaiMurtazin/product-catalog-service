package util;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Вспомогательный класс для программного запуска миграций Liquibase
 * при старте приложения.
 */
public class LiquibaseRunner {

    /**
     * Путь к главному changelog-файлу Liquibase в 'resources'.
     */
    private static final String CHANGELOG_MASTER_FILE = "db/changelog/db.changelog-master.xml";
    /**
     * SQL-команда для создания схемы, если она не существует.
     */
    private static final String SQL_CREATE_SCHEMA_IF_NOT_EXISTS = "CREATE SCHEMA IF NOT EXISTS ";
    /**
     * Сообщение в консоль: проверка схем.
     */
    private static final String MSG_SCHEMA_CHECK_COMPLETE = "Проверка схем завершена.";
    /**
     * Сообщение в консоль: старт миграций.
     */
    private static final String MSG_MIGRATION_START = "Запуск миграций Liquibase...";
    /**
     * Сообщение в консоль: миграции завершены.
     */
    private static final String MSG_MIGRATION_SUCCESS = "Миграции Liquibase успешно выполнены.";
    /**
     * Сообщение об ошибке в случае сбоя миграций.
     */
    private static final String ERR_MIGRATION_FAILED = "Ошибка при выполнении миграций Liquibase";


    /**
     * Запускает миграции Liquibase.
     *
     * @throws RuntimeException Оборачивает {@link SQLException} или {@link LiquibaseException}
     *                          в случае сбоя.
     */
    public static void runMigrations() {
        try (Connection connection = ConnectionManager.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                String appSchemaSql = SQL_CREATE_SCHEMA_IF_NOT_EXISTS
                        + ConfigLoader.getAppSchema();
                String liquibaseSchemaSql = SQL_CREATE_SCHEMA_IF_NOT_EXISTS
                        + ConfigLoader.getLiquibaseSchema();

                statement.execute(appSchemaSql);
                statement.execute(liquibaseSchemaSql);
                System.out.println(MSG_SCHEMA_CHECK_COMPLETE);
            }

            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(
                            new JdbcConnection(connection)
                    );

            database.setLiquibaseSchemaName(ConfigLoader.getLiquibaseSchema());
            database.setDefaultSchemaName(ConfigLoader.getAppSchema());

            Liquibase liquibase = new Liquibase(
                    CHANGELOG_MASTER_FILE,
                    new ClassLoaderResourceAccessor(),
                    database
            );

            System.out.println(MSG_MIGRATION_START);
            liquibase.update(new Contexts(), new LabelExpression());
            System.out.println(MSG_MIGRATION_SUCCESS);

        } catch (SQLException | LiquibaseException e) {
            throw new RuntimeException(ERR_MIGRATION_FAILED, e);
        }
    }
}