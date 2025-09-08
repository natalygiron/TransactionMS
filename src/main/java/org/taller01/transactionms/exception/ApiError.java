package org.taller01.transactionms.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Estructura estándar de error en TransactionMS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {
  @Schema(example = "2025-09-07T10:15:30Z")
  private Instant timestamp;

  @Schema(example = "400")
  private int status;

  @Schema(example = "Solicitud incorrecta")
  private String error;

  @Schema(example = "Hay errores de validación en el cuerpo enviado")
  private String message;

  @Schema(example = "/transacciones/transferencia")
  private String path;

  // Mapa campo → mensaje (validaciones Bean Validation)
  private Map<String, String> fieldErrors;
}
