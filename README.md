# TransactionMS

## Descripción breve del microservicio

Microservicio para la gestión de transacciones bancarias que permite realizar depósitos, retiros, transferencias entre cuentas y consultar el historial de transacciones. Este servicio forma parte de un ecosistema de microservicios bancarios que incluye **CustomerMS** (gestión de clientes), **AccountMS** (gestión de cuentas) y **TransactionMS** (gestión de transacciones). Se integra de forma reactiva con AccountMS para validar operaciones y mantener la consistencia de datos en el sistema bancario distribuido.

## Arquitectura del sistema

### Diagrama de Componentes
![Diagrama de Componentes](src/diagrams/Diagrama%20de%20componentes.png)

**Descripción:** Representa la arquitectura completa del ecosistema de microservicios bancarios, mostrando los tres servicios principales: **CustomerMS** (gestión de clientes), **AccountMS** (gestión de cuentas) y **TransactionMS** (gestión de transacciones). El diagrama ilustra las dependencias entre servicios, donde TransactionMS se comunica de forma reactiva con AccountMS para validar operaciones, y AccountMS mantiene la relación con CustomerMS para la gestión de propietarios de cuentas. Cada servicio mantiene su propia base de datos MongoDB siguiendo el patrón Database per Service.

### Diagrama de Secuencia  
![Diagrama de Secuencia](src/diagrams/Diagra%20de%20secuencia.png)

**Descripción:** Ilustra la interacción temporal detallada entre los componentes de los tres microservicios durante el procesamiento de una transacción. Muestra la secuencia de llamadas desde el cliente hacia TransactionMS, las comunicaciones reactivas (WebClient) con AccountMS para validaciones y actualizaciones de saldo, y cómo se mantiene la consistencia de datos entre los servicios. El diagrama incluye los escenarios de éxito y fallo, mostrando cómo se propagan los errores y se mantiene la integridad transaccional en el ecosistema distribuido.

### Diagrama de Flujo
![Diagrama de Flujo](src/diagrams/Diagrama%20de%20flujo.png)

**Descripción:** Detalla el flujo completo de procesamiento de transacciones bancarias dentro del ecosistema de los tres microservicios. Muestra cómo TransactionMS orquesta las operaciones de dep��sito, retiro y transferencia, incluyendo las validaciones necesarias con AccountMS (verificación de existencia de cuentas y fondos suficientes), la actualización de saldos, y el registro de la transacción. El diagrama también representa los puntos de decisión para el manejo de errores y los diferentes caminos según el tipo de operación.

## Lista de Endpoints

| Método | Endpoint | Descripción | Request Body | Respuesta |
|--------|----------|-------------|--------------|-----------|
| **POST** | `/transacciones/deposito` | Registra un depósito en una cuenta existente | `DepositRequest` | `TransactionResponse` |
| **POST** | `/transacciones/retiro` | Registra un retiro desde una cuenta existente | `WithdrawRequest` | `TransactionResponse` |
| **POST** | `/transacciones/transferencia` | Transfiere dinero entre dos cuentas válidas | `TransferRequest` | `TransactionResponse` |
| **GET** | `/transacciones/historial?cuentaId={id}` | Consulta historial de transacciones por cuenta | - | `Array<TransactionResponse>` |

### Esquemas de Request

**DepositRequest:**
```json
{
  "accountId": "string",
  "amount": "number (min: 0.01)"
}
```

**WithdrawRequest:**
```json
{
  "accountId": "string", 
  "amount": "number (min: 0.01)"
}
```

**TransferRequest:**
```json
{
  "fromAccountId": "string",
  "toAccountId": "string",
  "amount": "number (min: 0.01)"
}
```

**TransactionResponse:**
```json
{
  "id": "string",
  "type": "DEPOSIT | WITHDRAWAL | TRANSFER",
  "status": "SUCCESS | FAILED",
  "fromAccountId": "string | null",
  "toAccountId": "string | null", 
  "amount": "number",
  "createdAt": "ISO 8601 datetime",
  "message": "string"
}
```

## Reglas de Negocio

### Depósitos
- ✅ El monto debe ser mayor a 0.01
- ✅ La cuenta destino debe existir en AccountMS
- ✅ Se registra la transacción independientemente del resultado
- ✅ El saldo se actualiza automáticamente en AccountMS

