package aspects;

import aspects.annotation.Audit;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import service.AuditService;
import web.util.ServiceRegistry;

/**
 * Аспект для автоматического аудита действий пользователя.
 * <p>
 * Перехватывает методы, помеченные аннотацией {@link Audit},
 * и записывает информацию о действии в лог через {@link AuditService}.
 */
@Aspect
public class AuditAspect {

    /**
     * Совет (Advice), который выполняется после <b>успешного</b> завершения
     * метода, помеченного аннотацией {@link Audit}.
     * <p>
     * Если метод выбросит исключение, аудит записан не будет (что логично,
     * так как действие не совершилось).
     *
     * @param joinPoint Точка соединения, предоставляющая доступ к метаданным метода.
     */
    @AfterReturning("@annotation(annotation.Audit)")
    public void auditAction(org.aspectj.lang.JoinPoint joinPoint) {
        try {
            AuditService auditService = ServiceRegistry.getAuditService();
            if (auditService == null) {
                System.err.println("AuditService не инициализирован! Аудит пропущен.");
                return;
            }

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Audit annotation = signature.getMethod().getAnnotation(Audit.class);
            String action = annotation.action();

            auditService.logAction(action);

        } catch (Exception e) {
            System.err.println("Ошибка при записи аудита в аспекте: " + e.getMessage());
        }
    }
}