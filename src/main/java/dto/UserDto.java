package dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.Role;

/**
 * DTO (Data Transfer Object) для представления пользователя.
 * <p>
 * Используется для передачи данных о пользователе при регистрации,
 * аутентификации и получении информации о пользователях.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /**
     * Уникальный идентификатор пользователя.
     * Может быть {@code null} при регистрации нового пользователя.
     */
    private Long id;

    /**
     * Имя пользователя (логин). Обязательное поле.
     * Должно быть уникальным в системе.
     */
    @NotBlank(message = "Username не может быть пустым")
    private String username;

    /**
     * Пароль пользователя.
     * <p>
     * Передается от клиента к серверу (при регистрации/входе).
     * При передаче от сервера к клиенту это поле должно быть пустым или скрытым
     */
    private String password;

    /**
     * Роль пользователя в системе (ADMIN, USER).
     * Определяет уровень доступа к API.
     */
    private Role role;
}