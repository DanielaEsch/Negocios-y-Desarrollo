export const environment = {
  production: false,
  appName: 'TaskIn',
  apiUrl: '/api/v1',
  /**
   * Pasarela de pago. El navegador le manda la tarjeta directo con la llave publica;
   * TaskIn solo recibe el token (PCI-DSS). En la demo es el simulador del backend.
   */
  pasarela: {
    url: '/api/v1/sandbox/pasarela',
    llavePublica: 'pk_test_taskin_sandbox',
  },
};
