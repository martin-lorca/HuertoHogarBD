package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la clase AuthService.
 * Reflejando los cambios: registerUser acepta roles y lanza RuntimeException.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // Dependencias inyectadas en AuthService (serán Mocks)
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenUtil jwtTokenUtil;

    // Objeto a probar, donde se inyectan los Mocks
    @InjectMocks
    private AuthService authService;

    // Variables de prueba
    private final String TEST_USERNAME = "test@example.com";
    private final String TEST_PASSWORD = "password123";
    private final String TEST_FULLNAME = "Test User";
    private final List<String> TEST_ROLES = List.of("ROLE_USER");
    private final String ENCODED_PASSWORD = "encoded_password";
    private final String GENERATED_TOKEN = "jwt.token.generated";

    // Mocks comunes para login
    @Mock
    private Authentication authenticationMock;
    @Mock
    private User userPrincipalMock; // Usamos la entidad User como Principal


    // =========================================================================
    //                            PRUEBAS DE REGISTRO
    // =========================================================================

    /**
     * Prueba 1: Verifica que el registro de un usuario nuevo se realiza con éxito.
     */
    @Test
    void testRegisterUser_Success_ReturnsNewUser() {
        // Configuración (Arrange)
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        // El mock simulará guardar el usuario y devolverlo.
        User expectedUser = new User(TEST_USERNAME, ENCODED_PASSWORD, TEST_FULLNAME, TEST_ROLES);
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        // Ejecución (Act)
        // Se llama al nuevo método con el parámetro 'roles'
        User resultUser = authService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, TEST_ROLES);

        // Verificación (Assert)
        assertNotNull(resultUser);
        assertEquals(TEST_USERNAME, resultUser.getUsername());
        assertEquals(ENCODED_PASSWORD, resultUser.getPassword());
        assertTrue(resultUser.getRoles().contains("ROLE_USER"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Prueba 2: Verifica que se lanza una RuntimeException si el nombre de usuario ya existe.
     */
    @Test
    void testRegisterUser_UserAlreadyExists_ThrowsRuntimeException() {
        // Configuración (Arrange)
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

        // Ejecución y Verificación (Act & Assert)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            // Se llama al nuevo método con el parámetro 'roles'
            authService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, TEST_ROLES);
        });

        assertEquals("El nombre de usuario " + TEST_USERNAME + " ya está en uso.", exception.getMessage());
        // Verificamos que NO se llamó al método save
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Prueba 3: Verifica que la contraseña SIEMPRE se cifra antes de guardar.
     */
    @Test
    void testRegisterUser_PasswordIsEncoded() {
        // Configuración (Arrange)
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecución (Act)
        authService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, TEST_ROLES);

        // Verificación (Assert)
        // Verificamos que se usó el encoder
        verify(passwordEncoder, times(1)).encode(TEST_PASSWORD);
        // La verificación de que la contraseña guardada es la codificada se realiza dentro del .save
        verify(userRepository, times(1)).save(argThat(user -> user.getPassword().equals(ENCODED_PASSWORD)));
    }

    /**
     * Prueba 4: Verifica que si se pasan roles nulos, se asigna el rol por defecto (ROLE_USER).
     */
    @Test
    void testRegisterUser_NullRoles_AssignsDefaultRole() {
        // Configuración (Arrange)
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecución (Act)
        // Pasamos roles como null
        User resultUser = authService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, null);

        // Verificación (Assert)
        assertEquals(1, resultUser.getRoles().size());
        assertTrue(resultUser.getRoles().contains("ROLE_USER"));
    }

    /**
     * Prueba 5: Verifica que se pueden asignar roles específicos (ej. ROLE_ADMIN).
     */
    @Test
    void testRegisterUser_AssignsSpecificRole() {
        // Configuración (Arrange)
        List<String> adminRoles = List.of("ROLE_ADMIN");
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecución (Act)
        User resultUser = authService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, adminRoles);

        // Verificación (Assert)
        assertTrue(resultUser.getRoles().contains("ROLE_ADMIN"));
        assertFalse(resultUser.getRoles().contains("ROLE_USER")); // Si se especifica, no se pone el default
    }


    // =========================================================================
    //                             PRUEBAS DE LOGIN (Actualizadas)
    // =========================================================================

    /**
     * Prueba 6: Verifica que un inicio de sesión exitoso devuelve un token JWT.
     */
    @Test
    void testLogin_Success_ReturnsToken() {
        // Configuración (Arrange)
        // 1. Configurar AuthenticationManager para devolver un objeto Authentication exitoso
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authenticationMock);

        // 2. Configurar JwtTokenUtil para usar la nueva firma: generateJwtToken(Authentication)
        when(jwtTokenUtil.generateJwtToken(authenticationMock)).thenReturn(GENERATED_TOKEN);

        // Ejecución (Act)
        String token = authService.login(TEST_USERNAME, TEST_PASSWORD);

        // Verificación (Assert)
        assertNotNull(token);
        assertEquals(GENERATED_TOKEN, token);
        // Verificamos que el token se generó con el objeto Authentication (la nueva firma)
        verify(jwtTokenUtil, times(1)).generateJwtToken(authenticationMock);
    }

    /**
     * Prueba 7: Verifica que el inicio de sesión falla (lanza excepción) si la autenticación falla
     * (e.g., credenciales incorrectas).
     */
    @Test
    void testLogin_AuthenticationFails_ThrowsException() {
        // Configuración (Arrange)
        // Simular que el AuthenticationManager lanza BadCredentialsException
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Ejecución y Verificación (Act & Assert)
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(TEST_USERNAME, TEST_PASSWORD);
        });

        // Verificamos que si la autenticación falla, NO se intenta generar el token
        verify(jwtTokenUtil, never()).generateJwtToken(any());
    }

    /**
     * Prueba 8: Verifica que se llama al método generateJwtToken del JwtTokenUtil (la nueva firma).
     */
    @Test
    void testLogin_TokenGenerationCalledWithAuthenticationObject() {
        // Configuración (Arrange)
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authenticationMock);
        when(jwtTokenUtil.generateJwtToken(authenticationMock)).thenReturn(GENERATED_TOKEN);

        // Ejecución (Act)
        authService.login(TEST_USERNAME, TEST_PASSWORD);

        // Verificación (Assert)
        // Verificamos que se llamó exactamente una vez al método generateJwtToken con el objeto Authentication
        verify(jwtTokenUtil, times(1)).generateJwtToken(authenticationMock);
    }

    /**
     * Prueba 9: Verifica que se llama al método authenticate del AuthenticationManager
     * con el token de usuario/contraseña correcto.
     */
    @Test
    void testLogin_CallsAuthenticationManager() {
        // Configuración (Arrange)
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authenticationMock);
        when(jwtTokenUtil.generateJwtToken(authenticationMock)).thenReturn(GENERATED_TOKEN);

        // Ejecución (Act)
        authService.login(TEST_USERNAME, TEST_PASSWORD);

        // Verificación (Assert)
        // Creamos el objeto exacto que esperamos que se pase al método authenticate
        UsernamePasswordAuthenticationToken expectedToken = new UsernamePasswordAuthenticationToken(TEST_USERNAME, TEST_PASSWORD);

        // Verificamos que se llamó a authenticate con los argumentos correctos
        verify(authenticationManager, times(1)).authenticate(eq(expectedToken));
    }

    /**
     * Prueba 10: Verifica que el AuthenticationManager es llamado exactamente una vez durante el login.
     */
    @Test
    void testLogin_AuthenticationManagerCalledOnce() {
        // Configuración (Arrange)
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authenticationMock);
        when(jwtTokenUtil.generateJwtToken(authenticationMock)).thenReturn(GENERATED_TOKEN);

        // Ejecución (Act)
        authService.login(TEST_USERNAME, TEST_PASSWORD);

        // Verificación (Assert)
        // Verificamos que solo se llama una vez a authenticate, garantizando la eficiencia.
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}