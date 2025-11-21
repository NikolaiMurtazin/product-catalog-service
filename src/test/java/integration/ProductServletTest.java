package integration;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.ProductService;
import web.servlet.ProductServlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServletTest {

    @Mock
    private ProductService productService;
    @Mock
    private ServletConfig servletConfig;
    @Mock
    private ServletContext servletContext;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private ProductServlet productServlet;
    private ByteArrayOutputStream responseOutputStream;

    @BeforeEach
    void setUp() throws Exception {
        productServlet = new ProductServlet();

        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("productService")).thenReturn(productService);
        productServlet.init(servletConfig);

        responseOutputStream = new ByteArrayOutputStream();
        ServletOutputStreamMock servletOutputStream = new ServletOutputStreamMock(responseOutputStream);
        lenient().when(response.getOutputStream()).thenReturn(servletOutputStream);
    }

    @Test
    @DisplayName("GET /products должен вернуть список товаров в JSON")
    void doGet_ShouldReturnAllProducts() throws Exception {
        Product product = new Product(1L, "Test", "Cat", "Brand", 100.0, 10);
        when(productService.getAllProducts()).thenReturn(List.of(product));

        productServlet.doGet(request, response);

        verify(response).setContentType("application/json");
        assertThat(responseOutputStream.toString()).contains("\"name\":\"Test\"");
    }

    @Test
    @DisplayName("GET /products/1 должен вернуть товар по ID")
    void doGet_ById_ShouldReturnProduct() throws Exception {
        when(request.getPathInfo()).thenReturn("/1");
        Product product = new Product(1L, "Test", "Cat", "Brand", 100.0, 10);
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));

        productServlet.doGet(request, response);

        assertThat(responseOutputStream.toString()).contains("\"id\":1");
        assertThat(responseOutputStream.toString()).contains("\"name\":\"Test\"");
    }

    @Test
    @DisplayName("POST /products должен создать товар и вернуть 201")
    void doPost_ShouldCreateProduct() throws Exception {
        String jsonRequest = "{\"name\":\"New\",\"price\":500.0,\"stock\":5}";

        ServletInputStreamMock inputStream = new ServletInputStreamMock(
                new ByteArrayInputStream(jsonRequest.getBytes(StandardCharsets.UTF_8)));
        when(request.getInputStream()).thenReturn(inputStream);

        Product createdProduct = new Product(1L, "New", null, null, 500.0, 5);
        when(productService.addProduct(any(Product.class))).thenReturn(createdProduct);

        productServlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        assertThat(responseOutputStream.toString()).contains("\"name\":\"New\"");
    }

    private static class ServletOutputStreamMock extends jakarta.servlet.ServletOutputStream {
        private final ByteArrayOutputStream outputStream;

        public ServletOutputStreamMock(ByteArrayOutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void write(int b) throws IOException {
            outputStream.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
        }
    }

    private static class ServletInputStreamMock extends jakarta.servlet.ServletInputStream {
        private final ByteArrayInputStream inputStream;

        public ServletInputStreamMock(ByteArrayInputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }
    }
}