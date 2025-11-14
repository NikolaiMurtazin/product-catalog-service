import repository.AuditRepository;
import repository.JdbcAuditRepository;
import repository.JdbcProductRepository;
import repository.JdbcUserRepository;
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
     * Точка входа в приложение.
     *
     * @param args Аргументы командной строки (не используются).
     */
    public static void main(String[] args) {

        try {
            LiquibaseRunner.runMigrations();

        } catch (Exception e) {
            System.err.println("Ошибка при инициализации приложения. " +
                    "Убедитесь, что БД запущена (docker-compose up -d) " +
                    "и config.properties настроен верно.");
            e.printStackTrace();
            return;
        }

        ProductRepository productRepository = new JdbcProductRepository();
        UserRepository userRepository = new JdbcUserRepository();
        AuditRepository auditRepository = new JdbcAuditRepository();

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