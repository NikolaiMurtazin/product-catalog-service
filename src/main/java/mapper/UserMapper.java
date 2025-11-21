package mapper;

import dto.UserDto;
import model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Маппер для преобразования между сущностью {@link User} и DTO {@link UserDto}.
 * <p>
 * Выполняет конвертацию данных пользователя, учитывая различие в именовании
 * полей пароля ({@code password} в DTO и {@code passwordHash} в Entity).
 */
@Mapper
public interface UserMapper {

    /**
     * Единственный экземпляр (Singleton) маппера, создаваемый MapStruct.
     */
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    /**
     * Преобразует сущность пользователя в DTO.
     * <p>
     * Обрати внимание: так как имена полей пароля не совпадают
     * ({@code passwordHash} vs {@code password}), и мы не указали @Mapping,
     * поле {@code password} в DTO останется {@code null}.
     * Это <b>желаемое поведение</b>, чтобы не отправлять хэш пароля клиенту.
     *
     * @param user Сущность пользователя.
     * @return DTO пользователя (без хэша пароля).
     */
    UserDto toDto(User user);

    /**
     * Преобразует DTO пользователя в сущность.
     * <p>
     * Используется при регистрации или создании пользователя.
     * Явно маппит входящий {@code password} в поле {@code passwordHash} сущности.
     *
     * @param userDto DTO пользователя.
     * @return Сущность пользователя.
     */
    @Mapping(source = "password", target = "passwordHash")
    User toEntity(UserDto userDto);
}
