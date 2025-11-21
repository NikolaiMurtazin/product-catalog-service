package web.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Утилитарный класс для валидации объектов с использованием Jakarta Bean Validation.
 * <p>
 * Позволяет проверить любой объект, помеченный аннотациями валидации
 * (например, {@code @NotNull}, {@code @Min}), и выбросить исключение
 * с описанием всех ошибок, если валидация не прошла.
 */
public class ValidationUtils {

    /**
     * Фабрика валидаторов. Инициализируется один раз (Singleton).
     */
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    /**
     * Экземпляр валидатора, используемый для проверки объектов. Потокобезопасен.
     */
    private static final Validator validator = factory.getValidator();

    /**
     * Проверяет объект на соответствие ограничениям валидации.
     * <p>
     * Если объект валиден, метод просто завершает выполнение.
     * Если найдены нарушения, выбрасывается исключение со списком всех ошибок.
     *
     * @param object Объект для валидации (DTO).
     * @param <T>    Тип объекта.
     * @throws IllegalArgumentException Если найдены ошибки валидации.
     *                                  Сообщение исключения содержит список всех нарушенных полей и описания ошибок.
     */
    public static <T> void validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);

        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException(errorMessage);
        }
    }
}