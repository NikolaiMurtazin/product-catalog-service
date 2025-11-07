package service;

import model.User;

import java.util.Optional;

/**
 * Интерфейс сервиса для аутентификации и управления сессией пользователя.
 */
public interface AuthService {

    /**
     * Выполняет попытку входа пользователя в систему.
     * В случае успеха - сохраняет пользователя как "текущего".
     *
     * @param username Логин
     * @param password Пароль
     * @return Optional с пользователем в случае успеха, иначе пустой
     */
    Optional<User> login(String username, String password);

    /**
     * Выполняет выход пользователя из системы (очищает "текущего" пользователя).
     */
    void logout();

    /**
     * Возвращает текущего аутентифицированного пользователя.
     *
     * @return Optional с текущим пользователем
     */
    Optional<User> getCurrentUser();

    /**
     * Проверяет, является ли текущий пользователь администратором.
     *
     * @return true, если текущий пользователь - ADMIN, иначе false
     */
    boolean isAdmin();
}
