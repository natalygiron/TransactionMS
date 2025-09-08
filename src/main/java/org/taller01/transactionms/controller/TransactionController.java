package org.taller01.transactionms.controller;

import org.taller01.transactionms.dto.request.DepositRequest;
import org.taller01.transactionms.dto.request.TransferRequest;
import org.taller01.transactionms.dto.request.WithdrawRequest;
import org.taller01.transactionms.dto.response.TransactionResponse;
import org.taller01.transactionms.service.TransactionService;
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
    @PostMapping("/deposito")
    public Mono<TransactionResponse> depositar(@Valid @RequestBody DepositRequest req) {
        return service.deposit(req).map(TransactionResponse::from);
    }

    /** POST /transacciones/retiro — registrar retiro */
    @PostMapping("/retiro")
    public Mono<TransactionResponse> retirar(@Valid @RequestBody WithdrawRequest req) {
        return service.withdraw(req).map(TransactionResponse::from);
    }

    /** POST /transacciones/transferencia — registrar transferencia */
    @PostMapping("/transferencia")
    public Mono<TransactionResponse> transferir(@Valid @RequestBody TransferRequest req) {
        return service.transfer(req).map(TransactionResponse::from);
    }

    /** GET /transacciones/historial?accountId=... — historial por cuenta (origen o destino) */
    @GetMapping("/historial")
    public Flux<TransactionResponse> historial(@RequestParam String accountId) {
        return service.history(accountId).map(TransactionResponse::from);
    }
}
