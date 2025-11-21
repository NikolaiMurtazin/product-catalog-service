package web.listener;

import factory.RepositoryFactory;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import repository.AuditRepository;
import repository.ProductRepository;
import repository.UserRepository;
import service.AuditServiceImpl;
import service.AuthServiceImpl;
import service.ProductService;
import service.ProductServiceImpl;
import util.LiquibaseRunner;
import web.util.ServiceRegistry;

/**
 * Слушатель жизненного цикла веб-приложения.
 * Запускается при старте сервера и остановке.
 * Аналог метода main() для веб-приложений.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== Инициализация приложения Marketplace (Web) ===");

        try {
            LiquibaseRunner.runMigrations();

            ProductRepository productRepo = RepositoryFactory.getProductRepository();
            UserRepository userRepo = RepositoryFactory.getUserRepository();
            AuditRepository auditRepo = RepositoryFactory.getAuditRepository();

            AuditServiceImpl auditService = new AuditServiceImpl(auditRepo);
            AuthServiceImpl authService = new AuthServiceImpl(userRepo, auditService);
            auditService.setAuthService(authService);

            ProductService productService = new ProductServiceImpl(productRepo, auditService);

            ServiceRegistry.setAuditService(auditService);
            ServiceRegistry.setAuthService(authService);
            ServiceRegistry.setProductService(productService);

            ServletContext ctx = sce.getServletContext();
            ctx.setAttribute("productService", productService);
            ctx.setAttribute("authService", authService);

            System.out.println("=== Приложение успешно запущено ===");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Критическая ошибка при старте приложения", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("=== Остановка приложения Marketplace ===");
    }
}