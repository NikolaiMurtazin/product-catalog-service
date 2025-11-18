package service;

import model.User;
import repository.AuditRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса аудита.
 * Этот сервис ЗАВИСИТ от AuthService, чтобы знать,
 * какой пользователь выполнил действие.
 */
public class AuditServiceImpl implements AuditService {

    /** Репозиторий для сохранения записей аудита. */
    private final AuditRepository auditRepository;

    /**
     * Сервис аутентификации. Внедряется через сеттер
     * ({@link #setAuthService(AuthService)})
     * для разрешения циклической зависимости.
     */
    private AuthService authService;

    /** Форматтер для отметки времени в логах аудита. */
    private static final DateTimeFormatter AUDIT_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Имя пользователя по умолчанию для системных событий аудита. */
    private static final String DEFAULT_AUDIT_USERNAME = "SYSTEM";


    /**
     * Создает экземпляр сервиса аудита.
     *
     * @param auditRepository Репозиторий для сохранения логов.
     */
    public AuditServiceImpl(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    /**
     * Внедряет {@link AuthService} для разрешения циклической зависимости.
     * Этот метод должен быть вызван после создания {@code AuditServiceImpl}
     * и {@code AuthServiceImpl}.
     *
     * @param authService Сервис аутентификации.
     */
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Формирует запись лога, включающую отметку времени,
     * имя текущего пользователя (полученное из {@link AuthService})
     * и описание действия.
     */
    @Override
    public void logAction(String action) {
        String username = DEFAULT_AUDIT_USERNAME;

        if (authService != null) {
            Optional<User> userOpt = authService.getCurrentUser();
            if (userOpt.isPresent()) {
                username = userOpt.get().getUsername();
            }
        }

        String timestamp = LocalDateTime.now().format(AUDIT_TIMESTAMP_FORMATTER);
        String logEntry = String.format("[%s] User: [%s] - Action: [%s]",
                timestamp, username, action);

        auditRepository.save(logEntry);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Прямой вызов метода {@code findAll()} из репозитория.
     */
    @Override
    public List<String> getAuditHistory() {
        return auditRepository.findAll();
    }
}