package web.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Утилитарный класс для сериализации и десериализации JSON.
 * <p>
 * Является оберткой над библиотекой Jackson ({@link ObjectMapper}).
 * Упрощает чтение DTO из HTTP-запросов и запись ответов в формате JSON.
 */
public class JsonHelper {

    /**
     * Единственный экземпляр ObjectMapper (потокобезопасный).
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Статический блок инициализации.
     * <p>
     * Настраивает ObjectMapper:
     * 1. Регистрирует {@link JavaTimeModule} для поддержки типов Java 8 Date/Time (LocalDateTime и др.).
     * 2. Отключает запись дат как таймштампов (чисел), используя формат ISO-8601 (строки).
     */
    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Читает JSON из тела запроса и преобразует его в Java-объект.
     *
     * @param req   HTTP-запрос, содержащий JSON в теле.
     * @param clazz Класс целевого объекта (DTO).
     * @param <T>   Тип целевого объекта.
     * @return Десериализованный объект.
     * @throws RuntimeException Оборачивает {@link IOException}, если не удалось прочитать или распарсить JSON.
     */
    public static <T> T read(HttpServletRequest req, Class<T> clazz) {
        try {
            return mapper.readValue(req.getInputStream(), clazz);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения JSON из запроса", e);
        }
    }

    /**
     * Преобразует Java-объект в JSON и записывает его в тело ответа.
     * <p>
     * Также автоматически устанавливает заголовки:
     * <ul>
     * <li>Content-Type: application/json</li>
     * <li>Character-Encoding: UTF-8</li>
     * </ul>
     *
     * @param resp   HTTP-ответ.
     * @param object Объект для сериализации.
     * @throws RuntimeException Оборачивает {@link IOException}, если не удалось записать данные в поток вывода.
     */
    public static void write(HttpServletResponse resp, Object object) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try {
            mapper.writeValue(resp.getOutputStream(), object);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка записи JSON в ответ", e);
        }
    }
}