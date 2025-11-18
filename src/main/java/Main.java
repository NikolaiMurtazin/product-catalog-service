import factory.RepositoryFactory;
import repository.AuditRepository;
import repository.ProductRepository;
import repository.UserRepository;
import service.AuditServiceImpl;
import service.AuthService;
import service.AuthServiceImpl;
import service.ProductService;
import service.ProductServiceImpl;
import ui.ConsoleMenu;
import ui.in.UserInputHandler;
import ui.out.ConsolePrinter;
import util.LiquibaseRunner;

/**
 * Главный класс приложения (Точка входа).
 * <p>
 * Отвечает за:
 * 1. Запуск миграций Liquibase.
 * 2. Создание всех зависимостей (ручное Внедрение Зависимостей - DI).
 * 3. Решение циклической зависимости (Auth <-> Audit) через Setter Injection.
 * 4. Запуск консольного меню.
 */
public class Main {

    /**
     * Сообщение об ошибке, если приложение не может
     * подключиться к БД или запустить миграции.
     */
    private static final String ERR_APP_INIT_FAILED = """
            Ошибка при инициализации приложения.
            Убедитесь, что БД запущена (docker-compose up -d)
            и config.properties настроен верно.
            """;

    /**
     * Точка входа в приложение.
     *
     * @param args Аргументы командной строки (не используются).
     */
    public static void main(String[] args) {

        try {
            LiquibaseRunner.runMigrations();

        } catch (Exception e) {
            System.err.println(ERR_APP_INIT_FAILED);
            e.printStackTrace();
            return;
        }

        ProductRepository productRepository = RepositoryFactory.getProductRepository();
        UserRepository userRepository = RepositoryFactory.getUserRepository();
        AuditRepository auditRepository = RepositoryFactory.getAuditRepository();

        AuditServiceImpl auditService = new AuditServiceImpl(auditRepository);
        AuthService authService = new AuthServiceImpl(userRepository, auditService);
        auditService.setAuthService(authService);

        ProductService productService = new ProductServiceImpl(
                productRepository, auditService);

        UserInputHandler inputHandler = new UserInputHandler();
        ConsolePrinter consolePrinter = new ConsolePrinter();

        ConsoleMenu menu = new ConsoleMenu(
                productService,
                authService,
                auditService,
                inputHandler,
                consolePrinter
        );

        menu.run();
    }
}