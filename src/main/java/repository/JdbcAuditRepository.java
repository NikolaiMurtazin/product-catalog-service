package repository;

import exception.AuditRepositoryException;
import lombok.NonNull;
import util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-реализация репозитория аудита.
 * Работает с 'app_schema.audit_log'.
 */
public class JdbcAuditRepository implements AuditRepository {

    /** SQL-запрос для вставки новой записи аудита. */
    private static final String SAVE_SQL = """
            INSERT INTO app_schema.audit_log (event_log)
            VALUES (?)
            """;

    /** SQL-запрос для получения всех записей аудита, упорядоченных по ID. */
    private static final String FIND_ALL_SQL = """
            SELECT event_log
            FROM app_schema.audit_log
            ORDER BY id
            """;

    /**
     * {@inheritDoc}
     * <p>
     * Выполняет SQL-запрос {@code INSERT} в 'app_schema.audit_log'.
     * Соединение получается и закрывается через {@link ConnectionManager}.
     */
    @Override
    public void save(@NonNull String event) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_SQL)) {

            statement.setString(1, event);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new AuditRepositoryException("Ошибка при сохранении лога аудита", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Выполняет SQL-запрос {@code SELECT} из 'app_schema.audit_log'.
     * Соединение и ResultSet закрываются через {@code try-with-resources}.
     */
    @Override
    public List<String> findAll() {
        List<String> events = new ArrayList<>();

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                events.add(rs.getString("event_log"));
            }

        } catch (SQLException e) {
            throw new AuditRepositoryException("Ошибка при получении логов аудита", e);
        }

        return events;
    }
}