package model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель данных, представляющая товар в каталоге.
 */
@Data
@NoArgsConstructor
public class Product {

    /** Уникальный идентификатор товара. */
    private long id;

    /** Наименование товара. */
    private String name;

    /** Категория товара. */
    private String category;

    /** Бренд/производитель товара. */
    private String brand;

    /** Цена товара. */
    private double price;

    /** Количество товара на складе. */
    private int stock;

    /**
     * Создает экземпляр товара для сохранения (без ID).
     * ID, как предполагается, будет сгенерирован репозиторием.
     *
     * @param name     Наименование товара
     * @param category Категория товара
     * @param brand    Бренд товара
     * @param price    Цена товара
     * @param stock    Количество на складе
     */
    public Product(String name, String category, String brand, double price, int stock) {
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.price = price;
        this.stock = stock;
    }
}