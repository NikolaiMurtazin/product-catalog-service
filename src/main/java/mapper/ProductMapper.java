package mapper;

import dto.ProductDto;
import model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Маппер для преобразования между сущностью {@link Product} и DTO {@link ProductDto}.
 * <p>
 * Использует библиотеку MapStruct для автоматической генерации кода преобразования
 * на этапе компиляции.
 */
@Mapper
public interface ProductMapper {

    /**
     * Единственный экземпляр (Singleton) маппера, создаваемый MapStruct.
     * Используется для доступа к методам маппинга без внедрения зависимостей.
     */
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    /**
     * Преобразует сущность товара в DTO.
     *
     * @param product Сущность товара (из БД).
     * @return Объект передачи данных (DTO) для отправки клиенту.
     */
    ProductDto toDto(Product product);

    /**
     * Преобразует DTO товара в сущность.
     *
     * @param productDto Данные товара, полученные от клиента.
     * @return Сущность товара для сохранения в БД.
     */
    Product toEntity(ProductDto productDto);

    /**
     * Преобразует список сущностей товаров в список DTO.
     *
     * @param products Список сущностей.
     * @return Список DTO.
     */
    List<ProductDto> toDtoList(List<Product> products);
}