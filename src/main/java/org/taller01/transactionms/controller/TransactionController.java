package org.taller01.transactionms.controller;

import org.taller01.transactionms.dto.request.DepositRequest;
import org.taller01.transactionms.dto.request.TransferRequest;
import org.taller01.transactionms.dto.request.WithdrawRequest;
import org.taller01.transactionms.dto.response.TransactionResponse;
import org.taller01.transactionms.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transacciones")
public class TransactionController {

    private final TransactionService service;

    /** POST /transacciones/deposito — registrar depósito */
    @Operation(summary = "Registrar depósito", description = "Registra un depósito en una cuenta existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Depósito exitoso",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/deposito")
    public Mono<TransactionResponse> deposit(@Valid @RequestBody DepositRequest req) {
        return service.deposit(req).map(TransactionResponse::from);
    }

    /** POST /transacciones/retiro — registrar retiro */
    @Operation(summary = "Registrar retiro", description = "Registra un retiro desde una cuenta existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retiro exitoso",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "409", description = "Saldo insuficiente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/retiro")
    public Mono<TransactionResponse> withdraw(@Valid @RequestBody WithdrawRequest req) {
        return service.withdraw(req).map(TransactionResponse::from);
    }

    /** POST /transacciones/transferencia — registrar transferencia */
    @Operation(summary = "Registrar transferencia", description = "Transfiere dinero entre dos cuentas válidas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transferencia exitosa",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "409", description = "Saldo insuficiente o cuentas iguales"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/transferencia")
    public Mono<TransactionResponse> transfer(@Valid @RequestBody TransferRequest req) {
        return service.transfer(req).map(TransactionResponse::from);
    }

    /** GET /transacciones/historial?accountId=... — historial por cuenta (origen o destino) */
    @Operation(summary = "Consultar historial de transacciones", description = "Devuelve todas las transacciones donde la cuenta aparece como origen o destino")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial obtenido",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parámetro cuentaId faltante"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/historial")
    public Flux<TransactionResponse> history(@RequestParam String cuentaId) {
        return service.getHistory(cuentaId);
    }
}
