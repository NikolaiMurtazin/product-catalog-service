package web.util;

import lombok.Getter;
import lombok.Setter;
import service.AuditService;
import service.AuthService;
import service.ProductService;

/**
 * Статический реестр (Registry) для хранения ссылок на экземпляры сервисов.
 * <p>
 * <b>Назначение:</b>
 * Этот класс необходим для обеспечения доступа к бизнес-логике из компонентов,
 * которые создаются вне нашего контроля и не поддерживают ручное внедрение зависимостей.
 * В первую очередь это касается <b>Аспектов (AspectJ)</b>.
 * <p>
 * Аспекты инстанцируются виртуальной машиной или ткачом (Weaver) и не могут
 * получить сервисы через конструктор. Поэтому они обращаются к этому статическому реестру.
 * <p>
 * Инициализация полей происходит в {@code AppContextListener} при старте приложения.
 */
public class ServiceRegistry {

    /**
     * Глобальный экземпляр {@link ProductService}.
     * -- GETTER --
     * Возвращает сохраненный экземпляр {@link ProductService}.
     * -- SETTER --
     * Сохраняет экземпляр {@link ProductService}.
     */
    @Getter
    @Setter
    private static ProductService productService;

    /**
     * Глобальный экземпляр {@link AuthService}.
     * -- GETTER --
     * Возвращает сохраненный экземпляр {@link AuthService}.
     * -- SETTER --
     * Сохраняет экземпляр {@link AuthService}.
     */
    @Getter
    @Setter
    private static AuthService authService;

    /**
     * Глобальный экземпляр {@link AuditService}.
     * -- GETTER --
     * Возвращает сохраненный экземпляр {@link AuditService}.
     * -- SETTER --
     * Сохраняет экземпляр {@link AuditService}.
     */
    @Getter
    @Setter
    private static AuditService auditService;
}
