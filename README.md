# TransactionMS

Microservicio para la gestión de transacciones bancarias que permite realizar depósitos, retiros, transferencias entre cuentas y consultar el historial de transacciones. Este servicio forma parte de un ecosistema de microservicios bancarios y se integra con el microservicio de cuentas (AccountMS) para validar operaciones.

## 📋 Características principales

- ✅ Gestión completa de transacciones bancarias
- ✅ Validación de fondos suficientes para retiros y transferencias
- ✅ Integración reactiva con AccountMS
- ✅ Registro detallado de todas las operaciones (exitosas y fallidas)
- ✅ API RESTful completamente documentada
- ✅ Manejo robusto de errores y excepciones
- ✅ Validación de datos con anotaciones Spring

## 🏗️ Arquitectura del sistema

### Diagrama de Componentes
![Diagrama de Componentes](src/diagrams/Diagrama%20de%20componentes.png)
*Muestra la estructura interna del microservicio y sus dependencias principales.*

### Diagrama de Flujo
![Diagrama de Flujo](src/diagrams/Diagrama%20de%20flujo.png)
*Ilustra el flujo de procesamiento de las diferentes operaciones de transacciones.*

### Diagrama de Secuencia
![Diagrama de Secuencia](src/diagrams/Diagrama%20de%20secuencia.png)
*Detalla la interacción entre los componentes durante el procesamiento de una transacción.*

## 🛠️ Tecnologías

- Java 17
- Spring Boot + WebFlux
- MongoDB (Reactive)
- Swagger (SpringDoc OpenAPI)
- Postman (colección incluida)

## 📦 Endpoints

| Método | Ruta                          | Descripción                          |
|--------|-------------------------------|--------------------------------------|
| POST   | /transacciones/deposito       | Registrar depósito                   |
| POST   | /transacciones/retiro         | Registrar retiro                     |
| POST   | /transacciones/transferencia  | Registrar transferencia              |
| GET    | /transacciones/historial      | Consultar historial por cuenta       |

## 📄 Documentación

- Swagger UI disponible en: `http://localhost:8082/swagger-ui.html`
- Contrato OpenAPI: `openapi/transaction-ms-openapi.yaml`
- Colección Postman: `postman/transaction-collection.json`

## 🧪 Pruebas

Importa la colección Postman y prueba con cuentas como:

```json
{
  "fromAccountId": "68bd2d02a44f743f92283c1d",
  "toAccountId": "68bd301812736c427ae171ee",
  "amount": 5.0
}
```

## 🔧 Configuración

### Variables de entorno
```properties
# Puerto del servicio
server.port=8082

# Configuración de MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/transactiondb

# URL del microservicio de cuentas
account.service.url=http://localhost:8081
```

### Prerequisitos
- Java 17 o superior
- Maven 3.6+
- MongoDB 4.4+
- AccountMS ejecutándose en puerto 8081

## ✅ Buenas prácticas aplicadas

- Validación de datos con anotaciones @Valid
- Manejo de errores con GlobalExceptionHandler
- Documentación contract-first con ejemplos
- Separación clara entre DTO, dominio, servicio y controlador
- Registro de transacciones fallidas para trazabilidad

## 🚀 Ejecución local

### Opción 1: Maven
```bash
mvn clean install
mvn spring-boot:run
```

### Opción 2: JAR
```bash
mvn clean package
java -jar target/transaction-ms-0.0.1-SNAPSHOT.jar
```

> **Nota importante:** Asegúrate de que AccountMS esté corriendo en localhost:8081 y MongoDB esté disponible antes de iniciar este servicio.

## 📊 Monitoreo y salud

- Health check: `http://localhost:8082/actuator/health`
- Métricas: `http://localhost:8082/actuator/metrics`

## 🤝 Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -m 'Añadir nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

## 📝 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.
