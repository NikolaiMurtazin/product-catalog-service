package service;

import java.util.List;

/**
 * Интерфейс сервиса для логирования (аудита) действий пользователя.
 */
public interface AuditService {

    /**
     * Записывает действие, выполненное текущим пользователем.
     * Если пользователь не аутентифицирован, действие записывается
     * от имени "SYSTEM".
     *
     * @param action Описание действия (например, "LOGIN", "ADD_PRODUCT")
     */
    void logAction(String action);

    /**
     * Возвращает все записи аудита.
     *
     * @return Список всех записей
     */
    List<String> getAuditHistory();
}
