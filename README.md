# Franchises API

API REST para gestionar **franquicias**, sus **sucursales** y los **productos** ofertados en
cada sucursal. Prueba técnica backend desarrollada con Java 17, Spring Boot 3 y MongoDB,
siguiendo una arquitectura limpia por capas.

Una franquicia tiene un nombre y una lista de sucursales; cada sucursal tiene un nombre y una
lista de productos; cada producto tiene un nombre y una cantidad de stock.

---

## Stack técnico

- **Java 17** · **Spring Boot 3.5**
- **Maven** (con wrapper `mvnw`)
- **Spring Web** (REST, MVC)
- **Spring Data MongoDB**
- **Bean Validation** (Jakarta)
- **springdoc-openapi** (Swagger UI)
- **JUnit 5** · **Mockito** · **MockMvc**
- **Docker** · **Docker Compose** · **MongoDB 7**
- **Terraform** (IaC) · **AWS App Runner** · **MongoDB Atlas** (despliegue cloud)

---

## Arquitectura

Arquitectura hexagonal por capas, sin sobreingeniería. El dominio no conoce Spring ni MongoDB;
la persistencia y la web son detalles que dependen del dominio a través de un puerto.

```
com.franchise.management
├── application/service        FranchiseService (orquesta los casos de uso)
├── domain
│   ├── model                  Franchise (raíz de agregado), Branch, Product, TopStockProduct
│   ├── exception              ResourceNotFoundException -> Franchise/Branch/ProductNotFoundException,
│   │                          BusinessValidationException
│   └── port                   FranchiseRepositoryPort (boundary de persistencia)
├── infrastructure
│   ├── persistence
│   │   ├── document           FranchiseDocument (branches/products embebidos), Branch/ProductDocument
│   │   ├── mapper             FranchiseDocumentMapper (domain <-> document)
│   │   ├── repository         FranchiseMongoRepository (MongoRepository)
│   │   └── adapter            FranchiseRepositoryAdapter (implementa el puerto)
│   └── web
│       ├── controller         FranchiseController
│       ├── dto/request        records con Bean Validation
│       ├── dto/response       records de respuesta + ErrorResponse
│       ├── mapper             FranchiseWebMapper (domain -> response)
│       └── exception          GlobalExceptionHandler
└── config                     OpenApiConfig
```

**Decisión clave:** la `Franchise` es la raíz del agregado. El servicio carga el agregado
completo, aplica el cambio sobre el modelo de dominio, actualiza `updatedAt` y persiste todo el
documento. Las sucursales y productos se guardan **embebidos** dentro del documento de la franquicia.

---

## Requisitos

- **Para ejecutar con Docker:** solo Docker y Docker Compose.
- **Para ejecutar sin Docker:** JDK 17+ y una instancia de MongoDB accesible
  (local o MongoDB Atlas). Maven no es necesario: se incluye el wrapper `mvnw`.

---

## Variables de entorno

| Variable      | Descripción                         | Valor por defecto                                  |
|---------------|-------------------------------------|----------------------------------------------------|
| `MONGODB_URI` | URI de conexión a MongoDB           | `mongodb://localhost:27017/franchises_db`          |

La configuración vive en `src/main/resources/application.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/franchises_db}
      database: franchises_db
```

Para **MongoDB Atlas**, basta con exportar `MONGODB_URI` con la cadena de conexión del cluster:

```bash
export MONGODB_URI="mongodb+srv://<user>:<pass>@<cluster>.mongodb.net/franchises_db"
```

---

## Cómo ejecutar con Docker Compose (recomendado)

Levanta la API y MongoDB con un solo comando:

```bash
docker compose up --build
```

- API: <http://localhost:8080>
- MongoDB: `localhost:27017`

Para detener y limpiar (incluyendo el volumen de datos):

```bash
docker compose down -v
```

---

## Cómo ejecutar sin Docker

1. Levanta un MongoDB local (por ejemplo con Docker):
   ```bash
   docker run -d --name mongo -p 27017:27017 mongo:7
   ```
2. Arranca la aplicación:
   ```bash
   ./mvnw spring-boot:run
   ```

La API queda disponible en <http://localhost:8080>.

---

## Despliegue en la nube (Terraform → AWS App Runner + MongoDB Atlas)

