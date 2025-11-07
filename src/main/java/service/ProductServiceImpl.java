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
        auditService.logAction("ADD_PRODUCT: id=" + savedProduct.getId() +
                ", name=" + savedProduct.getName());

        // При любом изменении данных - СБРАСЫВАЕМ КЭШ
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
        auditService.logAction("UPDATE_PRODUCT: id=" + updatedProduct.getId());

        invalidateCache(); // СБРОС КЭША
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
        auditService.logAction("DELETE_PRODUCT: id=" + id);

        invalidateCache(); // СБРОС КЭША
    }

    /**
     * {@inheritDoc}
     * <p>
     * Кэширование для этого метода не используется,
     * так как поиск по ID в In-Memory реализации и так мгновенный (O(1)).
     */
    @Override
    public Optional<Product> getProductById(long id) {
        // Здесь кэш не нужен, т.к. findById в Map и так мгновенный
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
    public List<Product> searchProducts(String category, String brand,
                                        Double minPrice, Double maxPrice) {

        // 1. Создаем ключ по текущим критериям
        SearchCriteria criteria = new SearchCriteria(category, brand,
                minPrice, maxPrice);

        // 2. Ищем в кэше.
        // computeIfAbsent - атомарная операция:
        // "если по ключу criteria что-то есть - верни это.
        //  если нет - выполни лямбду (k -> ...),
        //  положи результат в кэш и верни его."

        long start = System.nanoTime(); // Метрика (бонус)

        List<Product> result = searchCache.computeIfAbsent(criteria, k -> {
            // Эта лямбда выполнится, только если в кэше ПУСТО
            auditService.logAction("CACHE_MISS: Выполняем поиск в репозитории");
            // k.category() - это вызов аксессора из record
            return productRepository.search(k.category(), k.brand(),
                    k.minPrice(), k.maxPrice());
        });

        long duration = (System.nanoTime() - start) / 1_000_000; // в мс

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
            auditService.logAction("CACHE_INVALIDATED");
        }
    }
}
