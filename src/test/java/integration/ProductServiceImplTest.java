package integration;

import model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.JdbcProductRepository;
import repository.ProductRepository;
import service.AuditService;
import service.ProductService;
import service.ProductServiceImpl;
import service.dto.SearchCriteria;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest extends BaseIntegrationTest {

    @Mock
    private AuditService auditService;

    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = new JdbcProductRepository();
        productService = new ProductServiceImpl(productRepository, auditService);
    }

    @Test
    @DisplayName("Должен найти продукт (из Liquibase) по ID")
    void testGetProductById_WhenProductExists() {
        SearchCriteria bookCriteria = new SearchCriteria("Книги", null, null, null);
        Product book = productRepository.search(bookCriteria).get(0);

        assertThat(book.getName()).isEqualTo("Книга 'Чистая Архитектура'");

        Optional<Product> result = productService.getProductById(book.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Книга 'Чистая Архитектура'");
    }

    @Test
    @DisplayName("Должен сохранить новый продукт в БД")
    void testAddProduct_ShouldSaveAndLog() {
        Product newProduct = new Product("Тестовый Товар", "Категория", "Бренд", 199.99, 10);
        long initialCount = productRepository.findAll().size();

        Product savedProduct = productService.addProduct(newProduct);

        assertThat(savedProduct.getId()).isNotNull().isGreaterThan(0);

        Optional<Product> foundInDb = productRepository.findById(savedProduct.getId());

        assertThat(foundInDb).isPresent();
        assertThat(foundInDb.get().getName()).isEqualTo("Тестовый Товар");
        assertThat(productRepository.findAll().size()).isEqualTo(initialCount + 1);
    }

    @Test
    @DisplayName("Должен удалить продукт из БД")
    void testDeleteProduct_ShouldDeleteAndLog() {
        SearchCriteria bookCriteria = new SearchCriteria("Книги", null, null, null);
        long bookId = productRepository.search(bookCriteria).get(0).getId();
        long initialCount = productRepository.findAll().size();

        productService.deleteProduct(bookId);

        assertThat(productRepository.findById(bookId)).isNotPresent();
        assertThat(productRepository.findAll().size()).isEqualTo(initialCount - 1);
    }

    @Test
    @DisplayName("Должен возвращать корректные данные при повторном поиске (кэш)")
    void testSearchProducts_ShouldUseCacheOnSecondCall() {
        SearchCriteria criteria = new SearchCriteria("Электроника", null, null, null);

        List<Product> results1 = productService.searchProducts(criteria);
        List<Product> results2 = productService.searchProducts(criteria);

        assertThat(results1).hasSize(2);
        assertThat(results2).isEqualTo(results1);
    }

    @Test
    @DisplayName("Должен возвращать актуальные данные после добавления нового продукта (сброс кэша)")
    void testSearchCache_ShouldBeInvalidatedAfterAddProduct() {
        SearchCriteria criteria = new SearchCriteria("Электроника", null, null, null);

        productService.searchProducts(criteria);

        productService.addProduct(new Product("Новый", "Электроника", "Б", 1.0, 1));

        List<Product> results = productService.searchProducts(criteria);

        assertThat(results).anyMatch(p -> p.getName().equals("Новый"));
    }
}