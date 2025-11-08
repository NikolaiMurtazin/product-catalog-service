package repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory реализация репозитория аудита.
 * Хранит записи в потокобезопасном списке.
 */
public class InMemoryAuditRepository implements AuditRepository {

    /**
     * Потокобезопасное хранилище для записей аудита.
     * Использует synchronizedList для обеспечения безопасности при
     * одновременном доступе.
     */
    private static final List<String> auditLog =
            Collections.synchronizedList(new ArrayList<>());

    /**
     * {@inheritDoc}
     *
     * Добавляет новую запись о событии в потокобезопасный список.
     */
    @Override
    public void save(String event) {
        auditLog.add(event);
    }

    /**
     * {@inheritDoc}
     *
     * Возвращает новую копию списка всех записей аудита,
     * чтобы предотвратить изменение оригинального списка извне.
     */
    @Override
    public List<String> findAll() {
        return new ArrayList<>(auditLog);
    }
}