### Retiros
- ✅ El monto debe ser mayor a 0.01
- ✅ La cuenta origen debe existir en AccountMS
- ✅ La cuenta debe tener saldo suficiente
- ✅ Se registra la transacción independientemente del resultado
- ✅ El saldo se actualiza automáticamente en AccountMS

### Transferencias
- ✅ El monto debe ser mayor a 0.01
- ✅ Las cuentas origen y destino deben existir en AccountMS
- ✅ Las cuentas origen y destino deben ser diferentes
- ✅ La cuenta origen debe tener saldo suficiente
- ✅ Se ejecuta como operación atómica (retiro + depósito)
- ✅ Se registra la transacción independientemente del resultado
- ✅ Los saldos se actualizan automáticamente en AccountMS

### Historial
- ✅ Devuelve todas las transacciones donde la cuenta aparece como origen o destino
- ✅ Incluye transacciones exitosas y fallidas
- ✅ Ordenadas por fecha de creación

### Integración con AccountMS
- ✅ Validación reactiva de existencia de cuentas
- ✅ Verificación de saldos antes de operaciones
- ✅ Actualización automática de saldos
- ✅ Manejo de errores y propagación de excepciones
- ✅ Registro de transacciones fallidas para trazabilidad

## Documentación

### Swagger UI
- **URL:** `http://localhost:8082/swagger-ui.html`
- **Descripción:** Interfaz interactiva para probar todos los endpoints
- **Incluye:** Ejemplos de request/response, validaciones y códigos de error

### OpenAPI Specification
- **Archivo:** `src/main/resources/openapi/transaction-ms-openapi.yaml`
- **Versión:** OpenAPI 3.0.3
- **Incluye:** Contratos completos, esquemas, ejemplos y documentación detallada

### Postman Collection
- **Archivo:** `postman/transaction-collection.json`
- **Incluye:** Todas las operaciones con ejemplos funcionales
- **Variables:** Preconfiguradas para pruebas locales

#### Ejemplos de prueba:
```json
// Depósito
{
  "accountId": "68bd301812736c427ae171ee",
  "amount": 100.0
}

// Retiro  
{
  "accountId": "68bd301812736c427ae171ee",
  "amount": 50.0
}

// Transferencia
{
  "fromAccountId": "68bd2d02a44f743f92283c1d",
  "toAccountId": "68bd301812736c427ae171ee", 
  "amount": 5.0
}
```

## Testing

### Checkstyle
- **Archivo:** `src/main/resources/checkstyle.xml`
- **Configuración:** Estándares de codificación Java
- **Reglas aplicadas:**
  - Validación de nomenclatura de paquetes: `org.taller01.transactionms`
  - Nombres de tipos, métodos y variables locales
  - Longitud máxima de métodos: 50 líneas
  - Eliminación de imports no utilizados
  - Orden alfabético de imports con separación

### Validación de código
- **Ejecución:** `mvn checkstyle:check`
- **Integración:** Validación automática en build

### Formato y estilo
- **Convenciones:** Google Java Style Guide adaptado
- **Validaciones:** Tabs, nueva línea al final de archivos
- **Imports:** Ordenados y agrupados correctamente

### Configuración de testing
```xml
<module name="MethodLength">
    <property name="max" value="50" />
</module>
<module name="PackageName">
    <property name="format" value="org\.taller01\.transactionms(\..+)?" />
</module>
```

## Tecnologías utilizadas

- **Java 17** - Lenguaje de programación
- **Spring Boot 3.x** - Framework principal
- **Spring WebFlux** - Programación reactiva
- **MongoDB Reactive** - Base de datos NoSQL
- **SpringDoc OpenAPI** - Documentación automática
- **WebClient** - Cliente HTTP reactivo para integración
- **Maven** - Gestión de dependencias
- **Lombok** - Reducción de código boilerplate

## Configuración del entorno

### Prerequisites
- Java 17+
- Maven 3.6+
- MongoDB 4.4+
- AccountMS ejecutándose en puerto 8081

### Variables de entorno
```properties
# Puerto del servicio
server.port=8082

# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/transactiondb

# AccountMS integration
account.service.url=http://localhost:8081
```

### Ejecución
```bash
# Compilar y ejecutar
mvn clean install
mvn spring-boot:run

# O con JAR
mvn clean package
java -jar target/transaction-ms-0.0.1-SNAPSHOT.jar
```

### Health Check
- **URL:** `http://localhost:8082/actuator/health`
- **Métricas:** `http://localhost:8082/actuator/metrics`
