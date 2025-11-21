package repository;

import exception.ProductRepositoryException;
import lombok.NonNull;
import model.Product;
import service.dto.SearchCriteria;
import util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-реализация репозитория товаров.
 * Работает с 'app_schema.products'.
 */
public class JdbcProductRepository implements ProductRepository {

    /**
     * Базовый SQL-запрос для выбора всех полей товара.
     */
    private static final String SELECT_BASE_SQL = """
            SELECT id, name, category, brand, price, stock
            FROM app_schema.products
            """;

    /**
     * SQL-запрос для поиска товара по ID.
     */
    private static final String FIND_BY_ID_SQL = SELECT_BASE_SQL + " WHERE id = ?";

    /**
     * SQL-запрос для поиска всех товаров с сортировкой по имени.
     */
    private static final String FIND_ALL_SQL = SELECT_BASE_SQL + " ORDER BY name";

    /**
     * SQL-запрос для удаления товара по ID.
     */
    private static final String DELETE_BY_ID_SQL = """
            DELETE FROM app_schema.products
            WHERE id = ?
            """;

    /**
     * SQL-запрос для вставки нового товара (ID генерируется sequence'ом).
     */
    private static final String INSERT_SQL = """
            INSERT INTO app_schema.products (name, category, brand, price, stock)
            VALUES (?, ?, ?, ?, ?)
            """;

    /**
     * SQL-запрос для полного обновления существующего товара по ID.
     */
    private static final String UPDATE_SQL = """
            UPDATE app_schema.products
            SET name = ?, category = ?, brand = ?, price = ?, stock = ?
            WHERE id = ?
            """;

    /**
     * Внутренний record для хранения "пары" из
     * динамически построенного SQL и списка его параметров.
     * (Используется для декомпозиции метода search).
     */
    private record DynamicQuery(String sql, List<Object> params) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * Делегирует вызов {@link #insert(Product)} (если {@code product.getId() == null})
     * или {@link #update(Product)} (если {@code product.getId() != null}).
     */
    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            return insert(product);
        } else {
            return update(product);
        }
    }

    /**
     * Приватный метод для вставки нового товара в БД.
     *
     * @param product Товар для вставки (с id = null).
     * @return Тот же объект товара, но с установленным ID,
     * сгенерированным базой данных.
     * @throws ProductRepositoryException Оборачивает {@link SQLException} при ошибке.
     */
    private Product insert(@NonNull Product product) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getCategory());
            statement.setString(3, product.getBrand());
            statement.setDouble(4, product.getPrice());
            statement.setInt(5, product.getStock());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Создание товара не удалось, ID не получен.");
                }
            }
        } catch (SQLException e) {
            throw new ProductRepositoryException("Ошибка при вставке (insert) товара", e);
        }
        return product;
    }

    /**
     * Приватный метод для обновления существующего товара в БД.
     *
     * @param product Товар для обновления (с id > 0).
     * @return Тот же объект товара.
     * @throws ProductRepositoryException Оборачивает {@link SQLException} при ошибке.
     */
    private Product update(@NonNull Product product) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getCategory());
            statement.setString(3, product.getBrand());
            statement.setDouble(4, product.getPrice());
            statement.setInt(5, product.getStock());
            statement.setLong(6, product.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new ProductRepositoryException("Ошибка при обновлении (update) товара", e);
        }
        return product;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Выполняет SQL-запрос {@code SELECT ... WHERE id = ?}.
     * Соединение и ResultSet закрываются через {@code try-with-resources}.
     */
    @Override
    public Optional<Product> findById(long id) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new ProductRepositoryException("Ошибка при поиске товара по ID", e);
        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Выполняет SQL-запрос {@code SELECT ... ORDER BY name}.
     * Соединение и ResultSet закрываются через {@code try-with-resources}.
     */
    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        } catch (SQLException e) {
            throw new ProductRepositoryException("Ошибка при поиске всех товаров", e);
        }
        return products;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Выполняет SQL-запрос {@code DELETE ... WHERE id = ?}.
     * Соединение закрывается через {@code try-with-resources}.
     */
    @Override
    public void deleteById(long id) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID_SQL)) {

            statement.setLong(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new ProductRepositoryException("Ошибка при удалении товара", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Делегирует сборку запроса {@link #buildSearchQuery(SearchCriteria)}
     * и выполнение {@link #executeSearchQuery(DynamicQuery)}.
     */
    @Override
    public List<Product> search(@NonNull SearchCriteria criteria) {
        DynamicQuery dynamicQuery = buildSearchQuery(criteria);
        return executeSearchQuery(dynamicQuery);
    }

    /**
     * Приватный метод: "Строитель" SQL-запроса.
     * <p>
     * Собирает SQL и список параметров на основе фильтров.
     *
     * @param criteria DTO с фильтрами.
     * @return {@link DynamicQuery}, содержащий SQL-строку и список параметров.
     */
    private DynamicQuery buildSearchQuery(@NonNull SearchCriteria criteria) {
        StringBuilder sql = new StringBuilder(SELECT_BASE_SQL);
        List<Object> params = new ArrayList<>();

        sql.append(" WHERE 1=1");

        if (criteria.category() != null) {
            sql.append(" AND category ILIKE ?");
            params.add(criteria.category());
        }
        if (criteria.brand() != null) {
            sql.append(" AND brand ILIKE ?");
            params.add(criteria.brand());
        }
        if (criteria.minPrice() != null) {
            sql.append(" AND price >= ?");
            params.add(criteria.minPrice());
        }
        if (criteria.maxPrice() != null) {
            sql.append(" AND price <= ?");
            params.add(criteria.maxPrice());
        }

        sql.append(" ORDER BY name");

        return new DynamicQuery(sql.toString(), params);
    }

    /**
     * Приватный метод: "Исполнитель" запроса.
     * <p>
     * Открывает соединение, выполняет запрос и маппит результат.
     *
     * @param query {@link DynamicQuery} с готовым SQL и параметрами.
     * @return Список найденных {@link Product}.
     * @throws ProductRepositoryException если происходит {@link SQLException}.
     */
    private List<Product> executeSearchQuery(@NonNull DynamicQuery query) {
        List<Product> products = new ArrayList<>();

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query.sql())) {

            setParameters(statement, query.params());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new ProductRepositoryException("Ошибка при поиске (search) товаров", e);
        }

        return products;
    }

    /**
     * Вспомогательный метод для заполнения PreparedStatement.
     *
     * @param statement Готовый PreparedStatement
     * @param params    Список параметров для вставки
     * @throws SQLException
     */
    private void setParameters(@NonNull PreparedStatement statement, @NonNull List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
    }

    /**
     * Вспомогательный "маппер" (DRY-принцип).
     *
     * @param rs {@link ResultSet}, установленный на текущую строку.
     * @return Заполненный объект {@link Product}.
     * @throws SQLException если имя колонки не найдено или тип не совпадает.
     */
    private Product mapRowToProduct(@NonNull ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));
        product.setCategory(rs.getString("category"));
        product.setBrand(rs.getString("brand"));
        product.setPrice(rs.getDouble("price"));
        product.setStock(rs.getInt("stock"));
        return product;
    }
}