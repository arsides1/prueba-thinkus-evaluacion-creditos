/**
 * Configuracion para el ambiente Docker.
 *
 * En docker-compose el frontend (servido por Nginx) hace requests al
 * orquestador via el nombre del servicio (ms-orquestador), no a localhost.
 * Pero como el navegador del usuario NO ve esa red interna, el frontend
 * apunta a 'http://localhost:8080' que es el puerto que docker-compose
 * expone al host.
 */
export const environment = {
  production: true,
  apiBase: 'http://localhost:8080',
};
