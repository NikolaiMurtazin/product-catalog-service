package dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) для представления товара.
 * <p>
 * Используется для передачи данных о товаре между клиентом (через HTTP/JSON)
 * и серверным приложением. Содержит аннотации валидации для проверки корректности данных.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    /**
     * Уникальный идентификатор товара.
     * Может быть {@code null} при создании нового товара (POST запрос).
     */
    private Long id;

    /**
     * Наименование товара. Обязательное поле.
     */
    @NotBlank(message = "Название товара не может быть пустым")
    private String name;

    /**
     * Категория товара.
     */
    private String category;

    /**
     * Бренд производителя.
     */
    private String brand;

    /**
     * Цена товара. Обязательное поле, не может быть отрицательной.
     */
    @NotNull(message = "Цена должна быть указана")
    @Min(value = 0, message = "Цена не может быть отрицательной")
    private Double price;

    /**
     * Количество товара на складе. Не может быть отрицательным.
     */
    @Min(value = 0, message = "Количество не может быть отрицательным")
    private Integer stock;
}