package repository;

import model.Role;
import model.User;
import util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * JDBC-реализация репозитория пользователей.
 * Работает с 'app_schema.users'.
 */
public class JdbcUserRepository implements UserRepository {

    /** SQL-запрос для поиска пользователя по имени. */
    private static final String FIND_BY_USERNAME_SQL = """
            SELECT id, username, password_hash, role
            FROM app_schema.users
            WHERE username = ?
            """;

    /** SQL-запрос для вставки нового пользователя. */
    private static final String SAVE_SQL = """
            INSERT INTO app_schema.users (username, password_hash, role)
            VALUES (?, ?, ?)
            """;

    /**
     * {@inheritDoc}
     * <p>
     * Выполняет SQL-запрос {@code SELECT} с {@code WHERE username = ?}.
     * Соединение и ResultSet закрываются через {@code try-with-resources}.
     */
    @Override
    public Optional<User> findByUsername(String username) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(FIND_BY_USERNAME_SQL)) {

            statement.setString(1, username);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске пользователя: " + username, e);
        }

        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Выполняет SQL-запрос {@code INSERT}, запрашивая
     * {@link Statement#RETURN_GENERATED_KEYS}.
     * Полученный ID устанавливается обратно в переданный объект {@code user}.
     */
    @Override
    public User save(User user) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getRole().name());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Создание пользователя не удалось, ни одна строка не изменена.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Создание пользователя не удалось, ID не получен.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении пользователя: " + user.getUsername(), e);
        }

        return user;
    }

    /**
     * Вспомогательный "маппер" (DRY-принцип).
     * Превращает одну строку {@link ResultSet} в объект {@link User}.
     *
     * @param rs {@link ResultSet}, установленный на текущую строку.
     * @return Заполненный объект {@link User}.
     * @throws SQLException если имя колонки не найдено или тип не совпадает.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(Role.valueOf(rs.getString("role")));
        return user;
    }
}