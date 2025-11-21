package aspects.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для маркировки методов, время выполнения которых
 * необходимо замерить и залогировать.
 * <p>
 * Методы, помеченные этой аннотацией, перехватываются аспектом
 * {@code LoggingAspect}, который вычисляет время выполнения
 * и выводит его в консоль (или лог-файл).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Loggable {
}