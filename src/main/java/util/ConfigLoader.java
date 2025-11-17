package util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Вспомогательный класс для загрузки конфигурации
 * из файла application.properties.
 */
public class ConfigLoader {

    /** Имя файла конфигурации в 'resources'. */
    private static final String CONFIG_FILE = "application.properties";
    /** Ключ для URL базы данных. */
    private static final String DB_URL_KEY = "db.url";
    /** Ключ для имени пользователя БД. */
    private static final String DB_USER_KEY = "db.user";
    /** Ключ для пароля БД. */
    private static final String DB_PASSWORD_KEY = "db.password";
    /** Ключ для схемы таблиц приложения. */
    private static final String DB_APP_SCHEMA_KEY = "db.schema.app";
    /** Ключ для схемы служебных таблиц Liquibase. */
    private static final String DB_LIQUIBASE_SCHEMA_KEY = "db.schema.liquibase";

    /**
     * Хранилище свойств.
     * (Поле НЕ final, чтобы тесты могли "подменить" его.)
     */
    private static Properties properties = new Properties();

    /**
     * Статический блок-инициализатор.
     * Выполняется один раз при загрузке класса в JVM.
     * Загружает {@link #CONFIG_FILE} в {@link #properties}.
     *
     * @throws IllegalStateException  если файл .properties не найден.
     * @throws RuntimeException       если произошла ошибка чтения файла.
     */
    static {
        try (InputStream input = ConfigLoader.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            if (input == null) {
                throw new IllegalStateException(
                        "Файл конфигурации '" + CONFIG_FILE + "' не найден.");
            }

            properties.load(input);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке конфигурации", e);
        }
    }

    /**
     * Получает значение свойства по его ключу.
     *
     * @param key Ключ из application.properties
     * @return Значение свойства
     * @throws IllegalArgumentException если свойство с таким ключом не найдено.
     */
    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException(
                    "Свойство '" + key + "' не найдено в " + CONFIG_FILE);
        }
        return value;
    }

    /**
     * @return URL для подключения к базе данных.
     */
    public static String getDbUrl() {
        return get(DB_URL_KEY);
    }

    /**
     * @return Имя пользователя для подключения к базе данных.
     */
    public static String getDbUser() {
        return get(DB_USER_KEY);
    }

    /**
     * @return Пароль для подключения к базе данных.
     */
    public static String getDbPassword() {
        return get(DB_PASSWORD_KEY);
    }

    /**
     * @return Имя схемы, где хранятся таблицы приложения.
     */
    public static String getAppSchema() {
        return get(DB_APP_SCHEMA_KEY);
    }

    /**
     * @return Имя схемы, где хранятся служебные таблицы Liquibase.
     */
    public static String getLiquibaseSchema() {
        return get(DB_LIQUIBASE_SCHEMA_KEY);
    }
}