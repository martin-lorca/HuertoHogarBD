package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la clase AuthService.
 * Utilizamos @ExtendWith(MockitoExtension.class) para habilitar las anotaciones de Mockito.
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
    private final String ENCODED_PASSWORD = "encoded_password";
    private final String GENERATED_TOKEN = "jwt.token.generated";

    // Setup: Inicialización común antes de cada test si fuera necesario.
    // En este caso, @InjectMocks se encarga de inyectar las dependencias al constructor
    // antes de cada prueba, por lo que este método puede ser omitido o usado para
    // configuraciones más complejas.

    // =========================================================================
    //                            PRUEBAS DE REGISTRO
    // =========================================================================

    /**
     * Prueba 1: Verifica que el registro de un usuario nuevo se realiza con éxito.
     */
    @Test
    void testRegisterUser_Success_ReturnsNewUser() throws Exception {
        // Configuración (Arrange)
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        // El mock simulará guardar el usuario y devolverlo.
        User expectedUser = new User(TEST_USERNAME, ENCODED_PASSWORD, TEST_FULLNAME, List.of("ROLE_USER"));
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        // Ejecución (Act)
        User resultUser = authService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME);

        // Verificación (Assert)
        assertNotNull(resultUser);
        assertEquals(TEST_USERNAME, resultUser.getUsername());
        assertEquals(ENCODED_PASSWORD, resultUser.getPassword());

        // Verificamos que se llamó al método save en el repositorio
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Prueba 2: Verifica que se lanza una excepción si el nombre de usuario (email) ya existe.
     */
    @Test
    void testRegisterUser_UserAlreadyExists_ThrowsException() {
        // Configuración (Arrange)
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

        // Ejecución y Verificación (Act & Assert)
        Exception exception = assertThrows(Exception.class, () -> {
            authService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME);
        });

        assertEquals("El email ya está registrado.", exception.getMessage());
        // Verificamos que NO se llamó al método save (no tiene sentido guardar si ya existe)
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Prueba 3: Verifica que la contraseña SIEMPRE se cifra antes de guardar.
     */
    @Test
    void testRegisterUser_PasswordIsEncoded() throws Exception {
        // Configuración (Arrange)
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Devuelve el objeto que se intentó guardar

        // Ejecución (Act)
        User resultUser = authService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME);

        // Verificación (Assert)
        // 1. Verificamos que se usó el encoder
        verify(passwordEncoder, times(1)).encode(TEST_PASSWORD);
        // 2. Verificamos que el usuario guardado tiene la contraseña codificada
        assertEquals(ENCODED_PASSWORD, resultUser.getPassword());
    }

    /**
     * Prueba 4: Verifica que al nuevo usuario se le asigna el rol por defecto (ROLE_USER).
     */
    @Test
    void testRegisterUser_AssignsDefaultRole() throws Exception {
        // Configuración (Arrange)
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        // Capturamos el argumento de save para verificar sus roles
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecución (Act)
        User resultUser = authService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME);

        // Verificación (Assert)
        assertTrue(resultUser.getRoles().contains("ROLE_USER"));
    }

    /**
     * Prueba 5: Verifica que el método save() del repositorio es llamado exactamente una vez.
     */
    @Test
    void testRegisterUser_SavesUserToRepository() throws Exception {
        // Configuración (Arrange)
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecución (Act)
        authService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME);

        // Verificación (Assert)
        // Verificamos que se llamó exactamente 1 vez al método save
        verify(userRepository, times(1)).save(any(User.class));
    }


    // =========================================================================
    //                             PRUEBAS DE LOGIN
    // =========================================================================

    /**
     * Mock para simular los detalles del usuario autenticado.
     */
    @Mock
    private UserDetails userDetailsMock;

    /**
     * Mock para simular el objeto de autenticación devuelto por AuthenticationManager.
     */
    @Mock
    private Authentication authenticationMock;


    /**
     * Prueba 6: Verifica que un inicio de sesión exitoso devuelve un token JWT.
     */
    @Test
    void testLogin_Success_ReturnsToken() throws Exception {
        // Configuración (Arrange)
        // 1. Configurar AuthenticationManager para devolver un objeto Authentication exitoso
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authenticationMock);

        // 2. Configurar el objeto Authentication para devolver los UserDetails
        when(authenticationMock.getPrincipal()).thenReturn(userDetailsMock);

        // 3. Configurar JwtTokenUtil para generar el token
        when(jwtTokenUtil.generateToken(userDetailsMock)).thenReturn(GENERATED_TOKEN);

        // Ejecución (Act)
        String token = authService.login(TEST_USERNAME, TEST_PASSWORD);

        // Verificación (Assert)
        assertNotNull(token);
        assertEquals(GENERATED_TOKEN, token);
        // Verificamos que el token se generó con los detalles del usuario correcto
        verify(jwtTokenUtil, times(1)).generateToken(userDetailsMock);
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
        verify(jwtTokenUtil, never()).generateToken(any());
    }

    /**
     * Prueba 8: Verifica que se llama al método generateToken del JwtTokenUtil.
     */
    @Test
    void testLogin_TokenGenerationCalled() throws Exception {
        // Configuración (Arrange)
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authenticationMock);
        when(authenticationMock.getPrincipal()).thenReturn(userDetailsMock);
        when(jwtTokenUtil.generateToken(userDetailsMock)).thenReturn(GENERATED_TOKEN);

        // Ejecución (Act)
        authService.login(TEST_USERNAME, TEST_PASSWORD);

        // Verificación (Assert)
        // Verificamos que se llamó exactamente una vez al método generateToken
        verify(jwtTokenUtil, times(1)).generateToken(userDetailsMock);
    }

    /**
     * Prueba 9: Verifica que se llama al método authenticate del AuthenticationManager
     * con el token de usuario/contraseña correcto.
     */
    @Test
    void testLogin_CallsAuthenticationManager() throws Exception {
        // Configuración (Arrange)
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authenticationMock);
        when(authenticationMock.getPrincipal()).thenReturn(userDetailsMock);
        when(jwtTokenUtil.generateToken(userDetailsMock)).thenReturn(GENERATED_TOKEN);

        // Ejecución (Act)
        authService.login(TEST_USERNAME, TEST_PASSWORD);

        // Verificación (Assert)
        // Creamos el objeto exacto que esperamos que se pase al método authenticate
        UsernamePasswordAuthenticationToken expectedToken = new UsernamePasswordAuthenticationToken(TEST_USERNAME, TEST_PASSWORD);

        // Verificamos que se llamó a authenticate con los argumentos correctos
        verify(authenticationManager, times(1)).authenticate(eq(expectedToken));
    }

    /**
     * Prueba 10: Verifica que se extrae correctamente el objeto UserDetails del resultado de la autenticación.
     */
    @Test
    void testLogin_ExtractsUserDetailsFromAuthentication() throws Exception {
        // Configuración (Arrange)
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authenticationMock);
        when(authenticationMock.getPrincipal()).thenReturn(userDetailsMock); // Simula la extracción de UserDetails
        when(jwtTokenUtil.generateToken(userDetailsMock)).thenReturn(GENERATED_TOKEN);

        // Ejecución (Act)
        authService.login(TEST_USERNAME, TEST_PASSWORD);

        // Verificación (Assert)
        // Verificamos que el método getPrincipal() fue llamado en el objeto Authentication
        verify(authenticationMock, times(1)).getPrincipal();
        // Aunque el token se generó (ver prueba 8), este test se centra en la extracción de UserDetails
        // para garantizar que la lógica intermedia es correcta.
    }
}