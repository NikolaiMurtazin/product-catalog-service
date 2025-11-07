package model;

import java.util.Objects;

/**
 * Модель данных, представляющая пользователя системы.
 */
public class User {

    /** Уникальный идентификатор пользователя. */
    private long id;

    /** Имя пользователя (логин). */
    private String username;

    /** Хэш пароля пользователя (в ДЗ-1 используется как сам пароль). */
    private String passwordHash;

    /** Роль пользователя (ADMIN или USER). */
    private Role role;

    /**
     * Создает пустой экземпляр пользователя.
     */
    public User() {
    }

    /**
     * Создает экземпляр пользователя с заданными параметрами.
     *
     * @param id           Уникальный идентификатор
     * @param username     Имя пользователя (логин)
     * @param passwordHash Хэш пароля
     * @param role         Роль пользователя
     */
    public User(long id, String username, String passwordHash, Role role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    /**
     * @return Уникальный идентификатор пользователя.
     */
    public long getId() {
        return id;
    }

    /**
     * @param id Уникальный идентификатор пользователя.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return Имя пользователя (логин).
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username Имя пользователя (логин).
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return Хэш пароля пользователя.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * @param passwordHash Хэш пароля пользователя.
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * @return Роль пользователя.
     */
    public Role getRole() {
        return role;
    }

    /**
     * @param role Роль пользователя.
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Сравнивает этого пользователя с другим объектом.
     * Сравнение идет по id, username и role. Пароль не учитывается.
     *
     * @param o Объект для сравнения.
     * @return true, если объекты идентичны, иначе false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        // Пароль намеренно не участвует в equals
        return id == user.id && Objects.equals(username, user.username) && role == user.role;
    }

    /**
     * Вычисляет хэш-код для пользователя.
     * Пароль намеренно не участвует в хэш-коде.
     *
     * @return Хэш-код пользователя.
     */
    @Override
    public int hashCode() {
        // Пароль намеренно не участвует в hashCode
        return Objects.hash(id, username, role);
    }

    /**
     * Возвращает строковое представление пользователя (без пароля).
     *
     * @return Строковое представление пользователя.
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role +
                '}';
    }
}
