package mx.unam.buzz.rutinas.dominio.usuario;

import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la regla de negocio de Usuario en aislamiento: sin Spring, sin base
 * de datos y con un cifrador falso en lugar de BCrypt.
 */
class UsuarioTest {

    private static final String CONTRASENA = "rutina2026";

    /** Cifrador falso: basta con que el hash sea distinto del texto plano. */
    private final CifradorContrasena cifrador = new CifradorContrasena() {
        @Override
        public String cifrar(String contrasenaPlana) {
            return "hash:" + new StringBuilder(contrasenaPlana).reverse();
        }

        @Override
        public boolean coincide(String contrasenaPlana, String hash) {
            return cifrar(contrasenaPlana).equals(hash);
        }
    };

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.registrar("Ana Lopez", "Ana@Negocio.mx", CONTRASENA, true, cifrador);
    }

    // --- Registro y cumplimiento LFPDPPP ---

    @Test
    void registrarCreaOperadorActivoConConsentimiento() {
        assertEquals(Perfil.OPERADOR, usuario.getPerfil());
        assertTrue(usuario.isActivo());
        assertTrue(usuario.isConsentimientoDatos());
        assertNotNull(usuario.getFechaConsentimiento());
        assertEquals("ana@negocio.mx", usuario.getCorreo());
    }

    @Test
    void registrarSinConsentimientoFalla() {
        assertThrows(ReglaNegocioException.class,
                () -> Usuario.registrar("Ana Lopez", "ana@negocio.mx", CONTRASENA, false, cifrador));
    }

    @Test
    void registrarConContrasenaCortaFalla() {
        assertThrows(ReglaNegocioException.class,
                () -> Usuario.registrar("Ana Lopez", "ana@negocio.mx", "corta", true, cifrador));
    }

    @Test
    void laContrasenaSoloSeGuardaComoHash() {
        assertNotEquals(CONTRASENA, usuario.getHashContrasena());
        assertTrue(usuario.verificarContrasena(CONTRASENA, cifrador));
        assertFalse(usuario.verificarContrasena("otraClave99", cifrador));
    }

    // --- Regla core: promoverASupervisor ---

    @Test
    void operadorConAreaEsPromovidoASupervisor() {
        usuario.asignarArea(1L);

        usuario.promoverASupervisor();

        assertEquals(Perfil.SUPERVISOR, usuario.getPerfil());
    }

    @Test
    void sinAreasAsignadasNoSePuedePromover() {
        assertThrows(ReglaNegocioException.class, usuario::promoverASupervisor);
        assertEquals(Perfil.OPERADOR, usuario.getPerfil());
    }

    @Test
    void sinConsentimientoNoSePuedePromover() {
        usuario.asignarArea(1L);
        usuario.revocarConsentimiento();

        assertThrows(ReglaNegocioException.class, usuario::promoverASupervisor);
        assertEquals(Perfil.OPERADOR, usuario.getPerfil());
    }

    @Test
    void usuarioInactivoNoSePuedePromover() {
        usuario.asignarArea(1L);
        usuario.desactivar();

        assertThrows(ReglaNegocioException.class, usuario::promoverASupervisor);
    }

    @Test
    void unSupervisorNoSePuedeVolverAPromover() {
        usuario.asignarArea(1L);
        usuario.promoverASupervisor();

        assertThrows(ReglaNegocioException.class, usuario::promoverASupervisor);
    }
}
