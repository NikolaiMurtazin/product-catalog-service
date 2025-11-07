package ui;

import model.Product;
import service.AuditService;
import service.AuthService;
import service.ProductService;
import ui.in.UserInputHandler;
import ui.out.ConsolePrinter;

import java.util.List;
import java.util.Optional;

/**
 * Главный класс, отвечающий за взаимодействие с пользователем через консоль.
 * Управляет главным циклом (REPL), отображает меню и вызывает
 * бизнес-сервисы.
 */
public class ConsoleMenu {

    /** Сервис для управления товарами. */
    private final ProductService productService;

    /** Сервис для аутентификации пользователей. */
    private final AuthService authService;

    /** Сервис для логирования действий. */
    private final AuditService auditService;

    /** Вспомогательный класс для чтения ввода из консоли. */
    private final UserInputHandler input;

    /** Вспомогательный класс для форматированного вывода в консоль. */
    private final ConsolePrinter output;

    /**
     * Создает экземпляр консольного меню (Внедрение Зависимостей).
     *
     * @param productService Сервис управления товарами
     * @param authService    Сервис аутентификации
     * @param auditService   Сервис аудита
     * @param input          Обработчик ввода
     * @param output         Обработчик вывода
     */
    public ConsoleMenu(ProductService productService, AuthService authService,
                       AuditService auditService, UserInputHandler input,
                       ConsolePrinter output) {
        this.productService = productService;
        this.authService = authService;
        this.auditService = auditService;
        this.input = input;
        this.output = output;
    }

    /**
     * Главный метод, запускающий приложение.
     * <p>
     * Сначала запускает цикл авторизации, затем -
     * главный цикл меню.
     */
    public void run() {
        output.printHeader("Каталог Товаров Маркетплейса");

        // 1. Цикл авторизации
        while (authService.getCurrentUser().isEmpty()) {
            runLoginMenu();
        }

        // 2. Основной цикл приложения
        runMainMenu();
    }

    /**
     * Приватный метод, отвечающий за цикл меню авторизации.
     * Повторяется, пока {@code authService.getCurrentUser()} не вернет
     * пользователя.
     */
    private void runLoginMenu() {
        output.printHeader("Авторизация");
        String username = input.readString("Введите логин:");
        String password = input.readString("Введите пароль:");

        authService.login(username, password)
                .ifPresentOrElse(
                        output::printUser, // Успех
                        () -> output.printError("Неверный логин или пароль.") // Провал
                );
    }

    /**
     * Приватный метод, отвечающий за главный цикл приложения
     * (после успешной авторизации).
     */
    private void runMainMenu() {
        boolean running = true;
        while (running) {
            output.printHeader("Главное Меню");
            System.out.println("1. Показать все товары");
            System.out.println("2. Найти товар по ID");
            System.out.println("3. Поиск/фильтрация товаров");

            // Админские функции
            if (authService.isAdmin()) {
                System.out.println("--- Администрирование ---");
                System.out.println("4. Добавить новый товар");
                System.out.println("5. Изменить товар");
                System.out.println("6. Удалить товар");
                System.out.println("7. Показать лог аудита");
            }

            System.out.println("---------------------------");
            System.out.println("0. Выход");

            int choice = input.readInt("Выберите опцию:");

            switch (choice) {
                case 1: viewAllProducts(); break;
                case 2: viewProductById(); break;
                case 3: searchProducts(); break;

                case 4: if (authService.isAdmin()) addProduct(); break;
                case 5: if (authService.isAdmin()) updateProduct(); break;
                case 6: if (authService.isAdmin()) deleteProduct(); break;
                case 7: if (authService.isAdmin()) viewAuditLog(); break;

                case 0:
                    running = false;
                    authService.logout();
                    output.printMessage("Выход из системы. До свидания!");
                    break;
                default:
                    output.printError("Неизвестная опция. Попробуйте снова.");
            }
        }
    }

    // --- Реализация опций меню ---

    /**
     * (Пункт 1) Получает и отображает список всех товаров.
     */
    private void viewAllProducts() {
        output.printHeader("Все товары");
        List<Product> products = productService.getAllProducts();
        output.printProducts(products);
        input.waitForEnter();
    }

