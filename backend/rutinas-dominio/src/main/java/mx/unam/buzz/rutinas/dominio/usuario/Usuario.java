package mx.unam.buzz.rutinas.dominio.usuario;

import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Usuario del sistema. Entidad de dominio pura: no depende de Spring ni de JPA,
 * por eso sus reglas se prueban sin base de datos.
 *
 * Cumplimiento LFPDPPP: no se puede registrar a nadie sin su consentimiento para
 * el tratamiento de datos personales, y la contrasena solo se guarda como hash.
 */
public class Usuario {

    public static final int LONGITUD_MINIMA_CONTRASENA = 8;

    private final String nombre;
    private final String correo;
    private String hashContrasena;
    private Perfil perfil;
    private boolean activo;
    private boolean consentimientoDatos;
    private LocalDateTime fechaConsentimiento;
    private final Set<Long> areas = new HashSet<>();

    private Usuario(String nombre, String correo) {
        this.nombre = nombre;
        this.correo = correo;
    }

    /**
     * Registra un usuario nuevo como OPERADOR.
     *
     * @param aceptaAvisoPrivacidad el usuario acepto el aviso de privacidad
     * @param cifrador              genera el hash de la contrasena
     */
    public static Usuario registrar(String nombre, String correo, String contrasenaPlana,
                                    boolean aceptaAvisoPrivacidad, CifradorContrasena cifrador) {
        validar(!estaVacio(nombre), "El nombre es obligatorio");
        validar(!estaVacio(correo), "El correo es obligatorio");
        validar(contrasenaPlana != null && contrasenaPlana.length() >= LONGITUD_MINIMA_CONTRASENA,
                "La contrasena debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres");
        validar(aceptaAvisoPrivacidad,
                "Sin consentimiento para el tratamiento de datos personales no se puede registrar al usuario");
        Objects.requireNonNull(cifrador, "cifrador");

        Usuario usuario = new Usuario(nombre.trim(), correo.trim().toLowerCase());
        usuario.hashContrasena = cifrador.cifrar(contrasenaPlana);
        usuario.perfil = Perfil.OPERADOR;
        usuario.activo = true;
        usuario.consentimientoDatos = true;
        usuario.fechaConsentimiento = LocalDateTime.now();
        return usuario;
    }

    /** Valida una contrasena contra el hash guardado. */
    public boolean verificarContrasena(String contrasenaPlana, CifradorContrasena cifrador) {
        return contrasenaPlana != null && cifrador.coincide(contrasenaPlana, hashContrasena);
    }

    public void asignarArea(Long idArea) {
        validar(idArea != null && idArea > 0, "El area no es valida");
        areas.add(idArea);
    }

    /**
     * Derecho de oposicion (ARCO): el usuario retira su consentimiento.
     * Mientras no lo vuelva a otorgar, no puede ser promovido.
     */
    public void revocarConsentimiento() {
        consentimientoDatos = false;
        fechaConsentimiento = null;
    }

    public void desactivar() {
        activo = false;
    }

    /**
     * Regla core: un OPERADOR pasa a SUPERVISOR solo si esta activo, mantiene su
     * consentimiento de datos y tiene al menos un area a su cargo.
     */
    public void promoverASupervisor() {
        validar(activo, "Un usuario inactivo no puede ser promovido");
        validar(consentimientoDatos, "El usuario retiro su consentimiento de datos; no puede ser promovido");
        validar(perfil == Perfil.OPERADOR, "Solo un OPERADOR puede ser promovido a SUPERVISOR");
        validar(!areas.isEmpty(), "Un supervisor necesita al menos un area asignada");
        perfil = Perfil.SUPERVISOR;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getHashContrasena() {
        return hashContrasena;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public boolean isActivo() {
        return activo;
    }

    public boolean isConsentimientoDatos() {
        return consentimientoDatos;
    }

    public LocalDateTime getFechaConsentimiento() {
        return fechaConsentimiento;
    }

    public Set<Long> getAreas() {
        return Collections.unmodifiableSet(areas);
    }

    private static void validar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new ReglaNegocioException(mensaje);
        }
    }

    private static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
