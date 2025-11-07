import model.Product;
import model.Role;
import model.User;
import repository.AuditRepository;
import repository.InMemoryAuditRepository;
import repository.InMemoryProductRepository;
import repository.InMemoryUserRepository;
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

/**
 * Главный класс приложения (Точка входа).
 * <p>
 * Отвечает за:
 * 1. Создание всех зависимостей (ручное Внедрение Зависимостей - DI).
 * 2. Решение циклической зависимости (Auth <-> Audit) через Setter Injection.
 * 3. Первичное наполнение данными (bootstrap).
 * 4. Запуск консольного меню.
 */
public class Main {

    /**
     * Точка входа в приложение.
     *
     * @param args Аргументы командной строки (не используются).
     */
    public static void main(String[] args) {

        // --- 1. Слой Репозиториев (Хранилище) ---
        // Создаем конкретные реализации репозиториев
        ProductRepository productRepository = new InMemoryProductRepository();
        UserRepository userRepository = new InMemoryUserRepository();
        AuditRepository auditRepository = new InMemoryAuditRepository();

        // --- 2. Слой Сервисов (Бизнес-логика) ---
        // Создаем реализации сервисов, передавая им репозитории

        // Особый случай: циклическая зависимость AuthService <-> AuditService
        // Решаем ее через Setter Injection

        // 2a. Создаем AuditService (он пока не знает про AuthService)
        AuditServiceImpl auditService = new AuditServiceImpl(auditRepository);

        // 2b. Создаем AuthService, передавая ему AuditService
        AuthService authService = new AuthServiceImpl(userRepository, auditService);

        // 2c. Теперь "до-внедряем" AuthService в AuditService
        auditService.setAuthService(authService); // <--- Цикл замкнулся!

        // 2d. Создаем ProductService (он зависит от репозитория и аудита)
        ProductService productService = new ProductServiceImpl(
                productRepository, auditService);

        // --- 3. Слой UI (Представление) ---
        // Создаем вспомогательные классы UI
        UserInputHandler inputHandler = new UserInputHandler();
        ConsolePrinter consolePrinter = new ConsolePrinter();

        // --- 4. Заполнение данными (Bootstrap) ---
        // Добавим админа, чтобы можно было войти
        userRepository.save(new User(0, "admin", "admin123", Role.ADMIN));

        // Добавим несколько товаров для теста
        productService.addProduct(new Product("Ноутбук 'Pro'", "Электроника",
                "TechBrand", 150000.00, 10));
        productService.addProduct(new Product("Смартфон 'X100'", "Электроника",
                "MobileCorp", 80000.00, 25));
        productService.addProduct(new Product("Кофеварка 'Barista'", "Бытовая техника",
                "HomeGoods", 12000.00, 15));
        productService.addProduct(new Product("Книга 'Чистая Архитектура'", "Книги",
                "BookPress", 1500.00, 50));

        // --- 5. Запуск Приложения ---
        // Создаем главный класс меню, передавая ему ВСЕ сервисы и утилиты UI
        ConsoleMenu menu = new ConsoleMenu(
                productService,
                authService,
                auditService,
                inputHandler,
                consolePrinter
        );

        // Запускаем!
        menu.run();
    }
}
