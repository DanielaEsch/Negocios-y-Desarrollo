/**
 * Modelos de pagos: corresponden a los VOs de /api/v1/pagos.
 */
export type Periodo = 'MENSUAL' | 'ANUAL';
export type MetodoPago = 'TARJETA' | 'TRANSFERENCIA';
export type EstadoOrden = 'PENDIENTE_PAGO' | 'PROCESANDO' | 'PAGADO' | 'RECHAZADO' | 'VENCIDO';
export type EstadoSuscripcion = 'PRUEBA' | 'ACTIVA' | 'VENCIDA';

export interface Plan {
  id: number;
  clave: string;
  nombre: string;
  descripcion: string;
  precioMensual: number;
  precioAnual: number;
  actual: boolean;
}

export interface DatosFiscales {
  rfc: string;
  razonSocial: string;
  codigoPostal: string;
  regimenFiscal: string;
  usoCfdi: string;
}

export interface Suscripcion {
  plan: Plan;
  estado: EstadoSuscripcion;
  fechaInicio: string;
  vigenciaFin: string;
  diasRestantes: number;
  datosFiscales: DatosFiscales | null;
}

export interface LineaDescuento {
  concepto: string;
  monto: number;
}

/** Total = (Subtotal - Descuentos) + IVA, calculado siempre por el servidor. */
export interface Desglose {
  precioMensual: number;
  meses: number;
  subtotal: number;
  descuentos: LineaDescuento[];
  descuento: number;
  base: number;
  iva: number;
  total: number;
  moneda: string;
}

export interface Cotizacion {
  plan: Plan;
  periodo: Periodo;
  cupon: string | null;
  desglose: Desglose;
}

export interface OrdenPago {
  id: number;
  folio: string;
  plan: Plan | null;
  periodo: Periodo;
  metodo: MetodoPago;
  estado: EstadoOrden;
  desglose: Desglose;
  cupon: string | null;
  fechaOrden: string;
  fechaExpiracion: string;
  fechaPago: string | null;
  referenciaPago: string | null;
  tarjeta: { marca: string; ultimos4: string } | null;
  motivoRechazo: string | null;
  datosFiscales: DatosFiscales;
  transferencia: { banco: string; clabe: string; beneficiario: string; referencia: string } | null;
  factura: {
    id: number;
    folioFiscal: string;
    serieFolio: string;
    fechaTimbrado: string;
    rfcEmisor: string;
    razonSocialEmisor: string;
    formaPago: string;
  } | null;
  solicitante: string | null;
  validador: string | null;
}

/** Lo que se manda al cotizar. Nunca lleva precios. */
export interface CotizacionRequest {
  plan: string;
  periodo: Periodo;
  cupon?: string | null;
}

export interface OrdenRequest extends CotizacionRequest {
  metodo: MetodoPago;
  datosFiscales: DatosFiscales;
}

/** Datos de la tarjeta: solo viajan del navegador a la pasarela. */
export interface DatosTarjeta {
  numero: string;
  mesExpiracion: number;
  anioExpiracion: number;
  cvc: string;
  titular: string;
}

export interface TokenTarjeta {
  id: string;
  tarjeta: { marca: string; ultimos4: string };
}

export const ESTADOS_FINALES: EstadoOrden[] = ['PAGADO', 'RECHAZADO', 'VENCIDO'];
