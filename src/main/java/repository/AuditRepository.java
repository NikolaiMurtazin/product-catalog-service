package repository;

import java.util.List;

/**
 * Интерфейс репозитория для хранения записей аудита.
 */
public interface AuditRepository {

    /**
     * Добавляет новую запись о событии в лог аудита.
     *
     * @param event Описание события (например, "USER_LOGIN: admin")
     */
    void save(String event);

    /**
     * Возвращает все сохраненные записи аудита.
     *
     * @return Список всех событий
     */
    List<String> findAll();
}