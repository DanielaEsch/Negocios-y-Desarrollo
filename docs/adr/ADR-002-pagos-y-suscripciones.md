# ADR 002: Pagos, Suscripciones y Facturación

* **Estado**: Aceptado
* **Fecha**: 2026-11-15
* **Squad**: Equipo Buzz
* **Autores**: Alessandre Ibañez Contreras · Rojas Cruz Carlo Emir · Escamilla Champala Daniela · Bahena Gorostieta Alejandro · Rodríguez Ruiz Diana Carolina

## 1. Contexto y Problema
TaskIn se vende como suscripción mensual a micro y pequeñas empresas, así que necesita cobrar dentro del producto. Nuestros clientes son de dos tipos: el dueño que paga con tarjeta y la empresa que solo paga por transferencia y exige factura. El cobro no puede comprometer datos de tarjetas (PCI-DSS), no puede depender de que el cliente deje abierta la pestaña y debe producir un CFDI.

## 2. Decisión
* **Modelo de monetización**: suscripción por plan. Básico $599, Negocio $1,490 y Pro $2,990 MXN al mes más IVA; 14 días de prueba en el plan Negocio; el pago anual regala 2 meses.
* **Dos canales de la matriz de monetización**:
  * **Tarjeta (B2C)**: el formulario de la pasarela tokeniza en el navegador y a TaskIn solo llega un token de un solo uso. Guardamos únicamente la marca y los últimos 4 dígitos.
  * **Transferencia (B2B)**: TaskIn emite una orden de compra con CLABE y referencia. El cliente reporta la clave de rastreo y el perfil `COBRANZA` valida el depósito, en lugar del webhook.
* **El webhook es la fuente de verdad**: la orden solo pasa a `PAGADO` con el aviso firmado de la pasarela. La firma es HMAC-SHA256 con marca de tiempo (tolerancia de 300 s), y la idempotencia se garantiza por el identificador del evento (`EVENTO_PAGO` con índice único).
* **Máquina de estados** en el dominio (`OrdenPago`): `PENDIENTE_PAGO` → `PROCESANDO` → `PAGADO` o `RECHAZADO`, y `PENDIENTE_PAGO` → `VENCIDO`. La orden con tarjeta vence a los 30 minutos y la de transferencia a las 72 horas.
* **El servidor dicta el precio**: Total = (Subtotal − Descuentos) + IVA 16 %. Los descuentos pueden ser un cupón, el pago anual o el crédito por los días no usados al cambiar de plan. Un total enviado por el navegador se rechaza.
* **Facturación**: al quedar `PAGADO` se activa la suscripción y se emite un CFDI 4.0 inmutable con los datos fiscales del cliente.
* **Pasarela**: un *sandbox* propio (módulo `pasarela-sandbox`) que imita el contrato de una pasarela real (tokens, cargos asíncronos, webhooks firmados con reintentos y entregas duplicadas). El backend la usa a través de `IPasarelaPagos`.

## 3. Alternativas Consideradas
* **Opción A (Descartada)**: Recibir el número de tarjeta en el backend y cobrar desde ahí — *Motivo de descarte*: mete a TaskIn en el alcance completo de PCI-DSS (principio de *Trust Zero*).
* **Opción B (Descartada)**: Confirmar el pago con la redirección del navegador — *Motivo de descarte*: si el cliente cierra la pestaña, perdemos la transacción ("redirigir y rezar").
* **Opción C (Descartada por ahora)**: Integrar directamente el modo de prueba de Stripe o Mercado Pago — *Motivo de descarte*: exige cuentas y llaves de cada integrante, y la demo dependería de la red. El *sandbox* respeta el mismo contrato; cambiar de proveedor es otra implementación de `IPasarelaPagos` y su formulario en el frontend.
* **Opción D (Descartada)**: Publicidad o división de pagos (*payment split*) — *Motivo de descarte*: TaskIn es un SaaS B2B con un solo vendedor; no hay tráfico que monetizar ni vendedores entre quienes repartir.

## 4. Consecuencias
* **Positivas**:
  * Ningún dato de tarjeta toca nuestra base de datos ni nuestros registros.
  * Un aviso repetido o un reintento de red no cobra ni factura dos veces (probado con el *sandbox* duplicando cada aviso).
  * El mismo flujo de cierre (activar el plan y emitir la factura) sirve para tarjeta y transferencia.
* **Riesgos / Limitaciones**:
  * El CFDI es de demostración: no se timbra con un PAC, así que no tiene validez fiscal.
  * No hay cobro recurrente automático: la renovación se paga desde la pantalla de suscripción.
  * Los tokens del *sandbox* viven en memoria y se pierden al reiniciar el backend.
  * Faltan reembolsos y notas de crédito.
