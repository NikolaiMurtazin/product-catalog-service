package service.dto;

/**
 * DTO (Data Transfer Object) для использования в качестве
 * ключа в кэше результатов поиска.
 * <p>
 * Реализован как {@code record}, что автоматически гарантирует
 * корректные {@code equals()}, {@code hashCode()}, {@code toString()}
 * и неизменяемость (immutability).
 *
 * @param category Категория для поиска (может быть null, если не используется)
 * @param brand    Бренд для поиска (может быть null, если не используется)
 * @param minPrice Минимальная цена для поиска (может быть null, если не используется)
 * @param maxPrice Максимальная цена для поиска (может быть null, если не используется)
 */
public record SearchCriteria(String category,
                             String brand,
                             Double minPrice,
                             Double maxPrice) {
}