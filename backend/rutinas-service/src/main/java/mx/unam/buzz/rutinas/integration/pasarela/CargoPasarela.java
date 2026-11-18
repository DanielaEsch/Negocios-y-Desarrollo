package mx.unam.buzz.rutinas.integration.pasarela;

/**
 * Lo que responde la pasarela al abrir un cargo. El estado aqui casi siempre es
 * "procesando": el resultado final llega por webhook.
 */
public record CargoPasarela(String id, String estado, String marca, String ultimos4) {
}
