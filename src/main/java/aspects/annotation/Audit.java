package aspects.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для маркировки методов, выполнение которых должно быть
 * зафиксировано в системе аудита.
 * <p>
 * Методы, помеченные этой аннотацией, перехватываются аспектом
 * {@code AuditAspect}, который записывает факт выполнения действия,
 * время и пользователя в базу данных.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Audit {

    /**
     * Описание бизнес-действия (например, "Создание товара", "Вход в систему").
     * <p>
     * Это строковое значение будет сохранено в лог аудита
     * для идентификации события.
     *
     * @return Описание действия.
     */
    String action();
}