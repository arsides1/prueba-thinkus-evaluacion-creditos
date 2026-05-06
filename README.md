# Mini-Ecosistema de Evaluacion de Creditos

Sistema de evaluación crediticia con frontend Angular, dos microservicios
Quarkus reactivos y PostgreSQL, dockerizado y orquestable con un solo
comando.

---

## Arquitectura

```
   +-----------------+         +--------------------+         +-------------------+
   |  Frontend       |         |  ms-orquestador    |         |  ms-riesgos       |
   |  Angular 20     |  POST   |  (BFF / Quarkus)   |  REST   |  (Quarkus mock)   |
   |  Bootstrap 5    +-------->+  Mutiny reactivo   +-------->+  Virtual Threads  |
   |  Nginx alpine   |  GET    |  Hibernate Reactive|  parallel |  latencia 2s/1.5s|
   |  :4200          |         |  :8080             |         |  :8081            |
   +-----------------+         +--------------------+         +-------------------+
                                          |
                                          v
                               +----------------------+
                               |  PostgreSQL 16       |
                               |  prueba_thinkus      |
                               |  Flyway migrations   |
                               |  :5432               |
                               +----------------------+
```

**Flujo central**:
1. El usuario ingresa cédula, monto, años y salario en el frontend.
2. El frontend valida la cédula con el algoritmo Módulo 10 (feedback instantáneo).
3. POST `/v1/credit-evaluations` al orquestador.
4. El orquestador llama EN PARALELO los dos endpoints del MS B usando
   `Uni.combine().all().unis(...)` de Mutiny:
   - GET `/v1/riesgos/score/{cedula}` (latencia simulada 2.0s)
   - GET `/v1/riesgos/deudas/{cedula}` (latencia simulada 1.5s)
5. Total ~2.0s en lugar de 3.5s secuenciales (ahorro 43%).
6. Aplica la politica: APROBADO si `score > 70` y
   `(deudaMensual + montoSolicitado) < (salario * 0.40)`.
7. Persiste en PostgreSQL con Hibernate Reactive Panache.
8. Devuelve resultado al frontend con badge APROBADO/RECHAZADO.

---

## Stack tecnológico

| Capa | Tecnologia |
|---|---|
| Backend | Java 21 LTS, Quarkus 3.15+, Mutiny, Hibernate Reactive Panache |
| Cliente HTTP | REST Client Reactive (MicroProfile) + SmallRye Fault Tolerance |
| Persistencia | PostgreSQL 16, Flyway migrations |
| Frontend | Angular 20.3 standalone, Signals, Reactive Forms, Bootstrap 5 |
| Build | Maven multi-módulo, npm/Vite |
| Contenedores | Docker multi-stage, docker-compose |
| Observabilidad | SmallRye Health (`/q/health`), OpenAPI + Swagger UI |
| Validacion | Bean Validation (commons module), validador cédula EC compartido |
| Testing | JUnit 5, Mockito, RestAssured, Karma + Jasmine |

---

## Como correr (local, recomendado)

### Opcion A: docker-compose (mas simple, todo levantado en un comando)

```bash
docker compose up --build
```

Tras ~1 minuto (build inicial), tienes:
- http://localhost:4200            -> Frontend
- http://localhost:8080/q/swagger-ui -> Swagger del orquestador
- http://localhost:8081/q/swagger-ui -> Swagger del MS B
- localhost:5432                    -> Postgres (user `thinkus_app`, db `prueba_thinkus`)

Para detener:
```bash
docker compose down       # mantiene la BD
docker compose down -v    # borra la BD y empieza limpio
```

### Opcion B: cada servicio aparte (para desarrollo)

Pre-requisitos:
- JDK 21 (Eclipse Temurin recomendado)
- Maven 3.9+
- Node 22+ y npm 10+
- PostgreSQL 17 corriendo en localhost:5432

Setup de la base de datos (una vez):
```sql
CREATE USER thinkus_app WITH PASSWORD 'thinkus_app_pwd';
CREATE DATABASE prueba_thinkus WITH OWNER = thinkus_app ENCODING 'UTF8';
GRANT ALL PRIVILEGES ON DATABASE prueba_thinkus TO thinkus_app;
```

Levantar (en 3 terminales distintas):
```bash
# Terminal 1: ms-riesgos
cd backend/ms-riesgos
mvn quarkus:dev      # 8081

# Terminal 2: ms-orquestador (Flyway aplicara V1__... al startup)
cd backend/ms-orquestador
mvn quarkus:dev      # 8080

# Terminal 3: frontend
cd frontend
npm install
npm start            # 4200
```

---

## Validacion rapida con curl

```bash
# Caso APROBADO esperado (cedula valida, salario alto, monto bajo)
curl -X POST http://localhost:8080/v1/credit-evaluations \
  -H "Content-Type: application/json" \
  -d '{"cedula":"1716123458","monto":500,"anios":3,"salario":3000}'

# Caso 400 (cedula invalida con Problem Details)
curl -i -X POST http://localhost:8080/v1/credit-evaluations \
  -H "Content-Type: application/json" \
  -d '{"cedula":"1234567890","monto":500,"anios":3,"salario":3000}'

# Listar historial
curl http://localhost:8080/v1/credit-evaluations
```

