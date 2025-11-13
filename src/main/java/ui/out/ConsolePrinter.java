package ui.out;

import model.Product;
import model.User;

import java.util.List;

/**
 * Вспомогательный класс для вывода информации в консоль.
 * Стандартизирует отображение данных.
 */
public class ConsolePrinter {

    /**
     * Печатает в консоль стандартизированный заголовок.
     *
     * @param title Текст заголовка (будет приведен к верхнему регистру)
     */
    public void printHeader(String title) {
        System.out.printf("""
                
                ========================================
                  %s
                ========================================
                %n""", title.toUpperCase());
    }

    /**
     * Печатает в консоль стандартное информационное сообщение.
     *
     * @param message Текст сообщения
     */
    public void printMessage(String message) {
        System.out.println("> " + message);
    }

    /**
     * Печатает в консоль стандартизированное сообщение об ошибке.
     *
     * @param error Текст ошибки
     */
    public void printError(String error) {
        System.out.println("! ОШИБКА: " + error);
    }

    /**
     * Печатает в консоль сообщение об успешном входе пользователя.
     *
     * @param user Пользователь, вошедший в систему
     */
    public void printUser(User user) {
        printMessage("Вход выполнен: " + user.getUsername() + " (Роль: " + user.getRole() + ")");
    }

    /**
     * Выводит список продуктов или сообщение, если список пуст.
     *
     * @param products Список продуктов
     */
    public void printProducts(List<Product> products) {
        if (products.isEmpty()) {
            printMessage("Товары не найдены.");
            return;
        }

        for (Product product : products) {
            System.out.println("--------------------");
            System.out.println(product);
        }
        System.out.println("--------------------");
    }

    /**
     * Выводит список записей аудита.
     *
     * @param history Список записей
     */
    public void printAuditHistory(List<String> history) {
        if (history.isEmpty()) {
            printMessage("История аудита пуста.");
            return;
        }

        printHeader("История Аудита");
        for (String event : history) {
            System.out.println("- " + event);
        }
    }
}