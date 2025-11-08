package service;

import model.Role;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        adminUser = new User(1L, "admin", "admin123", Role.ADMIN);
        regularUser = new User(2L, "user", "user123", Role.USER);
    }

    @Test
    @DisplayName("Должен успешно логинить пользователя с верными данными")
    void testLogin_Success() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        Optional<User> result = authService.login("admin", "admin123");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(adminUser);
        assertThat(authService.getCurrentUser()).isPresent();
        verify(auditService).logAction("LOGIN_SUCCESS");
    }

    @Test
    @DisplayName("Должен отклонять логин, если пользователь не найден")
    void testLogin_Failure_UserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        Optional<User> result = authService.login("unknown", "password");

        assertThat(result).isNotPresent();
        assertThat(authService.getCurrentUser()).isNotPresent();
        verify(auditService).logAction(startsWith("LOGIN_FAILURE"));
    }

    @Test
    @DisplayName("Должен отклонять логин, если пароль неверный")
    void testLogin_Failure_WrongPassword() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        Optional<User> result = authService.login("admin", "wrong_password");

        assertThat(result).isNotPresent();
        assertThat(authService.getCurrentUser()).isNotPresent();
        verify(auditService).logAction(startsWith("LOGIN_FAILURE"));
    }

    @Test
    @DisplayName("Должен корректно выполнять выход, если пользователь был залогинен")
    void testLogout_WhenLoggedIn() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        authService.login("admin", "admin123");
        assertThat(authService.getCurrentUser()).isPresent();

        authService.logout();

        assertThat(authService.getCurrentUser()).isNotPresent();
        verify(auditService).logAction("LOGOUT");
    }

    @Test
    @DisplayName("Не должен вызывать лог аудита при выходе, если никто не залогинен")
    void testLogout_WhenNotLoggedIn() {
        authService.logout();

        assertThat(authService.getCurrentUser()).isNotPresent();
        verify(auditService, never()).logAction(anyString());
    }

    @Test
    @DisplayName("Должен возвращать true для isAdmin(), если вошел админ")
    void testIsAdmin_True_WhenAdminLoggedIn() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        authService.login("admin", "admin123");

        assertThat(authService.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("Должен возвращать false для isAdmin(), если вошел обычный пользователь")
    void testIsAdmin_False_WhenUserLoggedIn() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        authService.login("user", "user123");

        assertThat(authService.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("Должен возвращать false для isAdmin(), если никто не залогинен")
    void testIsAdmin_False_WhenNotLoggedIn() {
        assertThat(authService.isAdmin()).isFalse();
    }
}