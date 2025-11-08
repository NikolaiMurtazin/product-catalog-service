package repository;

import model.Product;
import service.dto.SearchCriteria;

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
     *
     * @param criteria Объект-параметр, содержащий все критерии фильтрации.
     * @return Список отфильтрованных товаров
     * @see SearchCriteria
     */
    List<Product> search(SearchCriteria criteria);
}