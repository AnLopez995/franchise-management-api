# Infraestructura como código (Terraform)

Aprovisiona la solución en AWS + MongoDB Atlas:

- **MongoDB Atlas** — proyecto + cluster **M0 (free tier)** + usuario de BD + lista de acceso por IP.
- **AWS** — repositorio **ECR** + servicio **App Runner** que ejecuta la imagen de la API, con el
  `MONGODB_URI` del cluster inyectado como variable de entorno.

## Requisitos

- [Terraform](https://developer.hashicorp.com/terraform/install) >= 1.5
- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) con credenciales configuradas (`aws configure`)
- Docker (para construir y publicar la imagen)
- Una [API Key de MongoDB Atlas](https://www.mongodb.com/docs/atlas/configure-api-access/) (Public/Private) con permiso de **Project Owner** en la organización, y el **Org ID**

## Variables

Copia el ejemplo y completa los secretos (el archivo real está gitignored):

```bash
cp terraform.tfvars.example terraform.tfvars
# edita terraform.tfvars: atlas_public_key, atlas_private_key, atlas_org_id, db_password
```

## Despliegue (paso a paso)

App Runner necesita que la imagen **ya exista** en ECR, así que se aplica en dos pasos.

```bash
cd infra/terraform
terraform init

# 1) Crear solo el repositorio ECR
terraform apply -target=aws_ecr_repository.api

# 2) Construir y publicar la imagen (desde la raíz del repo)
ECR_URL=$(terraform output -raw ecr_repository_url)
AWS_REGION=us-east-1
aws ecr get-login-password --region "$AWS_REGION" | \
  docker login --username AWS --password-stdin "${ECR_URL%/*}"

docker build -t franchises-api ../..
docker tag franchises-api:latest "$ECR_URL:latest"
docker push "$ECR_URL:latest"

# 3) Crear el resto (Atlas + App Runner apuntando a la imagen)
terraform apply

# 4) Obtener la URL pública de la API
terraform output app_runner_service_url
```

La API queda en `https://<app_runner_service_url>` (Swagger en `/swagger-ui.html`).

## Actualizar la aplicación

Reconstruye y publica la imagen (paso 2) y fuerza un nuevo despliegue:

```bash
aws apprunner start-deployment --service-arn "$(terraform output -raw app_runner_service_arn 2>/dev/null || true)"
# o sube una nueva etiqueta y cambia var.image_tag
```

## Outputs

| Output                   | Descripción                                            |
|--------------------------|--------------------------------------------------------|
| `ecr_repository_url`     | URL del repositorio ECR donde publicar la imagen       |
| `app_runner_service_url` | URL pública HTTPS de la API                             |
| `atlas_cluster_srv`      | Connection string SRV de Atlas (sin credenciales)      |
| `mongodb_uri`            | `MONGODB_URI` completo inyectado (sensitive)           |

## Destruir

```bash
terraform destroy
```

## Notas

- **`allowed_cidr` por defecto es `0.0.0.0/0`** porque las IPs de salida de App Runner son
  dinámicas. Para producción, restringe el acceso (p. ej. App Runner con VPC Connector + IP fija
  vía NAT, y ese CIDR en la lista de acceso de Atlas).
- El cluster **M0 es gratuito** y suficiente para la prueba; no soporta backups ni escalado.
- `terraform.tfvars` y el estado (`*.tfstate`) contienen secretos y están **gitignored**. Para
  trabajo en equipo, usa un backend remoto (S3 + DynamoDB lock).
