package aspects;

import aspects.annotation.Loggable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Аспект для технического логирования времени выполнения методов.
 * <p>
 * Перехватывает методы, помеченные аннотацией {@link Loggable},
 * измеряет время их выполнения (в миллисекундах) и выводит результат в консоль.
 */
@Aspect
public class LoggingAspect {

    /**
     * Совет (Advice) типа Around, который оборачивает выполнение целевого метода.
     * <p>
     * Выполняет замер времени до и после вызова {@code joinPoint.proceed()},
     * а затем возвращает результат выполнения метода.
     *
     * @param joinPoint Точка соединения, позволяющая управлять выполнением метода.
     * @return Результат выполнения перехваченного метода.
     * @throws Throwable Если перехваченный метод выбросит исключение.
     */
    @Around("@annotation(annotation.Loggable)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - start;

        System.out.println("[LOG] Метод " + joinPoint.getSignature() + " выполнен за " + executionTime + "мс");
        return proceed;
    }
}