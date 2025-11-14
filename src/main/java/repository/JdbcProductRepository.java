package repository;

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

    /** Базовый SQL-запрос для выбора всех полей товара. */
    private static final String SELECT_BASE_SQL = """
            SELECT id, name, category, brand, price, stock
            FROM app_schema.products
            """;

    /** SQL-запрос для поиска товара по ID. */
    private static final String FIND_BY_ID_SQL = SELECT_BASE_SQL + " WHERE id = ?";

    /** SQL-запрос для поиска всех товаров с сортировкой по имени. */
    private static final String FIND_ALL_SQL = SELECT_BASE_SQL + " ORDER BY name";

    /** SQL-запрос для удаления товара по ID. */
    private static final String DELETE_BY_ID_SQL = """
            DELETE FROM app_schema.products
            WHERE id = ?
            """;

    /** SQL-запрос для вставки нового товара (ID генерируется sequence'ом). */
    private static final String INSERT_SQL = """
            INSERT INTO app_schema.products (name, category, brand, price, stock)
            VALUES (?, ?, ?, ?, ?)
            """;

    /** SQL-запрос для полного обновления существующего товара по ID. */
    private static final String UPDATE_SQL = """
            UPDATE app_schema.products
            SET name = ?, category = ?, brand = ?, price = ?, stock = ?
            WHERE id = ?
            """;

    /**
     * {@inheritDoc}
     * <p>
     * Делегирует вызов {@link #insert(Product)} (если {@code product.getId() == 0})
     * или {@link #update(Product)} (если {@code product.getId() > 0}).
     */
    @Override
    public Product save(Product product) {
        if (product.getId() == 0) {
            return insert(product);
        } else {
            return update(product);
        }
    }

    /**
     * Приватный метод для вставки нового товара в БД.
     *
     * @param product Товар для вставки (с id = 0).
     * @return Тот же объект товара, но с установленным ID,
     * сгенерированным базой данных.
     * @throws RuntimeException Оборачивает {@link SQLException} при ошибке.
     */
    private Product insert(Product product) {
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
            throw new RuntimeException("Ошибка при вставке (insert) товара", e);
        }
        return product;
    }

    /**
     * Приватный метод для обновления существующего товара в БД.
     *
     * @param product Товар для обновления (с id > 0).
     * @return Тот же объект товара.
     * @throws RuntimeException Оборачивает {@link SQLException} при ошибке.
     */
    private Product update(Product product) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getCategory());
            statement.setString(3, product.getBrand());
            statement.setDouble(4, product.getPrice());
            statement.setInt(5, product.getStock());
            statement.setLong(6, product.getId()); // id идет в 'WHERE id = ?'

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении (update) товара", e);
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
            throw new RuntimeException("Ошибка при поиске товара по ID", e);
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
            throw new RuntimeException("Ошибка при поиске всех товаров", e);
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
            throw new RuntimeException("Ошибка при удалении товара", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Динамически строит SQL-запрос {@code SELECT} на основе
     * не-null полей из {@link SearchCriteria}.
     * Использует {@code ILIKE} для регистронезависимого поиска.
     */
    @Override
    public List<Product> search(SearchCriteria criteria) {
        List<Product> products = new ArrayList<>();
        // Используем "чистый" SELECT ... FROM ...
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

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске (search) товаров", e);
        }

        return products;
    }

    /**
     * Вспомогательный "маппер" (DRY-принцип).
     * Превращает одну строку {@link ResultSet} в объект {@link Product}.
     *
     * @param rs {@link ResultSet}, установленный на текущую строку.
     * @return Заполненный объект {@link Product}.
     * @throws SQLException если имя колонки не найдено или тип не совпадает.
     */
    private Product mapRowToProduct(ResultSet rs) throws SQLException {
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