La persistencia y el despliegue están aprovisionados como **infraestructura como código** con
Terraform en `infra/terraform/`:

- **MongoDB Atlas** — proyecto + cluster M0 (free tier) + usuario + lista de acceso por IP.
- **AWS App Runner** — ejecuta la imagen Docker de la API (publicada en ECR), con el `MONGODB_URI`
  del cluster inyectado como variable de entorno.

Resumen (detalle completo en [`infra/terraform/README.md`](infra/terraform/README.md)):

```bash
cd infra/terraform
cp terraform.tfvars.example terraform.tfvars   # completa secretos de Atlas/AWS
terraform init
terraform apply -target=aws_ecr_repository.api  # 1) crear ECR
#    2) build + push de la imagen a ECR (ver infra/terraform/README.md)
terraform apply                                 # 3) Atlas + App Runner
terraform output app_runner_service_url         # URL pública de la API
```

> **Estado del despliegue.** La infraestructura se aprovisionó en la nube con Terraform: se crearon
> el repositorio ECR (con la imagen publicada), los roles IAM y, en MongoDB Atlas, el proyecto, el
> cluster M0 y el usuario de BD. La creación del servicio **App Runner** quedó a la espera de que AWS
> levante una restricción administrativa de la cuenta (ajena al código); en cuanto se habilite, basta
> con re-ejecutar `terraform apply` para crear el servicio restante y obtener la URL pública.

---

## Documentación interactiva (Swagger)

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

---

## Endpoints

Base: `/api/v1/franchises`

| Método   | Ruta                                                          | Descripción                                  | Éxito |
|----------|---------------------------------------------------------------|----------------------------------------------|-------|
| `POST`   | `/`                                                           | Crear franquicia                             | 201   |
| `POST`   | `/{franchiseId}/branches`                                     | Agregar sucursal                             | 201   |
| `POST`   | `/{franchiseId}/branches/{branchId}/products`                 | Agregar producto                             | 201   |
| `DELETE` | `/{franchiseId}/branches/{branchId}/products/{productId}`     | Eliminar producto                            | 204   |
| `PATCH`  | `/{franchiseId}/branches/{branchId}/products/{productId}/stock` | Modificar stock de producto                | 200   |
| `GET`    | `/{franchiseId}/branches/top-stock-products`                  | Producto con más stock por sucursal          | 200   |
| `PATCH`  | `/{franchiseId}/name`                                         | Actualizar nombre de franquicia              | 200   |
| `PATCH`  | `/{franchiseId}/branches/{branchId}/name`                     | Actualizar nombre de sucursal                | 200   |
| `PATCH`  | `/{franchiseId}/branches/{branchId}/products/{productId}/name` | Actualizar nombre de producto               | 200   |

### Respuestas de error

Formato estándar (`GlobalExceptionHandler`):

```json
{
  "timestamp": "2026-06-20T10:45:00",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Franchise not found with id: ...",
  "path": "/api/v1/franchises/..."
}
```

| Situación                                            | HTTP |
|------------------------------------------------------|------|
| Franquicia / sucursal / producto inexistente         | 404  |
| Validación de body (nombre vacío, stock null/negativo) | 400 |
| Regla de negocio violada (stock negativo en dominio) | 400  |
| Modificación concurrente sobre un agregado desactualizado | 409 |
| Error inesperado                                     | 500  |

---

## Ejemplos con curl

```bash
# 1. Crear franquicia
curl -X POST http://localhost:8080/api/v1/franchises \
  -H "Content-Type: application/json" \
  -d '{"name":"Franquicia Norte"}'
# -> 201 {"id":"<franchiseId>","name":"Franquicia Norte","branches":[]}

# 2. Agregar sucursal
curl -X POST http://localhost:8080/api/v1/franchises/<franchiseId>/branches \
  -H "Content-Type: application/json" \
  -d '{"name":"Sucursal Centro"}'

# 3. Agregar producto
curl -X POST http://localhost:8080/api/v1/franchises/<franchiseId>/branches/<branchId>/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Producto A","stock":50}'

# 4. Modificar stock
curl -X PATCH http://localhost:8080/api/v1/franchises/<franchiseId>/branches/<branchId>/products/<productId>/stock \
  -H "Content-Type: application/json" \
  -d '{"stock":120}'

# 5. Producto con más stock por sucursal
curl http://localhost:8080/api/v1/franchises/<franchiseId>/branches/top-stock-products

# 6. Eliminar producto
curl -X DELETE http://localhost:8080/api/v1/franchises/<franchiseId>/branches/<branchId>/products/<productId>
# -> 204 No Content

# 7. Renombrar franquicia / sucursal / producto
curl -X PATCH http://localhost:8080/api/v1/franchises/<franchiseId>/name \
  -H "Content-Type: application/json" -d '{"name":"Nuevo nombre"}'
```

