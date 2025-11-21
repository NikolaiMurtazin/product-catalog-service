package exception;

/**
 * Не-проверяемое (unchecked) исключение,
 * сигнализирующее об ошибке при доступе к данным в
 * {@link repository.AuditRepository}.
 * <p>
 * Является оберткой для {@link java.sql.SQLException}.
 */
public class AuditRepositoryException extends RuntimeException {

    /**
     * Конструктор с сообщением и оригинальной причиной (Exception).
     *
     * @param message Описание ошибки
     * @param cause   Оригинальное исключение (например, SQLException)
     */
    public AuditRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
