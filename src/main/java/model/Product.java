package model;

import java.util.Objects;

/**
 * Модель данных, представляющая товар в каталоге.
 */
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
     * Создает пустой экземпляр товара.
     */
    public Product() {
    }

    /**
     * Создает экземпляр товара с заданными параметрами.
     * ID не устанавливается, предполагается его генерация репозиторием.
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

    /**
     * @return Уникальный идентификатор товара.
     */
    public long getId() {
        return id;
    }

    /**
     * @param id Уникальный идентификатор товара.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return Наименование товара.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name Наименование товара.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Категория товара.
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category Категория товара.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return Бренд товара.
     */
    public String getBrand() {
        return brand;
    }

    /**
     * @param brand Бренд товара.
     */
    public void setBrand(String brand) {
        this.brand = brand;
    }

    /**
     * @return Цена товара.
     */
    public double getPrice() {
        return price;
    }

    /**
     * @param price Цена товара.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * @return Количество товара на складе.
     */
    public int getStock() {
        return stock;
    }

    /**
     * @param stock Количество товара на складе.
     */
    public void setStock(int stock) {
        this.stock = stock;
    }

    /**
     * Сравнивает этот товар с другим объектом на основе всех полей.
     *
     * @param o Объект для сравнения.
     * @return true, если объекты идентичны, иначе false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id && Double.compare(product.price, price) == 0 &&
                stock == product.stock && Objects.equals(name, product.name) &&
                Objects.equals(category, product.category) &&
                Objects.equals(brand, product.brand);
    }

    /**
     * Вычисляет хэш-код для товара на основе всех полей.
     *
     * @return Хэш-код товара.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name, category, brand, price, stock);
    }

    /**
     * Возвращает отформатированное строковое представление товара.
     *
     * @return Строковое представление товара.
     */
    @Override
    public String toString() {
        return String.format(
                "ID: %d | %s (%s) \n\t Категория: %s \n\t Цена: %.2f руб. \n\t На складе: %d шт.",
                id, name, brand, category, price, stock
        );
    }
}