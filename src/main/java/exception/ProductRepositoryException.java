package exception;

/**
 * Не-проверяемое (unchecked) исключение,
 * сигнализирующее об ошибке при доступе к данным в
 * {@link repository.ProductRepository}.
 *
 * Является оберткой для {@link java.sql.SQLException}.
 */
public class ProductRepositoryException extends RuntimeException {

    /**
     * Конструктор с сообщением и оригинальной причиной (Exception).
     *
     * @param message Описание ошибки
     * @param cause   Оригинальное исключение (например, SQLException)
     */
    public ProductRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
