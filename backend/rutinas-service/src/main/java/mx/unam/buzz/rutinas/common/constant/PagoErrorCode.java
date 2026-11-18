package mx.unam.buzz.rutinas.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codigos de error del modulo de pagos (1600-1699).
 */
@Getter
@RequiredArgsConstructor
public enum PagoErrorCode {
    PLAN_NOT_FOUND(1601),
    CUPON_INVALIDO(1602),
    NOT_FOUND(1603),
    SUSCRIPCION_NOT_FOUND(1604),
    FIRMA_INVALIDA(1605),
    COBRO_EN_PROCESO(1606),
    PASARELA_NO_DISPONIBLE(1607),
    STATE_NOT_SUPPORTED(1608),
    FACTURA_NOT_FOUND(1609),
    PERIODO_O_METODO_INVALIDO(1610),
    SOLO_TRANSFERENCIAS(1611);

    private final Integer code;
}
