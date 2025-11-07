package repository;

import model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс репозитория для управления сущностями Product.
 * Определяет стандартные операции CRUD и поиск.
 */
public interface ProductRepository {

    /**
     * Сохраняет (создает или обновляет) товар.
     * Если id товара 0 - создает новый, иначе обновляет существующий.
     *
     * @param product Товар для сохранения
     * @return Сохраненный товар (возможно, с новым ID)
     */
    Product save(Product product);

    /**
     * Находит товар по его ID.
     *
     * @param id ID товара
     * @return Optional, содержащий товар, если найден, или пустой Optional
     */
    Optional<Product> findById(long id);

    /**
     * Возвращает список всех товаров.
     *
     * @return Список всех товаров
     */
    List<Product> findAll();

    /**
     * Удаляет товар по его ID.
     *
     * @param id ID товара для удаления
     */
    void deleteById(long id);

    /**
     * Выполняет поиск и фильтрацию товаров по заданным критериям.
     * Если какой-либо параметр равен null, он не используется в фильтрации.
     *
     * @param category Категория (может быть null)
     * @param brand    Бренд (может быть null)
     * @param minPrice Минимальная цена (может быть null)
     * @param maxPrice Максимальная цена (может быть null)
     * @return Список отфильтрованных товаров
     */
    List<Product> search(String category, String brand, Double minPrice, Double maxPrice);
}
