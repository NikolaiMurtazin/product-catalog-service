package model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Модель данных, представляющая пользователя системы.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "passwordHash")
@EqualsAndHashCode(exclude = "passwordHash")
public class User {

    /** Уникальный идентификатор пользователя. */
    private long id;

    /** Имя пользователя (логин). */
    private String username;

    /** Хэш пароля пользователя */
    private String passwordHash;

    /** Роль пользователя (ADMIN или USER). */
    private Role role;
}