/**
 * Configuracion por ambiente.
 *
 * En produccion (docker-compose / EKS) este archivo se sobrescribe via
 * fileReplacements en angular.json para apuntar al host real del orquestador.
 */
export const environment = {
  production: false,
  apiBase: 'http://localhost:8080',
};
