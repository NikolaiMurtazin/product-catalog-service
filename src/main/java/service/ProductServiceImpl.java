package service;

import aspects.annotation.Audit;
import aspects.annotation.Loggable;
import model.Product;
import repository.ProductRepository;
import service.dto.SearchCriteria;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация сервиса управления товарами.
 * Включает логику аудита (через Аспекты) и кэширования поиска.
 */
public class ProductServiceImpl implements ProductService {

    /**
     * Репозиторий для доступа к данным товаров.
     */
    private final ProductRepository productRepository;

    /**
     * Сервис для логирования действий.
     * Используется здесь только для логирования технических событий кэша.
     * Основной бизнес-аудит выполняется через аспект {@code AuditAspect}.
     */
    private final AuditService auditService;

    /**
     * Потокобезопасный кэш для результатов поиска.
     * Ключ - {@link SearchCriteria}, Значение - {@link List} товаров.
     */
    private final Map<SearchCriteria, List<Product>> searchCache =
            new ConcurrentHashMap<>();

    // Константы для технических логов кэша
    private static final String LOG_CACHE_MISS = "CACHE_MISS: Выполняем поиск в репозитории";
    private static final String LOG_CACHE_INVALIDATED = "CACHE_INVALIDATED";

    /**
     * Создает экземпляр сервиса управления товарами.
     *
     * @param productRepository Репозиторий для доступа к данным товаров.
     * @param auditService      Сервис для логирования действий.
     */
    public ProductServiceImpl(ProductRepository productRepository,
                              AuditService auditService) {
        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Метод помечен {@link Audit}, поэтому действие будет записано в лог автоматически.
     * После сохранения происходит сброс кэша поиска.
     */
    @Override
    @Loggable
    @Audit(action = "Создание товара")
    public Product addProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        invalidateCache();
        return savedProduct;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Метод помечен {@link Audit}, поэтому действие будет записано в лог автоматически.
     * После обновления происходит сброс кэша поиска.
     */
    @Override
    @Loggable
    @Audit(action = "Обновление товара")
    public Product updateProduct(Product product) {
        Product updatedProduct = productRepository.save(product);
        // Ручной лог убран, работает аспект
        invalidateCache();
        return updatedProduct;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Метод помечен {@link Audit}, поэтому действие будет записано в лог автоматически.
     * После удаления происходит сброс кэша поиска.
     */
    @Override
    @Loggable
    @Audit(action = "Удаление товара")
    public void deleteProduct(long id) {
        productRepository.deleteById(id);
        // Ручной лог убран, работает аспект
        invalidateCache();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Кэширование не используется для поиска по ID.
     */
    @Override
    // @Loggable здесь можно не ставить, если метод очень быстрый,
    // но для единообразия можно и оставить.
    public Optional<Product> getProductById(long id) {
        return productRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Использует внутренний кэш {@link #searchCache}.
     * Если результаты поиска для данных критериев уже есть в кэше,
     * они возвращаются мгновенно. Иначе выполняется запрос в БД.
     */
    @Override
    @Loggable // Аспект сам замерит время выполнения (с кэшем или без)
    public List<Product> searchProducts(SearchCriteria criteria) {
        // Ручной замер времени (System.nanoTime) убран, так как есть @Loggable

        return searchCache.computeIfAbsent(criteria, k -> {
            // Этот лог оставим, чтобы видеть работу кэша
            auditService.logAction(LOG_CACHE_MISS);
            return productRepository.search(k);
        });
    }

    /**
     * Приватный метод для сброса (инвалидации) кэша поиска.
     * Вызывается при любом изменении данных.
     */
    private void invalidateCache() {
        if (!searchCache.isEmpty()) {
            searchCache.clear();
            auditService.logAction(LOG_CACHE_INVALIDATED);
        }
    }
}