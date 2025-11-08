package service;

import model.Role;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.AuditRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private AuthService authService;

    private AuditServiceImpl auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditServiceImpl(auditRepository);
        auditService.setAuthService(authService);
    }

    @Test
    @DisplayName("Должен писать лог от имени [admin], если юзер залогинен")
    void testLogAction_WithAuthenticatedUser() {
        User adminUser = new User(1L, "admin", "admin123", Role.ADMIN);
        when(authService.getCurrentUser()).thenReturn(Optional.of(adminUser));

        auditService.logAction("TEST_ACTION");

        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditRepository).save(logCaptor.capture());

        String loggedEvent = logCaptor.getValue();
        assertThat(loggedEvent)
                .contains("User: [admin]")
                .contains("Action: [TEST_ACTION]");
    }

    @Test
    @DisplayName("Должен писать лог от имени [SYSTEM], если юзер не залогинен")
    void testLogAction_WithNoUser() {
        when(authService.getCurrentUser()).thenReturn(Optional.empty());

        auditService.logAction("TEST_ACTION");

        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditRepository).save(logCaptor.capture());

        String loggedEvent = logCaptor.getValue();
        assertThat(loggedEvent)
                .contains("User: [SYSTEM]")
                .contains("Action: [TEST_ACTION]");
    }

    @Test
    @DisplayName("Должен писать лог от [SYSTEM], если AuthService еще не внедрен")
    void testLogAction_AuthServiceNotSet() {
        AuditServiceImpl freshAuditService = new AuditServiceImpl(auditRepository);

        freshAuditService.logAction("BOOTSTRAP_ACTION");

        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditRepository).save(logCaptor.capture());

        String loggedEvent = logCaptor.getValue();
        assertThat(loggedEvent)
                .contains("User: [SYSTEM]")
                .contains("Action: [BOOTSTRAP_ACTION]");
    }

    @Test
    @DisplayName("Должен возвращать всю историю аудита из репозитория")
    void testGetAuditHistory() {
        List<String> mockHistory = List.of("Event 1", "Event 2");
        when(auditRepository.findAll()).thenReturn(mockHistory);

        List<String> history = auditService.getAuditHistory();

        assertThat(history).isEqualTo(mockHistory);
        verify(auditRepository).findAll();
    }
}