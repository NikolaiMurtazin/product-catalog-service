package service;

import model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.ProductRepository;
import service.dto.SearchCriteria;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    @DisplayName("Должен вернуть продукт по ID, если он существует")
    void testGetProductById_WhenProductExists() {
        Product testProduct = new Product("Ноутбук", "Электроника", "Brand", 100, 10);
        testProduct.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Optional<Product> result = productService.getProductById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Ноутбук");

        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("Должен сохранить продукт и записать лог аудита")
    void testAddProduct_ShouldSaveAndLog() {
        Product newProduct = new Product("Телефон", "Электроника", "Brand", 50, 5);
        Product savedProduct = new Product("Телефон", "Электроника", "Brand", 50, 5);
        savedProduct.setId(1L);

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        Product result = productService.addProduct(newProduct);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Телефон");

        verify(productRepository).save(newProduct);
        verify(auditService).logAction(startsWith("ADD_PRODUCT:"));
    }

    @Test
    @DisplayName("Должен удалить продукт и записать лог аудита")
    void testDeleteProduct_ShouldDeleteAndLog() {
        long idToDelete = 42L;
        doNothing().when(productRepository).deleteById(idToDelete);

        productService.deleteProduct(idToDelete);

        verify(productRepository).deleteById(42L);
        verify(auditService).logAction("DELETE_PRODUCT: id=42");
    }

    @Test
    @DisplayName("Должен использовать кэш при повторном поиске с теми же критериями")
    void testSearchProducts_ShouldUseCacheOnSecondCall() {
        SearchCriteria criteria = new SearchCriteria("Электроника", null, null, null);
        List<Product> mockResults = List.of(
                new Product("Ноутбук", "Электроника", "B", 100, 1)
        );

        when(productRepository.search(criteria)).thenReturn(mockResults);

        List<Product> results1 = productService.searchProducts(criteria);
        List<Product> results2 = productService.searchProducts(criteria);

        assertThat(results1).isEqualTo(mockResults);
        assertThat(results2).isEqualTo(mockResults);

        verify(productRepository).search(criteria);
        verify(auditService).logAction(startsWith("CACHE_MISS:"));
    }

    @Test
    @DisplayName("Должен сбрасывать кэш поиска после добавления нового продукта")
    void testSearchCache_ShouldBeInvalidatedAfterAddProduct() {
        SearchCriteria criteria = new SearchCriteria(null, null, null, null);
        List<Product> mockResults = List.of(new Product("Ноутбук", "Э", "B", 100, 1));

        when(productRepository.search(criteria)).thenReturn(mockResults);
        when(productRepository.save(any(Product.class)))
                .thenReturn(new Product("Телефон", "T", "C", 200, 2));

        productService.searchProducts(criteria);
        productService.addProduct(new Product());
        productService.searchProducts(criteria);

        verify(productRepository, times(2)).search(criteria);
        verify(auditService).logAction("CACHE_INVALIDATED");
    }
}