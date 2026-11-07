package mx.unam.buzz.rutinas.common.security;

import mx.unam.buzz.rutinas.dominio.usuario.Perfil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restringe un endpoint a ciertos perfiles. Sin la anotacion basta con tener
 * sesion. Las reglas por area (solo las rondas de mis areas) viven en el servicio.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequierePerfil {

    Perfil[] value();
}
