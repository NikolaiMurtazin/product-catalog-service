package web.servlet;

import dto.ProductDto;
import exception.ProductRepositoryException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mapper.ProductMapper;
import model.Product;
import service.ProductService;
import service.dto.SearchCriteria;
import web.util.JsonHelper;
import web.util.ValidationUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Сервлет для управления товарами (REST Controller).
 * <p>
 * Обрабатывает HTTP-запросы по путям {@code /products} и {@code /products/*}.
 * Поддерживает операции CRUD:
 * <ul>
 * <li>GET - Получение списка (с фильтрацией) или одного товара.</li>
 * <li>POST - Создание нового товара.</li>
 * <li>PUT - Обновление существующего товара.</li>
 * <li>DELETE - Удаление товара.</li>
 * </ul>
 * Обмен данными происходит в формате JSON.
 */
@WebServlet("/products/*")
public class ProductServlet extends HttpServlet {

    /**
     * Сервис бизнес-логики для работы с товарами.
     */
    private ProductService productService;

    /**
     * Маппер для преобразования между сущностями и DTO.
     */
    private final ProductMapper productMapper = ProductMapper.INSTANCE;

    /**
     * Инициализация сервлета.
     * <p>
     * Извлекает {@link ProductService} из атрибутов контекста сервлета,
     * куда он был помещен при старте приложения (в {@code AppContextListener}).
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.productService = (ProductService) config.getServletContext().getAttribute("productService");
    }

    /**
     * Обрабатывает GET-запросы.
     * <p>
     * Сценарии:
     * 1. {@code /products/{id}} - Возвращает товар по ID. Если не найден - 404.
     * 2. {@code /products} - Возвращает список всех товаров.
     * 3. {@code /products?param=...} - Возвращает список товаров, отфильтрованный по параметрам
     * (category, brand, minPrice, maxPrice).
     *
     * @param req  Объект запроса.
     * @param resp Объект ответа.
     * @throws IOException В случае ошибки ввода-вывода.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && !pathInfo.equals("/")) {
                long id = extractId(pathInfo);
                Optional<Product> productOpt = productService.getProductById(id);

                if (productOpt.isPresent()) {
                    ProductDto dto = productMapper.toDto(productOpt.get());
                    JsonHelper.write(resp, dto);
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Товар не найден");
                }
                return;
            }

            String category = req.getParameter("category");
            String brand = req.getParameter("brand");
            Double minPrice = parseDouble(req.getParameter("minPrice"));
            Double maxPrice = parseDouble(req.getParameter("maxPrice"));

            if (category != null || brand != null || minPrice != null || maxPrice != null) {
                SearchCriteria criteria = new SearchCriteria(category, brand, minPrice, maxPrice);
                List<Product> products = productService.searchProducts(criteria);
                List<ProductDto> dtos = productMapper.toDtoList(products);
                JsonHelper.write(resp, dtos);
            } else {
                List<Product> products = productService.getAllProducts();
                List<ProductDto> dtos = productMapper.toDtoList(products);
                JsonHelper.write(resp, dtos);
            }

        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    /**
     * Обрабатывает POST-запросы для создания нового товара.
     * <p>
     * Ожидает JSON-объект {@link ProductDto} в теле запроса.
     * Выполняет валидацию данных.
     * В случае успеха возвращает созданный товар и статус 201 Created.
     *
     * @param req  Объект запроса.
     * @param resp Объект ответа.
     * @throws IOException В случае ошибки ввода-вывода.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ProductDto dto = JsonHelper.read(req, ProductDto.class);

            ValidationUtils.validate(dto);

            Product product = productMapper.toEntity(dto);
            Product savedProduct = productService.addProduct(product);

            ProductDto resultDto = productMapper.toDto(savedProduct);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            JsonHelper.write(resp, resultDto);

        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    /**
     * Обрабатывает PUT-запросы для обновления товара.
     * <p>
     * Ожидает ID товара в пути ({@code /products/{id}}) и JSON с новыми данными в теле.
     * Если товар не найден - возвращает 404.
     *
     * @param req  Объект запроса.
     * @param resp Объект ответа.
     * @throws IOException В случае ошибки ввода-вывода.
     */
    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID товара обязателен для обновления");
            return;
        }

        try {
            long id = extractId(pathInfo);

            if (productService.getProductById(id).isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Товар не найден");
                return;
            }

            ProductDto dto = JsonHelper.read(req, ProductDto.class);
            ValidationUtils.validate(dto);

            Product productToUpdate = productMapper.toEntity(dto);
            productToUpdate.setId(id);

            Product updatedProduct = productService.updateProduct(productToUpdate);

            JsonHelper.write(resp, productMapper.toDto(updatedProduct));

        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    /**
     * Обрабатывает DELETE-запросы для удаления товара.
     * <p>
     * Ожидает ID товара в пути ({@code /products/{id}}).
     * В случае успеха возвращает 204 No Content.
     *
     * @param req  Объект запроса.
     * @param resp Объект ответа.
     * @throws IOException В случае ошибки ввода-вывода.
     */
    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID товара обязателен для удаления");
            return;
        }

        try {
            long id = extractId(pathInfo);

            if (productService.getProductById(id).isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Товар не найден");
                return;
            }

            productService.deleteProduct(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    /**
     * Извлекает числовой ID из пути URL.
     *
     * @param pathInfo Строка пути (например, "/123").
     * @return Извлеченный ID.
     * @throws IllegalArgumentException если формат ID неверен.
     */
    private long extractId(String pathInfo) {
        try {
            return Long.parseLong(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректный ID товара");
        }
    }

    /**
     * Безопасно парсит строковый параметр в Double.
     *
     * @param param Строковое значение параметра.
     * @return Double или null, если параметр пуст.
     * @throws IllegalArgumentException если формат числа неверен.
     */
    private Double parseDouble(String param) {
        if (param == null || param.isBlank()) return null;
        try {
            return Double.parseDouble(param);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректный формат числа: " + param);
        }
    }

    /**
     * Централизованная обработка исключений сервлета.
     * Устанавливает соответствующий HTTP статус-код и сообщение об ошибке.
     *
     * @param resp Объект ответа.
     * @param e    Исключение.
     * @throws IOException В случае ошибки отправки ответа.
     */
    private void handleException(HttpServletResponse resp, Exception e) throws IOException {
        e.printStackTrace();
        if (e instanceof IllegalArgumentException) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } else if (e instanceof ProductRepositoryException) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных");
        } else {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }
}