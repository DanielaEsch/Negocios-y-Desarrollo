package mx.unam.buzz.rutinas.dominio.pago;

/**
 * Periodo que se paga por adelantado. El anual regala dos meses: se cobra el
 * subtotal de 12 y se descuentan 2, antes de impuestos.
 */
public enum Periodo {

    MENSUAL(1, 0),
    ANUAL(12, 2);

    private final int meses;
    private final int mesesDeRegalo;

    Periodo(int meses, int mesesDeRegalo) {
        this.meses = meses;
        this.mesesDeRegalo = mesesDeRegalo;
    }

    public int getMeses() {
        return meses;
    }

    public int getMesesDeRegalo() {
        return mesesDeRegalo;
    }
}
