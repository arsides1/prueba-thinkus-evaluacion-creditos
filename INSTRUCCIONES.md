# Instrucciones para ejecutar el proyecto

Documento dirigido a quien va a revisar la prueba técnica. Contiene los
pasos exactos para levantar el ecosistema completo, validarlo y solucionar
los problemas más comunes.

---

## Resumen en una línea

```bash
git clone https://github.com/arsides1/prueba-thinkus-evaluacion-creditos.git && cd prueba-thinkus-evaluacion-creditos && docker compose up --build
```

Tras aproximadamente un minuto (la primera vez compila imágenes), el
sistema está disponible en **http://localhost:4200**.

---

## Pre-requisitos

| Herramienta | Versión mínima | Para qué |
|---|---|---|
| Docker Desktop | 27.x (con Docker Compose v2) | Levantar todo el ecosistema |
| Git | 2.x | Clonar el repositorio |
| Navegador moderno | Chrome / Firefox / Edge actuales | Abrir el frontend |

**No se requiere** instalar Java, Maven, Node, ni PostgreSQL en el host.
Todo corre dentro de contenedores Docker.

### Puertos que deben estar libres

Por defecto el sistema usa estos puertos del host. Si alguno está ocupado,
ver la sección [Solución de problemas](#solucion-de-problemas).

| Puerto | Servicio |
|---|---|
| `5432` | PostgreSQL (BD `prueba_thinkus`) |
| `8080` | Microservicio A (Orquestador, BFF) |
| `8081` | Microservicio B (Riesgos Mock) |
| `4200` | Frontend Angular (Nginx) |

---

## Paso a paso

### 1. Clonar el repositorio

```bash
git clone https://github.com/arsides1/prueba-thinkus-evaluacion-creditos.git
cd prueba-thinkus-evaluacion-creditos
```

### 2. Levantar todo el ecosistema

```bash
docker compose up --build
```

La **primera vez** este comando hace lo siguiente (toma 60–120 segundos):

1. Descarga las imágenes base (`postgres:16-alpine`, `maven:3.9-eclipse-temurin-21`, `node:22-alpine`, `nginx:1.27-alpine`, `ubi9/openjdk-21`).
2. Construye las imágenes de los dos microservicios Quarkus (compila con Maven el módulo `commons` + el microservicio).
3. Construye la imagen del frontend (compila con `ng build` y empaqueta con Nginx).
4. Levanta los 4 contenedores en una red privada `thinkus-net`.
5. Espera a que PostgreSQL esté listo (`pg_isready`).
6. Arranca el orquestador, que **automáticamente** corre la migración Flyway `V1__create_evaluaciones_table.sql`.
7. Arranca el frontend.

Cuando todo está listo verás en la consola:

```
thinkus-frontend         | ... ready
thinkus-ms-orquestador   | ... ms-orquestador 1.0.0-SNAPSHOT on JVM started in X.Xs. Listening on: http://0.0.0.0:8080
thinkus-ms-riesgos       | ... ms-riesgos 1.0.0-SNAPSHOT on JVM started in X.Xs. Listening on: http://0.0.0.0:8081
thinkus-postgres         | ... database system is ready to accept connections
```

### 3. Probar el sistema

Abrir en el navegador:

| URL | Qué muestra |
|---|---|
| **http://localhost:4200** | Frontend Angular (formulario de evaluación + historial) |
| http://localhost:8080/q/swagger-ui | Swagger UI del orquestador (probar `POST /v1/credit-evaluations` directamente) |
| http://localhost:8081/q/swagger-ui | Swagger UI del MS B (probar `GET /v1/riesgos/score/{cedula}`) |
| http://localhost:8080/q/health | Health check del orquestador |

**Cédulas ecuatorianas válidas para probar** (cumplen el algoritmo Módulo 10):

```
1716123458
0500000005
0500000500
```

### 4. Detener el sistema

```bash
docker compose down       # Detiene los contenedores; mantiene la BD
docker compose down -v    # Detiene y BORRA el volumen de Postgres (BD limpia)
```

---

## Validación con curl

```bash
# Caso APROBADO esperado (cédula válida, salario alto, monto bajo)
curl -X POST http://localhost:8080/v1/credit-evaluations \
  -H "Content-Type: application/json" \
  -d '{"cedula":"1716123458","monto":500,"anios":3,"salario":3000}'

# Caso 400 con Problem Details (cédula inválida)
curl -i -X POST http://localhost:8080/v1/credit-evaluations \
  -H "Content-Type: application/json" \
  -d '{"cedula":"1234567890","monto":500,"anios":3,"salario":3000}'

# Listar el historial completo
curl http://localhost:8080/v1/credit-evaluations
```

La respuesta del POST tarda **aproximadamente 2 segundos** porque el
orquestador llama en paralelo a los dos endpoints del MS B (latencia
simulada 2.0 s y 1.5 s respectivamente, en paralelo el total es ~2.0 s).

---

## Solución de problemas

### Algún puerto está ocupado en mi máquina

Cualquiera de los puertos 5432, 8080, 8081 o 4200 puede estar en uso.
Para cambiar los puertos del host **sin modificar código versionado**,
copiar el archivo de ejemplo y editarlo:

```bash
cp .env.example .env
```

Luego editar `.env` y cambiar los puertos que necesite. Por ejemplo, si
ya tiene PostgreSQL local en 5432:

```
DB_HOST_PORT=5433
```

Y volver a ejecutar:

```bash
docker compose up --build
```

El puerto INTERNO de cada contenedor no cambia; solo cambia el puerto
expuesto al host. Los contenedores se siguen comunicando entre sí por
nombre (`postgres`, `ms-riesgos`) sin verse afectados.

### El frontend muestra "Error de comunicación con el servicio"

El navegador del usuario hace requests a `http://localhost:8080`. Si se
cambió `ORQUESTADOR_HOST_PORT` a otro valor (p. ej. 8090), el frontend
todavía intentará conectarse a 8080. Solución: dejar
`ORQUESTADOR_HOST_PORT=8080` en el `.env` (es el default), o reconstruir
el frontend con la URL correcta.

### Docker Desktop no está corriendo

Aparece error tipo `Cannot connect to the Docker daemon`. Iniciar Docker
Desktop y esperar a que el ícono diga "Engine running".

### El primer build tarda mucho

Es normal: descarga ~2 GB de imágenes base y compila Maven + npm desde
cero. Las ejecuciones posteriores reutilizan el cache de Docker y suben
en segundos.

### Quiero ver los logs de un servicio específico

```bash
docker compose logs -f ms-orquestador     # logs en vivo
docker compose logs --tail=50 ms-riesgos  # últimas 50 líneas
docker compose ps                         # estado de cada servicio
```

### Quiero conectarme a la base de datos para revisar los datos

Con cualquier cliente Postgres (DBeaver, pgAdmin, psql):

```
Host:     localhost
Puerto:   5432       (o el que se haya configurado en DB_HOST_PORT)
Base:     prueba_thinkus
Usuario:  thinkus_app
Password: thinkus_app_pwd
```

Tabla principal: `public.evaluaciones`. La tabla `flyway_schema_history`
muestra qué migraciones se aplicaron.

---

## Estructura del repositorio

```
prueba-thinkus/
├── README.md                visión general del proyecto
├── INSTRUCCIONES.md         este archivo
├── docker-compose.yml       orquesta postgres + 2 micros + frontend
├── .env.example             puertos opcionales (copiar como .env si hace falta)
├── .gitlab-ci.yml           pipeline CI/CD declarado (build + test + scan + deploy)
├── backend/                 stack Java (Quarkus + PostgreSQL)
│   ├── pom.xml              parent POM Maven (BOM Quarkus 3.15.1, Java 21)
│   ├── commons/             DTOs + validador cédula EC compartido
│   ├── ms-orquestador/      Microservicio A (BFF reactivo con Mutiny)
│   └── ms-riesgos/          Microservicio B (mock con Virtual Threads)
├── frontend/                SPA Angular 20 standalone
│   ├── Dockerfile           multi-stage Node + Nginx
│   ├── nginx.conf           SPA fallback + gzip + cache
│   └── src/app/             core/ + shared/ + features/creditos/
├── infra/                   Terraform DECLARADO (no aplicado)
│   ├── README.md
│   ├── modules/             vpc, eks, rds, ecr, sqs (módulos oficiales)
│   └── environments/        dev/ y prd/ con backend S3 + DynamoDB lock
└── chart/                   Helm chart genérico para los micros (IRSA, HPA, Ingress ALB)
```

---

## Contacto

**Arsides Ancajima Valencia** — arsidesav@gmail.com

Repositorio: https://github.com/arsides1/prueba-thinkus-evaluación-creditos
