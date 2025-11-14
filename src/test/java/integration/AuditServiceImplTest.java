package integration;

import model.Role;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.AuditRepository;
import repository.JdbcAuditRepository;
import service.AuditServiceImpl;
import service.AuthService;
import util.ConnectionManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest extends BaseIntegrationTest {

    private AuditRepository auditRepository;

    @Mock
    private AuthService authService;

    private AuditServiceImpl auditService;

    @BeforeEach
    void setUp() {
        auditRepository = new JdbcAuditRepository();
        auditService = new AuditServiceImpl(auditRepository);
        auditService.setAuthService(authService);

        try (var conn = ConnectionManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE app_schema.audit_log RESTART IDENTITY");
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось очистить audit_log перед тестом", e);
        }
    }

    @Test
    @DisplayName("Должен писать лог от имени [admin], если юзер залогинен")
    void testLogAction_WithAuthenticatedUser() {
        User adminUser = new User(1L, "admin", "admin123", Role.ADMIN);
        when(authService.getCurrentUser()).thenReturn(Optional.of(adminUser));
        auditService.logAction("TEST_ACTION");

        List<String> logs = auditRepository.findAll();

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0))
                .contains("User: [admin]")
                .contains("Action: [TEST_ACTION]");
    }

    @Test
    @DisplayName("Должен писать лог от имени [SYSTEM], если юзер не залогинен")
    void testLogAction_WithNoUser() {
        when(authService.getCurrentUser()).thenReturn(Optional.empty());

        auditService.logAction("TEST_ACTION");

        List<String> logs = auditRepository.findAll();

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0))
                .contains("User: [SYSTEM]")
                .contains("Action: [TEST_ACTION]");
    }

    @Test
    @DisplayName("Должен писать лог от [SYSTEM], если AuthService еще не внедрен")
    void testLogAction_AuthServiceNotSet() {
        AuditServiceImpl freshAuditService = new AuditServiceImpl(auditRepository);
        freshAuditService.logAction("BOOTSTRAP_ACTION");
        List<String> logs = auditRepository.findAll();

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0))
                .contains("User: [SYSTEM]")
                .contains("Action: [BOOTSTRAP_ACTION]");
    }

    @Test
    @DisplayName("Должен возвращать всю историю аудита из репозитория")
    void testGetAuditHistory() {
        auditRepository.save("Event 1");
        auditRepository.save("Event 2");

        List<String> history = auditService.getAuditHistory();

        assertThat(history).hasSize(2);
        assertThat(history).containsExactly("Event 1", "Event 2");
    }
}