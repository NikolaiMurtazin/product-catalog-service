package factory;

import repository.AuditRepository;
import repository.JdbcAuditRepository;
import repository.JdbcProductRepository;
import repository.JdbcUserRepository;
import repository.ProductRepository;
import repository.UserRepository;

/**
 * Фабрика для создания экземпляров репозиториев.
 */
public class RepositoryFactory {

    /**
     * @return Экземпляр репозитория для работы с Продуктами.
     */
    public static ProductRepository getProductRepository() {
        return new JdbcProductRepository();
    }

    /**
     * @return Экземпляр репозитория для работы с Пользователями.
     */
    public static UserRepository getUserRepository() {
        return new JdbcUserRepository();
    }

    /**
     * @return Экземпляр репозитория для работы с Аудитом.
     */
    public static AuditRepository getAuditRepository() {
        return new JdbcAuditRepository();
    }
}