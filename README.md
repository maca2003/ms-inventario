# ms-inventario

Microservicio de **inventario**: stock por producto, lotes con fecha de vencimiento y movimientos (entradas y salidas). Forma parte de la solución de microservicios (MS-AUTH, MS-PRODUCTOS, MS-VENTAS, MS-INVENTARIO y API Gateway).

El descuento de stock usa **bloqueo pesimista** (`SELECT ... FOR UPDATE`) para que dos ventas simultáneas no dejen el stock en negativo.

## Tecnologías

- Java 21
- Spring Boot 3.5 (Web, Data JPA, Validation)
- MySQL 8
- AWS SDK v2 (Amazon SQS)
- Maven
- JUnit 5 + Mockito

## Estructura

```
src/main/java/cl/duoc/msinventario
├── controller   -> Endpoints REST
├── service      -> Lógica de negocio
├── repository   -> Acceso a datos (Spring Data JPA)
├── model        -> Entidades JPA (Inventario 1 ── * Lote, Inventario 1 ── * Movimiento)
├── dto          -> Objetos de entrada y salida
├── messaging    -> Productor de la cola SQS de alertas
└── exception    -> Manejo global de errores
```

## Requisitos

- Java 21
- Maven 3.9+
- MySQL 8 corriendo en `localhost:3306`

## Cómo ejecutar

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/maca2003/ms-inventario.git
   cd ms-inventario
   ```
2. Revisar el usuario y contraseña de MySQL en `src/main/resources/application.properties` (por defecto `root` / `root`). La base de datos `inventario_db` se crea automáticamente.
3. Compilar, ejecutar pruebas y empaquetar:
   ```bash
   mvn clean install
   ```
4. Ejecutar:
   ```bash
   java -jar target/ms-inventario-1.0.0.jar
   ```
   o bien `mvn spring-boot:run`.

El servicio queda disponible en `http://localhost:8084`.

## Alertas de stock bajo (Amazon SQS)

Cuando una salida de stock deja un producto con `stockActual <= stockMinimo`, el servicio publica un mensaje en la cola **Amazon SQS `inventario-alertas`** (productor). El mensaje se envía **después** de confirmar la transacción y una falla de la cola no impide la venta.

```json
{ "productoId": 1, "stockActual": 5, "stockMinimo": 5, "fecha": "2026-11-21T10:00:00" }
```

La cola la consume la función serverless [fn-alertas-inventario](https://github.com/maca2003/fn-alertas-inventario).

| Variable | Descripción |
|---|---|
| `SQS_ALERTAS_URL` | URL de la cola. Si está vacía (ejecución local), las alertas solo se registran en el log |
| `AWS_REGION` | Región de AWS (por defecto `us-east-1`) |

En el clúster, las credenciales de AWS se obtienen del rol de la instancia EC2 (`LabInstanceProfile`).

## Docker

El `Dockerfile` usa dos etapas:

1. **Compilación** (`maven:3.9.9-eclipse-temurin-21`): descarga las dependencias (en una capa separada para aprovechar la caché) y genera el `.jar`.
2. **Ejecución** (`eclipse-temurin:21-jre-alpine`): imagen liviana solo con el JRE, usuario sin privilegios (uid 1001), `HEALTHCHECK` y la JVM limitada al 75% de la memoria del contenedor.

```bash
docker build -t ms-inventario:1.0.0 .
docker run -p 8084:8084 -e DB_HOST=host.docker.internal -e DB_PASSWORD=root --name ms-inventario ms-inventario:1.0.0
```

`DB_HOST=host.docker.internal` permite al contenedor usar el MySQL instalado en tu computador. Para levantar el sistema completo (todos los servicios + MySQL) usa el `docker-compose.yml` del repositorio [infraestructura-swarm](https://github.com/maca2003/infraestructura-swarm).

## CI/CD (GitHub Actions)

Archivo `.github/workflows/ci-cd.yml`. Se ejecuta en cada push o pull request a `main` y `develop`:

| Etapa | Ramas | Qué hace |
|---|---|---|
| Build y test | todas | `mvn clean verify` (compila, ejecuta pruebas y genera el `.jar`) |
| Provisionar Amazon ECR | `main` | Crea el repositorio de imágenes `ms-inventario` en ECR si no existe |
| Construir y publicar imagen | `main` | `docker build` y `docker push` a ECR con etiquetas `<commit>` y `latest` |
| Desplegar en Docker Swarm | `main` | Por SSH al manager: `docker service update --image ... sistema_ms-inventario` y verifica que `/api/inventario` responda 200 a través del API Gateway |

Secrets requeridos (Settings → Secrets and variables → Actions):

| Secret | Valor |
|---|---|
| `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN` | Learner Lab → AWS Details → AWS CLI (cambian en cada sesión) |
| `EC2_SSH_KEY` | Contenido de `labsuser.pem` |
| `EC2_HOST` | IP pública del manager (salida `IpPublicaManager` del stack `cluster-swarm`) |

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/inventario` | Listar inventarios |
| GET | `/api/inventario/{id}` | Obtener inventario |
| GET | `/api/inventario/producto/{productoId}` | Obtener inventario de un producto |
| POST | `/api/inventario` | Crear inventario para un producto |
| PUT | `/api/inventario/{id}` | Actualizar inventario |
| DELETE | `/api/inventario/{id}` | Eliminar inventario |
| POST | `/api/inventario/{id}/lotes` | Ingresar un lote (suma stock) |
| GET | `/api/inventario/{id}/lotes` | Listar lotes ordenados por vencimiento |
| GET | `/api/inventario/{id}/movimientos` | Listar movimientos |
| POST | `/api/inventario/producto/{productoId}/salida` | Descontar stock (lo usa MS-VENTAS) |
| POST | `/api/inventario/producto/{productoId}/entrada` | Devolver stock (lo usa MS-VENTAS) |

### Ejemplos de cuerpo

Crear inventario:
```json
{ "productoId": 1, "stockMinimo": 5 }
```

Ingresar lote:
```json
{ "codigo": "L-001", "cantidad": 50, "fechaVencimiento": "2027-03-31" }
```

Salida de stock:
```json
{ "cantidad": 3, "motivo": "Venta" }
```

## Respuestas de error

| Código | Caso |
|---|---|
| 400 | Datos inválidos (cantidad 0, fecha vencida, etc.) |
| 404 | Inventario no encontrado |
| 409 | Stock insuficiente o inventario duplicado para el producto |
