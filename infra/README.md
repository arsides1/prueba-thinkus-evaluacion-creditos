# Infraestructura como código (Terraform)

Esta carpeta contiene la definición de la infraestructura objetivo del
sistema en AWS. **Está declarada pero no aplicada**: el ecosistema
funcional se levanta localmente con `docker compose up`. Aplicar
Terraform real implica:

- Costos AWS (~USD 73/mes solo por el control plane de EKS).
- Provisionar VPC, NAT Gateway, RDS Postgres, EKS — toma 30-45 minutos.
- Cuenta AWS con permisos administrativos.

Queda como **propuesta de despliegue** lista para `terraform apply`
en cualquier cuenta válida.

---

## Estructura

```
infra/
├── modules/                 modulos reutilizables (sin estado propio)
│   ├── network/             VPC + subnets publicas/privadas/aisladas
│   ├── eks/                 cluster Kubernetes managed
│   ├── rds/                 PostgreSQL multi-AZ
│   ├── ecr/                 registries para las imagenes
│   ├── sqs/                 colas para eventos asincronos (futuro)
│   └── eventbridge/         bus de eventos (futuro)
└── environments/
    ├── dev/                 cluster dev: 1 NAT, instances pequenas
    │   ├── main.tf
    │   ├── variables.tf
    │   ├── backend.tf       state remoto en S3 + lock DynamoDB
    │   └── terraform.tfvars
    └── prd/                 cluster prod: multi-AZ NAT, instances medianas
        ├── main.tf
        ├── variables.tf
        ├── backend.tf
        └── terraform.tfvars
```

Esta estructura sigue las recomendaciones de Gruntwork y
[terraform-best-practices](https://www.terraform-best-practices.com/).
Cada ambiente tiene su propio state, su propia bucket S3 de backend,
y consume los mismos módulos con valores distintos.

---

## Como aplicarlo (cuando se quiera)

### Pre-requisitos

- AWS CLI v2 configurado con credenciales adecuadas (mejor con SSO o role,
  no access keys hardcodeadas).
- Terraform >= 1.6.
- Bucket S3 para state (creado UNA SOLA VEZ a mano):
  ```bash
  aws s3 mb s3://thinkus-tfstate-PRD --region us-east-1
  aws s3api put-bucket-versioning --bucket thinkus-tfstate-PRD \
        --versioning-configuration Status=Enabled
  ```
- Tabla DynamoDB para state lock:
  ```bash
  aws dynamodb create-table --table-name thinkus-tfstate-lock \
        --attribute-definitions AttributeName=LockID,AttributeType=S \
        --key-schema AttributeName=LockID,KeyType=HASH \
        --billing-mode PAY_PER_REQUEST --region us-east-1
  ```

### Aplicar el ambiente dev

```bash
cd infra/environments/dev
terraform init        # descarga providers y conecta al backend S3
terraform plan        # diff: que va a crear/cambiar/destruir
terraform apply       # aplica (pide confirmacion)
```

Tras el apply (~30 min):

- Cluster EKS listo en region `us-east-1`
- RDS PostgreSQL apuntable desde dentro de la VPC
- ECR repositories `thinkus/ms-orquestador` y `thinkus/ms-riesgos` listos
  para push
- Outputs imprimen el endpoint del cluster y la URL de conexión a Postgres

### Despues del apply

```bash
# Configurar kubectl para apuntar al cluster
aws eks update-kubeconfig --region us-east-1 --name thinkus-dev

# Push de imagenes
$(aws ecr get-login-password --region us-east-1) | docker login --username AWS --password-stdin <account>.dkr.ecr.us-east-1.amazonaws.com
docker tag thinkus/ms-orquestador:1.0.0 <account>.dkr.ecr.us-east-1.amazonaws.com/thinkus/ms-orquestador:1.0.0
docker push <account>.dkr.ecr.us-east-1.amazonaws.com/thinkus/ms-orquestador:1.0.0

# Deploy con Helm
helm upgrade --install ms-orquestador ../../chart \
  --namespace creditos --create-namespace \
  --set image.repository=<account>.dkr.ecr.us-east-1.amazonaws.com/thinkus/ms-orquestador \
  --set image.tag=1.0.0
```

---

## Modulos elegidos

Se usan los módulos oficiales del registro
[terraform-aws-modules](https://registry.terraform.io/namespaces/terraform-aws-modules):

- `terraform-aws-modules/vpc/aws` — VPC con subnets en 3 AZ, NAT y endpoints
- `terraform-aws-modules/eks/aws` — Cluster EKS con managed node groups,
  IRSA habilitado, addons gestionados (vpc-cni, coredns, kube-proxy, ebs-csi)
- `terraform-aws-modules/rds/aws` — PostgreSQL 16 multi-AZ en prd, single-AZ
  en dev, parameter groups y subnet groups gestionados

Esto evita reinventar 600 lineas de HCL por módulo y se beneficia de las
mejores practicas que la comunidad ya tiene refinadas.

---

## Por qué no se incluye Terraform aplicado

La estrategia es entregar tres capas claras:

- **Funcional**: microservicios + frontend ejecutables localmente con
  `docker compose up`.
- **Declarado**: Terraform, Helm chart y pipeline GitLab CI listos para
  ejecutarse en una cuenta AWS válida.
- **Documentado**: README explica las decisiones y alternativas.

Aplicar Terraform en producción consume tiempo significativo en
provisioning, debugging de IAM, networking y security groups, además de
costos AWS reales (~USD 73/mes solo por el control plane de EKS). Por
eso se entrega declarado y listo para aplicar, pero no aplicado.
