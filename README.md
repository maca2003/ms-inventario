# ms-inventario

Microservicio de **inventario**: stock por producto, lotes con fecha de vencimiento y movimientos (entradas y salidas). Forma parte de la solución de microservicios (MS-AUTH, MS-PRODUCTOS, MS-VENTAS, MS-INVENTARIO y API Gateway).

El descuento de stock usa **bloqueo pesimista** (`SELECT ... FOR UPDATE`) para que dos ventas simultáneas no dejen el stock en negativo.

## Tecnologías

- Java 21
- Spring Boot 3.5 (Web, Data JPA, Validation)
- MySQL 8
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
