package service;

import model.Role;
import model.User;
import repository.UserRepository;

import java.util.Optional;

/**
 * Реализация сервиса аутентификации.
 * Управляет состоянием (текущим пользователем).
 */
public class AuthServiceImpl implements AuthService {

    /** Репозиторий для доступа к данным пользователей. */
    private final UserRepository userRepository;

    /** Сервис для логирования действий (вход, выход). */
    private final AuditService auditService;

    /**
     * Хранит состояние текущей "сессии".
     * {@code null}, если пользователь не аутентифицирован.
     */
    private User currentUser = null;

    /**
     * Создает экземпляр сервиса аутентификации
     * (Внедрение зависимостей через конструктор).
     *
     * @param userRepository Репозиторий для поиска пользователей.
     * @param auditService   Сервис для логирования входа/выхода.
     */
    public AuthServiceImpl(UserRepository userRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    /**
     * {@inheritDoc}
     * <p>
     * ВНИМАНИЕ: В ДЗ-1 проверка пароля выполняется в открытом виде (простое
     * сравнение строк).
     * В случае успеха, пользователь сохраняется в поле {@code currentUser}
     * и выполняется логирование "LOGIN_SUCCESS" через {@link AuditService}.
     * В случае неудачи, логируется "LOGIN_FAILURE".
     */
    @Override
    public Optional<User> login(String username, String password) {
        // Ищем юзера в репозитории
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // В ДЗ-1 мы не хэшируем, а просто сравниваем пароли
            if (user.getPasswordHash().equals(password)) {
                this.currentUser = user; // Сохраняем состояние

                // Логируем успешный вход
                // Мы вызываем logAction ПОСЛЕ установки currentUser,
                // чтобы AuditService знал, КТО выполнил действие.
                auditService.logAction("LOGIN_SUCCESS");
                return Optional.of(user);
            }
        }

        // Логируем неудачный вход (пользователь еще null)
        auditService.logAction("LOGIN_FAILURE: username=" + username);
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Выполняет логирование "LOGOUT" через {@link AuditService} (если
     * пользователь был залогинен) и сбрасывает поле {@code currentUser} в {@code null}.
     */
    @Override
    public void logout() {
        if (currentUser != null) {
            auditService.logAction("LOGOUT");
            this.currentUser = null; // Сбрасываем состояние
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Возвращает внутреннее поле {@code currentUser},
     * обернутое в {@link Optional#ofNullable(Object)}.
     */
    @Override
    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Проверяет, что {@code currentUser} не {@code null} и его роль
     * {@link Role#ADMIN}.
     */
    @Override
    public boolean isAdmin() {
        // Проверяем, что юзер есть И что его роль ADMIN
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }
}