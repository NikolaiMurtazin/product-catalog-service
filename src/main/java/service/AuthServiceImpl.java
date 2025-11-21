package service;

import aspects.annotation.Audit;
import aspects.annotation.Loggable;
import model.Role;
import model.User;
import repository.UserRepository;

import java.util.Optional;

/**
 * Реализация сервиса аутентификации.
 * Управляет состоянием (текущим пользователем).
 */
public class AuthServiceImpl implements AuthService {

    /**
     * Репозиторий для доступа к данным пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Сервис для логирования действий (вход, выход).
     */
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
     * <p>
     * Метод помечен аннотацией {@link Audit}, поэтому факт попытки входа
     * будет автоматически записан в лог аудита.
     */
    @Override
    @Loggable
    @Audit(action = "Попытка входа в систему")
    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPasswordHash().equals(password)) {
                this.currentUser = user;
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Сбрасывает поле {@code currentUser} в {@code null}.
     * Факт выхода записывается в аудит автоматически через аннотацию {@link Audit}.
     */
    @Override
    @Loggable
    @Audit(action = "Выход из системы")
    public void logout() {
        if (currentUser != null) {
            this.currentUser = null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Возвращает внутреннее поле {@code currentUser},
     * обернутое в {@link Optional#ofNullable(Object)}.
     */
    @Override
    @Loggable
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
    @Loggable
    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }
}