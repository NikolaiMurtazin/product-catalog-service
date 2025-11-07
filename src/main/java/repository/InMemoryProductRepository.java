package repository;

import model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory реализация репозитория товаров.
 * Хранит товары в потокобезопасной Map.
 */
public class InMemoryProductRepository implements ProductRepository {

    /**
     * Потокобезопасное In-memory хранилище для товаров.
     * Ключ - ID товара (Long), Значение - объект Product.
     * Используем {@link ConcurrentHashMap} для безопасности при
     * одновременном доступе.
     */
    private static final Map<Long, Product> storage = new ConcurrentHashMap<>();

    /**
     * Потокобезопасный генератор уникальных ID для новых товаров.
     * Используем {@link AtomicLong} для атомарного инкремента.
     */
    private static final AtomicLong idCounter = new AtomicLong(0);

    /**
     * {@inheritDoc}
     * <p>
     * Генерирует новый ID с помощью {@link AtomicLong}, если
     * ID сохраняемого товара равен 0.
     */
    @Override
    public Product save(Product product) {
        if (product.getId() == 0) { // Это новый продукт
            product.setId(idCounter.incrementAndGet());
        }
        storage.put(product.getId(), product);
        return product;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Поиск в {@link ConcurrentHashMap} выполняется за O(1).
     * Оборачивает результат в {@link Optional#ofNullable(Object)}.
     */
    @Override
    public Optional<Product> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Возвращает <strong>новую копию</strong> списка всех значений
     * ({@code new ArrayList<>(storage.values())}),
     * чтобы предотвратить модификацию внутреннего хранилища извне.
     */
    @Override
    public List<Product> findAll() {
        return new ArrayList<>(storage.values());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Удаляет запись из {@link ConcurrentHashMap} по ключу (ID).
     */
    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Реализация выполнена с помощью Stream API для фильтрации
     * коллекции значений ({@code storage.values()}).
     * Фильтрация происходит поочередно по каждому не-null параметру.
     */
    @Override
    public List<Product> search(String category, String brand,
                                Double minPrice, Double maxPrice) {

        // 1. Берем все значения из Map
        return storage.values().stream()
                // 2. Начинаем фильтрацию
                .filter(product -> category == null ||
                        product.getCategory().equalsIgnoreCase(category))

                .filter(product -> brand == null ||
                        product.getBrand().equalsIgnoreCase(brand))

                .filter(product -> minPrice == null ||
                        product.getPrice() >= minPrice)

                .filter(product -> maxPrice == null ||
                        product.getPrice() <= maxPrice)

                // 3. Собираем результат в новый список
                .collect(Collectors.toList());
    }
}
