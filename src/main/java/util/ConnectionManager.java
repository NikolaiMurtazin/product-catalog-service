package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Менеджер соединений с базой данных (JDBC).
 * <p>
 * Отвечает за создание соединений с PostgreSQL, используя настройки
 * из {@link ConfigLoader}.
 */
public class ConnectionManager {

    /**
     * Полное имя класса JDBC-драйвера PostgreSQL.
     */
    private static final String DB_DRIVER = "org.postgresql.Driver";

    /**
     * Ключ для URL базы данных из {@code application.properties}.
     */
    private static final String DB_URL_KEY = "db.url";

    /**
     * Ключ для имени пользователя БД из {@code application.properties}.
     */
    private static final String DB_USER_KEY = "db.user";

    /**
     * Ключ для пароля БД из {@code application.properties}.
     */
    private static final String DB_PASSWORD_KEY = "db.password";

    /**
     * Статический блок инициализации.
     * <p>
     * Явно загружает класс драйвера PostgreSQL в память.
     * Это необходимо для корректной работы в некоторых веб-контейнерах (например, Apache Tomcat),
     * где автоматическая регистрация драйверов через SPI может не сработать.
     *
     * @throws RuntimeException если класс драйвера не найден в classpath.
     */
    static {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Драйвер PostgreSQL не найден. Проверьте зависимости.", e);
        }
    }

    /**
     * Открывает и возвращает новое соединение (Connection) с БД.
     * <p>
     * Параметры подключения (URL, логин, пароль) загружаются через {@link ConfigLoader}.
     *
     * @return Новый объект {@link Connection}.
     * @throws SQLException если произошла ошибка при попытке подключения к БД.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                ConfigLoader.get(DB_URL_KEY),
                ConfigLoader.get(DB_USER_KEY),
                ConfigLoader.get(DB_PASSWORD_KEY)
        );
    }
}