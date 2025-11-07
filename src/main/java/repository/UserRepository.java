package repository;

import model.User;

import java.util.Optional;

/**
 * Интерфейс репозитория для управления сущностями User.
 */
public interface UserRepository {

    /**
     * Находит пользователя по его имени (логину).
     *
     * @param username Имя пользователя
     * @return Optional, содержащий пользователя, если найден
     */
    Optional<User> findByUsername(String username);

    /**
     * Сохраняет пользователя (для первоначального добавления админа).
     *
     * @param user Пользователь для сохранения
     * @return Сохраненный пользователь
     */
    User save(User user);
}