# TransactionMS

Microservicio para la gestión de transacciones bancarias: depósitos, retiros, transferencias y consulta de historial.

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

## ✅ Buenas prácticas aplicadas

- Validación de datos con anotaciones @Valid
- Manejo de errores con GlobalExceptionHandler
- Documentación contract-first con ejemplos
- Separación clara entre DTO, dominio, servicio y controlador
- Registro de transacciones fallidas para trazabilidad

## 🚀 Ejecución local
```bash
mvn clean install
mvn spring-boot:run
```
> Asegúrate de que AccountMS esté corriendo en localhost:8081.
