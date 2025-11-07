package service;

import model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.ProductRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Тесты для {@link ProductServiceImpl}.
 * Мы используем Mockito, чтобы "замокать" (сымитировать)
 * зависимости (репозиторий и аудит).
 */
@ExtendWith(MockitoExtension.class) // Включаем Mockito
class ProductServiceImplTest {

    /** Мок репозитория, зависимость класса {@link ProductServiceImpl}. */
    @Mock
    private ProductRepository productRepository;

    /** Мок сервиса аудита, зависимость класса {@link ProductServiceImpl}. */
    @Mock
    private AuditService auditService;

    /**
     * Тестируемый класс.
     * Mockito автоматически внедрит в него {@code productRepository}
     * и {@code auditService}.
     */
    @InjectMocks
    private ProductServiceImpl productService;

    // --- Тестовые случаи ---

    /**
     * Тестирует сценарий {@link ProductService#getProductById(long)},
     * когда продукт с заданным ID существует в репозитории.
     * <p>
     * Ожидается:
     * 1. Возвращается {@link Optional} с нужным продуктом.
     * 2. Метод {@code productRepository.findById} вызывается 1 раз.
     */
    @Test
    void testGetProductById_WhenProductExists() {
        // 1. Arrange (Подготовка)
        Product testProduct = new Product("Ноутбук", "Электроника", "Brand", 100, 10);
        testProduct.setId(1L);

        // "Когда кто-то вызовет productRepository.findById(1L),
        //  тогда верни Optional.of(testProduct)"
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // 2. Act (Действие)
        Optional<Product> result = productService.getProductById(1L);

        // 3. Assert (Проверка)
        assertThat(result).isPresent(); // Проверяем, что Optional не пустой
        assertThat(result.get().getName()).isEqualTo("Ноутбук");

        // Проверяем, что метод findById был вызван ровно 1 раз с аргументом 1L
        verify(productRepository, times(1)).findById(1L);
    }

    /**
     * Тестирует сценарий {@link ProductService#addProduct(Product)}.
     * <p>
     * Ожидается:
     * 1. Метод {@code productRepository.save} вызывается 1 раз.
     * 2. Метод {@code auditService.logAction} вызывается 1 раз с
     * сообщением "ADD_PRODUCT:".
     * 3. Кэш сбрасывается (проверяется в
     * {@link #testSearchCache_ShouldBeInvalidatedAfterAddProduct()}).
     */
    @Test
    void testAddProduct_ShouldSaveAndLog() {
        // 1. Arrange
        Product newProduct = new Product("Телефон", "Электроника", "Brand", 50, 5);
        // "сохраненный" продукт будет иметь ID
        Product savedProduct = new Product("Телефон", "Электроника", "Brand", 50, 5);
        savedProduct.setId(1L);

        // Когда будет вызван save с ЛЮБЫМ (any) объектом Product...
        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct); // ...верни savedProduct

        // 2. Act
        Product result = productService.addProduct(newProduct);

        // 3. Assert
        assertThat(result.getId()).isEqualTo(1L); // Убедились, что ID присвоился
        assertThat(result.getName()).isEqualTo("Телефон");

        // Проверяем, что save был вызван 1 раз
        verify(productRepository).save(newProduct);

