package service;

import model.Product;
import repository.ProductRepository;
import service.dto.SearchCriteria;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация сервиса управления товарами.
 * Включает логику аудита и кэширования.
 */
public class ProductServiceImpl implements ProductService {

    /** Репозиторий для доступа к данным товаров. */
    private final ProductRepository productRepository;

    /** Сервис для логирования CRUD-операций. */
    private final AuditService auditService;

    /**
     * Потокобезопасный кэш для результатов поиска ({@link #searchProducts}).
     * Ключ - {@link SearchCriteria}, Значение - {@link List} of {@link Product}.
     */
    private final Map<SearchCriteria, List<Product>> searchCache =
            new ConcurrentHashMap<>();

    private static final String AUDIT_ACTION_ADD = "ADD_PRODUCT";

    private static final String AUDIT_ACTION_UPDATE = "UPDATE_PRODUCT";

    private static final String AUDIT_ACTION_DELETE = "DELETE_PRODUCT";

    private static final String AUDIT_CACHE_MISS = "CACHE_MISS: Выполняем поиск в репозитории";

    private static final String AUDIT_CACHE_INVALIDATED = "CACHE_INVALIDATED";

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
     * После сохранения, логирует действие "ADD_PRODUCT"
     * и сбрасывает кэш поиска {@link #invalidateCache()}.
     */
    @Override
    public Product addProduct(Product product) {
        Product savedProduct = productRepository.save(product);

        String logMessage = String.format("%s: id=%d, name=%s",
                AUDIT_ACTION_ADD, savedProduct.getId(), savedProduct.getName());
        auditService.logAction(logMessage);

        invalidateCache();
        return savedProduct;
    }

    /**
     * {@inheritDoc}
     * <p>
     * После обновления, логирует действие "UPDATE_PRODUCT"
     * и сбрасывает кэш поиска {@link #invalidateCache()}.
     */
    @Override
    public Product updateProduct(Product product) {
        Product updatedProduct = productRepository.save(product);

        String logMessage = String.format("%s: id=%d",
                AUDIT_ACTION_UPDATE, updatedProduct.getId());
        auditService.logAction(logMessage);

        invalidateCache();
        return updatedProduct;
    }

    /**
     * {@inheritDoc}
     * <p>
     * После удаления, логирует действие "DELETE_PRODUCT"
     * и сбрасывает кэш поиска {@link #invalidateCache()}.
     */
    @Override
    public void deleteProduct(long id) {
        productRepository.deleteById(id);

        String logMessage = String.format("%s: id=%d", AUDIT_ACTION_DELETE, id);
        auditService.logAction(logMessage);

        invalidateCache();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Кэширование для этого метода не используется,
     * так как поиск по ID в In-Memory реализации и так мгновенный (O(1)).
     */
    @Override
    public Optional<Product> getProductById(long id) {
        return productRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Кэширование для этого метода не используется.
     */
    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * {@inheritDoc}
     * <p>
     * **Реализация с кэшированием (Бонус):**
     * 1. При вызове, {@link SearchCriteria} используется как ключ в {@link #searchCache}.
     * 2. Если результат найден в кэше, он возвращается мгновенно.
     * 3. Если нет - выполняется реальный поиск в {@link ProductRepository},
     * результат сохраняется в кэш и возвращается.
     * <p>
     * **Метрика (Бонус):**
     * Измеряет время выполнения запроса (в мс) и выводит в консоль,
     * наглядно демонстрируя разницу между "cache miss" (реальный поиск)
     * и "cache hit" (мгновенный ответ из кэша).
     */
    @Override
    // (Исправление 1: ИЗМЕНЕНА СИГНАТУРА, теперь DTO)
    public List<Product> searchProducts(SearchCriteria criteria) {
        long start = System.nanoTime();

        List<Product> result = searchCache.computeIfAbsent(criteria, k -> {
            auditService.logAction(AUDIT_CACHE_MISS);

            return productRepository.search(k);
        });

        long duration = (System.nanoTime() - start) / 1_000_000;

        if (duration > 0) {
            System.out.println(
                    "\n[Метрика] Поиск (с кэшем) занял: " + duration + " мс");
        } else {
            System.out.println(
                    "\n[Метрика] Поиск (из кэша) занял < 1 мс (мгновенно)");
        }

        return result;
    }

    /**
     * Приватный вспомогательный метод для сброса (инвалидации) кэша поиска.
     * <p>
     * Вызывается при любом изменении данных ({@link #addProduct},
     * {@link #updateProduct}, {@link #deleteProduct}),
     * чтобы гарантировать, что кэш не будет возвращать устаревшие данные.
     * <p>
     * Логирует действие "CACHE_INVALIDATED", если кэш был не пуст.
     */
    private void invalidateCache() {
        if (!searchCache.isEmpty()) {
            searchCache.clear();
            auditService.logAction(AUDIT_CACHE_INVALIDATED);
        }
    }
}