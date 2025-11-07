package service;

import model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс сервиса для управления каталогом товаров.
 * Включает бизнес-логику, такую как аудит и кэширование.
 */
public interface ProductService {

    /**
     * Добавляет новый товар в каталог.
     *
     * @param product Новый товар
     * @return Сохраненный товар
     */
    Product addProduct(Product product);

    /**
     * Обновляет существующий товар.
     *
     * @param product Товар с обновленными данными
     * @return Обновленный товар
     */
    Product updateProduct(Product product);

    /**
     * Удаляет товар по ID.
     *
     * @param id ID товара
     */
    void deleteProduct(long id);

    /**
     * Получает товар по ID.
     *
     * @param id ID товара
     * @return Optional с товаром, если найден
     */
    Optional<Product> getProductById(long id);

    /**
     * Получает все товары.
     *
     * @return Список всех товаров
     */
    List<Product> getAllProducts();

    /**
     * Ищет товары по критериям.
     * (Здесь будет реализовано кэширование)
     *
     * @param category Категория (null, если не используется)
     * @param brand    Бренд (null, если не используется)
     * @param minPrice Мин. цена (null, если не используется)
     * @param maxPrice Макс. цена (null, если не используется)
     * @return Список найденных товаров
     */
    List<Product> searchProducts(String category, String brand,
                                 Double minPrice, Double maxPrice);
}