> También hay una colección lista para usar en `docs/api-examples.http`
> (compatible con el HTTP Client de IntelliJ y la extensión REST Client de VS Code).

---

## Cómo ejecutar los tests

```bash
./mvnw clean test
```

La suite cubre dominio, servicio (con el puerto mockeado), mapeo de persistencia y la capa web
(`@WebMvcTest` + MockMvc). Los tests unitarios **no requieren** una instancia de MongoDB.

Además hay:

- **Tests de integración con Testcontainers** (`FranchisePersistenceIntegrationTest`): levantan un
  MongoDB real y verifican el round-trip del agregado, el incremento de `version`, el bloqueo
  optimista (409) y la creación de los índices embebidos. Se **omiten automáticamente** si Docker no
  está disponible (`@Testcontainers(disabledWithoutDocker = true)`), así que `./mvnw test` sigue
  pasando sin Docker.
- **Test de arquitectura con ArchUnit** (`ArchitectureTest`): falla el build si el dominio depende de
  Spring/Mongo o de las capas externas, o si la capa de aplicación alcanza la de infraestructura.

---

## Decisiones técnicas

- **Agregado y embebido.** `Franchise` es la raíz; sucursales y productos se guardan embebidos en
  un único documento. Simplifica la consistencia (una sola escritura por operación) y encaja con
  el tamaño esperado del modelo.
- **Puerto + adaptador.** El servicio depende de `FranchiseRepositoryPort`, no de Spring Data. Esto
  permite testear los casos de uso con un mock y deja la tecnología de persistencia como un detalle.
- **Factorías `create` / `rehydrate`.** `create` genera UUID y aplica invariantes (stock ≥ 0);
  `rehydrate` reconstruye desde Mongo preservando ids y timestamps.
- **`updatedAt` centralizado.** Cualquier mutación (nombre, stock, alta/baja de producto) llama a
  `Franchise.touch()` antes de persistir.
- **Jerarquía `ResourceNotFoundException`.** Un único handler mapea los tres 404.
- **DTOs como `records`** y **constructor injection**; el controller no contiene lógica de negocio.
- **IDs:** la franquicia usa el `ObjectId` que genera Mongo; sucursales y productos usan UUID.
- **Bloqueo optimista (`@Version`).** El agregado lleva una `version` que viaja en el round-trip de
  persistencia; Spring Data condiciona cada actualización a la versión almacenada, de modo que una
  escritura concurrente sobre un agregado desactualizado falla con 409 en vez de pisar el cambio
  (lost update).
- **Índices sobre los ids embebidos** (`branches._id`, `branches.products._id`), declarados en el
  documento raíz. Su creación es *opt-in* por entorno (`MONGODB_AUTO_INDEX=true`) para no forzar una
  conexión a la BD en el arranque; el test de integración los verifica.

---


## Posibles mejoras futuras

- **Escrituras atómicas** sobre el subcampo (`$set`/`$inc` con filtros posicionales) en lugar de
  reescribir el documento completo en cada mutación, para reducir la amplificación de escritura.
- **`top-stock-products` vía aggregation pipeline** (`$unwind` + `$group`/`$sort`) para que el cálculo
  ocurra en Mongo y no cargando el agregado completo en memoria.
- **Paginación/listado de franquicias** con proyecciones.
- **Separación de agregados** (Branch/Product en colecciones propias con referencia) y, eventualmente,
  CQRS + eventos de dominio, cuando un sub-array sea no acotado o de muy alta escritura (rompe el
  techo de 16 MB del documento). Criterio: embebido mientras el tamaño por franquicia sea acotado.
- **Perfiles `dev`/`prod`**, observabilidad (Actuator + Micrometer) y configuración de seguridad.