        // Проверяем, что аудит был вызван 1 раз
        // с сообщением, которое НАЧИНАЕТСЯ с "ADD_PRODUCT"
        verify(auditService).logAction(startsWith("ADD_PRODUCT:"));
    }

    /**
     * Тестирует сценарий {@link ProductService#deleteProduct(long)}.
     * <p>
     * Ожидается:
     * 1. Метод {@code productRepository.deleteById} вызывается 1 раз
     * с корректным ID.
     * 2. Метод {@code auditService.logAction} вызывается 1 раз с
     * сообщением "DELETE_PRODUCT: id=42".
     */
    @Test
    void testDeleteProduct_ShouldDeleteAndLog() {
        // 1. Arrange
        long idToDelete = 42L;
        // Для void методов (которые ничего не возвращают)
        doNothing().when(productRepository).deleteById(idToDelete);

        // 2. Act
        productService.deleteProduct(idToDelete);

        // 3. Assert
        // Проверяем, что deleteById был вызван 1 раз с нужным ID
        verify(productRepository).deleteById(42L);

        // Проверяем, что аудит был вызван
        verify(auditService).logAction("DELETE_PRODUCT: id=42");
    }

    /**
     * Тестирует работу кэша в {@link ProductService#searchProducts}.
     * <p>
     * Ожидается:
     * 1. При первом вызове `searchProducts` -
     * {@code productRepository.search} ВЫЗЫВАЕТСЯ.
     * 2. При втором вызове `searchProducts` с теми же параметрами -
     * {@code productRepository.search} **НЕ ВЫЗЫВАЕТСЯ**
     * (результат берется из кэша).
     * 3. В итоге {@code productRepository.search} вызывается всего 1 раз.
     */
    @Test
    void testSearchProducts_ShouldUseCacheOnSecondCall() {
        // 1. Arrange
        String category = "Электроника";
        List<Product> mockResults = List.of(
                new Product("Ноутбук", "Электроника", "B", 100, 1)
        );

        // Настраиваем мок репозитория:
        when(productRepository.search(eq(category), isNull(), isNull(), isNull()))
                .thenReturn(mockResults);

        // 2. Act (Первый вызов - "Cache Miss")
        List<Product> results1 = productService.searchProducts(
                category, null, null, null);

        // 2. Act (Второй вызов - "Cache Hit")
        List<Product> results2 = productService.searchProducts(
                category, null, null, null);

        // 3. Assert
        assertThat(results1).isEqualTo(mockResults);
        assertThat(results2).isEqualTo(mockResults); // Результаты одинаковые

        // !!! ГЛАВНАЯ ПРОВЕРКА КЭША !!!
        // Мы ДВАЖДЫ вызвали сервис, но репозиторий (физический поиск)
        // должен был быть вызван ТОЛЬКО ОДИН РАЗ (при первом вызове).
        verify(productRepository, times(1))
                .search(eq(category), isNull(), isNull(), isNull());

        // ...и лог "CACHE_MISS" тоже должен быть вызван только 1 раз
        verify(auditService, times(1))
                .logAction(startsWith("CACHE_MISS:"));
    }

    /**
     * Тестирует инвалидацию (сброс) кэша после {@link ProductService#addProduct}.
     * <p>
     * Ожидается:
     * 1. Первый вызов {@code searchProducts} (кэш наполняется).
     * 2. Вызов {@code addProduct} (кэш сбрасывается).
     * 3. Второй вызов {@code searchProducts} (кэш пуст,
     * снова идет вызов {@code productRepository.search}).
     * 4. В итоге {@code productRepository.search} вызывается 2 раза.
     * 5. Лог "CACHE_INVALIDATED" вызывается.
     */
    @Test
    void testSearchCache_ShouldBeInvalidatedAfterAddProduct() {
        // 1. Arrange
        List<Product> mockResults = List.of(new Product("Ноутбук", "Э", "B", 100, 1));
        when(productRepository.search(any(), any(), any(), any()))
                .thenReturn(mockResults);

        // Мок для save
        when(productRepository.save(any(Product.class)))
                .thenReturn(new Product("Телефон", "T", "C", 200, 2));

        // 2. Act
        // Первый поиск -> кладет в кэш
        productService.searchProducts(null, null, null, null);

        // Добавляем продукт -> должен сбросить кэш
        productService.addProduct(new Product());

        // Второй поиск -> должен СНОВА пойти в репозиторий (т.к. кэш сброшен)
        productService.searchProducts(null, null, null, null);

        // 3. Assert
        // Проверяем, что search был вызван ДВАЖДЫ (до и после сброса кэша)
        verify(productRepository, times(2))
                .search(any(), any(), any(), any());

        // Проверяем, что CACHE_INVALIDATED был вызван
        verify(auditService).logAction("CACHE_INVALIDATED");
    }
}