---

## Tests

```bash
# Backend (98 tests)
cd backend
mvn clean test

# Frontend (12 tests)
cd frontend
npm test -- --watch=false --browsers=ChromeHeadless
```

Total: **110 tests pasando**.

| Módulo | Tests | Tipo |
|---|---|---|
| commons | 18 | Unitarios puros (validador cédula EC, algoritmo Módulo 10) |
| ms-riesgos | 67 | 6 smoke tests del recurso REST + 61 generadores random |
| ms-orquestador | 13 | 9 dominio (politica) + 4 use case (Mockito puro) |
| frontend | 12 | 2 App shell + 10 cédula validator |

---

## Estructura del repositorio

```
prueba-thinkus/
├── README.md                         este archivo
├── INSTRUCCIONES.md                  instrucciones de despliegue local
├── docker-compose.yml                orquesta postgres + 2 micros + frontend
├── .gitlab-ci.yml                    pipeline build + test + scan + package + deploy
├── .env.example                      puertos opcionales
│
├── backend/                          stack Java
│   ├── pom.xml                       parent POM (Quarkus BOM, Java 21)
│   ├── commons/                      DTOs + validador cedula EC compartido
│   ├── ms-orquestador/               Microservicio A (BFF)
│   └── ms-riesgos/                   Microservicio B (mock)
│
├── frontend/                         SPA Angular 20
│   ├── package.json
│   ├── Dockerfile                    multi-stage Node + Nginx
│   ├── nginx.conf                    SPA fallback + gzip + cache
│   └── src/app/
│       ├── core/                     servicios + tipos
│       ├── shared/                   validadores + spinner
│       └── features/creditos/        pages + components
│
├── infra/                            Terraform DECLARADO (no aplicado)
│   ├── README.md
│   ├── modules/                      vpc, eks, rds, ecr, sqs
│   └── environments/{dev,prd}/
│
└── chart/                            Helm chart generico para los micros
    ├── Chart.yaml
    ├── values.yaml
    └── templates/                    Deployment, Service, ServiceAccount(IRSA), HPA, Ingress
```

---

## Decisiones técnicas (resumen)

1. **Frontend**: Angular 20 standalone con signals y nuevo control flow.
2. **Repositorio**: mono-repo con separación `backend/` + `frontend/` y multi-módulo Maven.
3. **Comunicacion A <-> B**: REST + OpenAPI (gRPC documentado como alternativa evaluada).
4. **Modelo de programacion**: Mutiny end-to-end en MS A + Virtual Threads en MS B.
5. **Persistencia**: Repository pattern con Hibernate Reactive Panache.
6. **Migraciones**: Flyway con SQL puro.
7. **Estructura interna**: Hexagonal (Ports & Adapters) estricto.
8. **Alcance entrega**: tres capas — Funcionando + Declarado + Documentado.

---

## AWS deployment proposal

La carpeta `infra/` declara la infraestructura objetivo en Terraform usando
los módulos oficiales `terraform-aws-modules/*`:

- **VPC** con 3 AZs, subnets publicas/privadas/aisladas, NAT por AZ en prd
- **EKS** con managed node groups, IRSA habilitado, addons gestionados
- **RDS PostgreSQL 16** multi-AZ en prd, con password gestionado en Secrets Manager
- **ECR** con scan-on-push, lifecycle policies, encriptacion KMS
- **SQS + DLQ** preparados para eventos asincronos futuros

No está aplicada en esta entrega para mantener alcance y evitar costos.
El README en `infra/README.md` explica el setup paso a paso para
`terraform apply` en una cuenta válida.

El chart de Helm en `chart/` esta listo para `helm upgrade --install` con
deployment, service, serviceaccount con annotation IRSA, HPA opcional e
ingress ALB opcional.

El pipeline `.gitlab-ci.yml` cubre build + test + scan (Trivy) + package
(kaniko a ECR) + deploy (Helm a EKS, gateado como `when: manual` por
seguridad). Usa OIDC federation para asumir roles AWS sin guardar
access keys.

---

## Alternativas evaluadas y descartadas

- **gRPC entre A y B**: descartado porque para 2 microservicios la
  simplicidad operativa de REST + OpenAPI supera las ventajas de protobuf.
  Reservado para mallas de 10+ microservicios internos.
- **AppSync (GraphQL)**: descartado porque no hay clientes mobile
  heterogeneos pidiendo shapes distintas, ni necesidad de subscripciones.
- **Liquibase**: descartado a favor de Flyway por simplicidad de SQL puro
  y menor curva. Liquibase queda como opcion para escenarios con
  rollbacks declarativos o auditoria SOX estricta.
- **Active Record (Panache)**: descartado a favor de Repository pattern
  para mantener entidades como POJOs sin logica y facilitar mocking en
  tests del use case.

---

## Repositorio

https://github.com/arsides1/prueba-thinkus-evaluación-creditos

---

## Contacto

**Arsides Ancajima Valencia**
arsidesav@gmail.com
