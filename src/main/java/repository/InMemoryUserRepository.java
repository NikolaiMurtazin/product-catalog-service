package repository;

import model.User;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory реализация репозитория пользователей.
 * Хранит пользователей в потокобезопасной Map.
 */
public class InMemoryUserRepository implements UserRepository {

    /**
     * Потокобезопасное In-memory хранилище для пользователей.
     * Ключ - Имя пользователя (String), Значение - объект User.
     * Используем {@link ConcurrentHashMap} для быстрого поиска по
     * имени пользователя O(1).
     */
    private static final Map<String, User> storage = new ConcurrentHashMap<>();

    /**
     * Потокобезопасный генератор уникальных ID для новых пользователей.
     * Используем {@link AtomicLong} для атомарного инкремента.
     */
    private static final AtomicLong idCounter = new AtomicLong(0);

    /**
     * {@inheritDoc}
     * <p>
     * Поиск в {@link ConcurrentHashMap} выполняется за O(1).
     * Оборачивает результат в {@link Optional#ofNullable(Object)}.
     */
    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(storage.get(username));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Генерирует новый ID с помощью {@link AtomicLong}, если
     * ID сохраняемого пользователя равен 0.
     * Записывает пользователя в Map по его имени ({@code getUsername()}).
     */
    @Override
    public User save(User user) {
        if (user.getId() == 0) {
            user.setId(idCounter.incrementAndGet());
        }
        storage.put(user.getUsername(), user);
        return user;
    }
}