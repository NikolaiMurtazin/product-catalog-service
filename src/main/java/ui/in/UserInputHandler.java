package ui.in;

import java.util.Scanner;

/**
 * Вспомогательный класс для обработки ввода пользователя из консоли.
 * Инкапсулирует работу со Scanner и обработку ошибок ввода.
 */
public class UserInputHandler {

    /**
     * Единственный экземпляр Scanner для всего приложения.
     * <p>
     * Scanner лучше создавать один раз (как static final),
     * так как {@code System.in} — это единый поток,
     * и создание нескольких Scanner'ов поверх него может
     * привести к непредсказуемому поведению.
     */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Читает строку от пользователя.
     *
     * @param prompt Сообщение для пользователя (например, "Введите имя:")
     * @return Введенная строка
     */
    public String readString(String prompt) {
        System.out.print(prompt + " ");
        return scanner.nextLine();
    }

    /**
     * Читает целое число от пользователя с обработкой ошибок.
     * <p>
     * Повторяет запрос, пока не будет введено корректное целое число.
     *
     * @param prompt Сообщение для пользователя
     * @return Введенное число
     */
    public int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt + " ");
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("! Ошибка: Пожалуйста, введите целое число.");
            }
        }
    }

    /**
     * Читает число с плавающей точкой (double) от пользователя.
     * <p>
     * Повторяет запрос, пока не будет введено корректное число.
     *
     * @param prompt Сообщение для пользователя
     * @return Введенное число
     */
    public double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt + " ");
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("! Ошибка: Пожалуйста, введите число (например, 199.99).");
            }
        }
    }

    /**
     * Читает опциональную строку (может быть пустой).
     * <p>
     * Если пользователь просто нажимает Enter, возвращает {@code null}.
     *
     * @param prompt Сообщение для пользователя
     * @return Введенная строка или {@code null}, если строка пустая
     */
    public String readOptionalString(String prompt) {
        System.out.print(prompt + " (нажмите Enter, чтобы пропустить): ");
        String input = scanner.nextLine();
        return input.isEmpty() ? null : input;
    }

    /**
     * Читает опциональный Double.
     * <p>
     * Если пользователь просто нажимает Enter, возвращает {@code null}.
     * Повторяет запрос при неверном формате числа.
     *
     * @param prompt Сообщение для пользователя
     * @return Введенное число или {@code null}, если строка пустая
     */
    public Double readOptionalDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt + " (нажмите Enter, чтобы пропустить): ");
                String input = scanner.nextLine();
                return input.isEmpty() ? null : Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("! Ошибка: Пожалуйста, введите число (например, 199.99).");
            }
        }
    }

    /**
     * Ожидает нажатия Enter от пользователя.
     * Используется для создания паузы в консольном меню.
     */
    public void waitForEnter() {
        System.out.println("\n...Нажмите Enter, чтобы продолжить...");
        scanner.nextLine();
    }
}
