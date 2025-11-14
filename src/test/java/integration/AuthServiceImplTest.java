package integration;

import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.AuditRepository;
import repository.JdbcAuditRepository;
import repository.JdbcUserRepository;
import repository.UserRepository;
import service.AuditServiceImpl;
import service.AuthServiceImpl;
import util.ConnectionManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceImplTest extends BaseIntegrationTest {

    private UserRepository userRepository;
    private AuditRepository auditRepository;
    private AuditServiceImpl auditService;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        userRepository = new JdbcUserRepository();
        auditRepository = new JdbcAuditRepository();
        auditService = new AuditServiceImpl(auditRepository);
        authService = new AuthServiceImpl(userRepository, auditService);
        auditService.setAuthService(authService);

        try (var conn = ConnectionManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE app_schema.audit_log RESTART IDENTITY");
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось очистить audit_log перед тестом", e);
        }
    }

    @Test
    @DisplayName("Должен успешно логинить пользователя (из Liquibase) с верными данными")
    void testLogin_Success() {
        Optional<User> result = authService.login("admin", "admin123");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("admin");
        assertThat(authService.getCurrentUser()).isPresent();

        List<String> logs = auditRepository.findAll();

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0)).contains("User: [admin]", "Action: [LOGIN_SUCCESS]");
    }

    @Test
    @DisplayName("Должен отклонять логин, если пользователь не найден")
    void testLogin_Failure_UserNotFound() {
        Optional<User> result = authService.login("unknown", "password");

        assertThat(result).isNotPresent();
        assertThat(authService.getCurrentUser()).isNotPresent();

        List<String> logs = auditRepository.findAll();

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0)).contains("User: [SYSTEM]", "Action: [LOGIN_FAILURE");
    }

    @Test
    @DisplayName("Должен отклонять логин, если пароль неверный")
    void testLogin_Failure_WrongPassword() {
        Optional<User> result = authService.login("admin", "wrong_password");

        assertThat(result).isNotPresent();
        assertThat(authService.getCurrentUser()).isNotPresent();

        List<String> logs = auditRepository.findAll();

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0)).contains("User: [SYSTEM]", "Action: [LOGIN_FAILURE");
    }

    @Test
    @DisplayName("Должен корректно выполнять выход, если пользователь был залогинен")
    void testLogout_WhenLoggedIn() {
        authService.login("admin", "admin123");

        assertThat(authService.getCurrentUser()).isPresent();

        authService.logout();

        assertThat(authService.getCurrentUser()).isNotPresent();

        List<String> logs = auditRepository.findAll();

        assertThat(logs).hasSize(2);
        assertThat(logs.get(1)).contains("User: [admin]", "Action: [LOGOUT]");
    }

    @Test
    @DisplayName("Должен возвращать true для isAdmin(), если вошел админ")
    void testIsAdmin_True_WhenAdminLoggedIn() {
        authService.login("admin", "admin123");

        assertThat(authService.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("Должен возвращать false для isAdmin(), если никто не залогинен")
    void testIsAdmin_False_WhenNotLoggedIn() {
        assertThat(authService.isAdmin()).isFalse();
    }
}