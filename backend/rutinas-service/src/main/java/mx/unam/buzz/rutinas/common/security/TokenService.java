package mx.unam.buzz.rutinas.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import mx.unam.buzz.rutinas.common.constant.AuthErrorCode;
import mx.unam.buzz.rutinas.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Emite y valida el token de sesion: un JWT firmado con HMAC-SHA256 que solo lleva
 * el id del usuario y su perfil. El servidor no guarda sesiones.
 */
@Component
public class TokenService {

    private static final String CLAIM_PERFIL = "perfil";

    private final SecretKey llave;
    private final Duration duracion;

    public TokenService(@Value("${taskin.seguridad.secreto}") String secreto,
                        @Value("${taskin.seguridad.duracion-horas:12}") long horas) {
        this.llave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracion = Duration.ofHours(horas);
    }

    /** Token emitido y su fecha de expiracion. */
    public record Token(String valor, Instant expiraEn) {
    }

    public Token emitir(Long idUsuario, String perfil) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(duracion);
        String valor = Jwts.builder()
                .subject(String.valueOf(idUsuario))
                .claim(CLAIM_PERFIL, perfil)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expira))
                .signWith(llave)
                .compact();
        return new Token(valor, expira);
    }

    /** Devuelve el id del usuario o lanza 401 si el token no es valido o ya expiro. */
    public Long validar(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(llave).build().parseSignedClaims(token).getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException(AuthErrorCode.TOKEN_INVALID.getCode(), HttpStatus.UNAUTHORIZED);
        }
    }
}