    /**
     * (Пункт 2) Запрашивает ID и отображает информацию о товаре.
     */
    private void viewProductById() {
        output.printHeader("Поиск по ID");
        long id = input.readInt("Введите ID товара:");

        productService.getProductById(id)
                .ifPresentOrElse(
                        product -> {
                            output.printHeader("Найденный товар");
                            System.out.println(product);
                        },
                        () -> output.printError("Товар с ID=" + id + " не найден.")
                );
        input.waitForEnter();
    }

    /**
     * (Пункт 3) Запрашивает критерии фильтрации и отображает результат.
     * <p>
     * Также демонстрирует работу кэша (Бонус),
     * выполняя запрос дважды и замеряя время.
     */
    private void searchProducts() {
        output.printHeader("Поиск и фильтрация");
        output.printMessage("Запустим поиск по кэшируемым данным.");

        // Считываем опциональные параметры
        String category = input.readOptionalString("Категория:");
        String brand = input.readOptionalString("Бренд:");
        Double minPrice = input.readOptionalDouble("Мин. цена:");
        Double maxPrice = input.readOptionalDouble("Макс. цена:");

        // Выполняем поиск (он использует кэш)
        List<Product> products = productService.searchProducts(
                category, brand, minPrice, maxPrice);

        output.printProducts(products);

        // Демонстрация кэша:
        output.printMessage("\n--- Демонстрация Кэша ---");
        output.printMessage("Запускаем ТОЧНО ТАКОЙ ЖЕ поиск еще раз...");

        // Вызываем сервис еще раз. Теперь он должен взять из кэша.
        products = productService.searchProducts(category, brand, minPrice, maxPrice);

        output.printProducts(products);
        output.printMessage("--- Конец демонстрации кэша ---");

        input.waitForEnter();
    }

    /**
     * (Пункт 4) (Админ) Запрашивает данные о новом товаре и
     * вызывает сервис для его добавления.
     */
    private void addProduct() {
        output.printHeader("Добавление товара");
        String name = input.readString("Название:");
        String category = input.readString("Категория:");
        String brand = input.readString("Бренд:");
        double price = input.readDouble("Цена:");
        int stock = input.readInt("Кол-во на складе:");

        Product newProduct = new Product(name, category, brand, price, stock);
        Product savedProduct = productService.addProduct(newProduct);

        output.printMessage("Товар успешно добавлен с ID=" + savedProduct.getId());
        input.waitForEnter();
    }

    /**
     * (Пункт 5) (Админ) Запрашивает ID товара и новые данные
     * для его обновления.
     */
    private void updateProduct() {
        output.printHeader("Изменение товара");
        long id = input.readInt("Введите ID товара для изменения:");

        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            output.printError("Товар с ID=" + id + " не найден.");
            input.waitForEnter();
            return;
        }

        Product product = productOpt.get();
        output.printMessage("Найден товар: " + product.getName());

        // Считываем новые данные
        String name = input.readString("Новое название:");
        String category = input.readString("Новая категория:");
        String brand = input.readString("Новый бренд:");
        double price = input.readDouble("Новая цена:");
        int stock = input.readInt("Новое кол-во на складе:");

        // Обновляем объект
        product.setName(name);
        product.setCategory(category);
        product.setBrand(brand);
        product.setPrice(price);
        product.setStock(stock);

        productService.updateProduct(product);
        output.printMessage("Товар успешно обновлен.");
        input.waitForEnter();
    }

    /**
     * (Пункт 6) (Админ) Запрашивает ID товара
     * для его удаления.
     */
    private void deleteProduct() {
        output.printHeader("Удаление товара");
        long id = input.readInt("Введите ID товара для удаления:");

        // Проверяем, существует ли товар, прежде чем удалять
        if (productService.getProductById(id).isPresent()) {
            productService.deleteProduct(id);
            output.printMessage("Товар с ID=" + id + " успешно удален.");
        } else {
            output.printError("Товар с ID=" + id + " не найден.");
        }
        input.waitForEnter();
    }

    /**
     * (Пункт 7) (Админ) Отображает историю
     * из сервиса аудита.
     */
    private void viewAuditLog() {
        List<String> history = auditService.getAuditHistory();
        output.printAuditHistory(history);
        input.waitForEnter();
    }
}
