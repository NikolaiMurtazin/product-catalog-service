package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Менеджер соединений с базой данных (JDBC).
 * <p>
 * Использует {@link ConfigLoader} для получения
 * параметров подключения.
 */
public class ConnectionManager {

    /** Ключ для URL базы данных из {@code application.properties}. */
    private static final String DB_URL_KEY = "db.url";
    /** Ключ для имени пользователя БД из {@code application.properties}. */
    private static final String DB_USER_KEY = "db.user";
    /** Ключ для пароля БД из {@code application.properties}. */
    private static final String DB_PASSWORD_KEY = "db.password";


    /**
     * Открывает и возвращает новое соединение (Connection) с БД.
     * <p>
     * Параметры подключения берутся из {@link ConfigLoader}.
     *
     * @return Объект {@link Connection}
     * @throws SQLException если произошла ошибка подключения
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                ConfigLoader.get(DB_URL_KEY),
                ConfigLoader.get(DB_USER_KEY),
                ConfigLoader.get(DB_PASSWORD_KEY)
        );
    }
}