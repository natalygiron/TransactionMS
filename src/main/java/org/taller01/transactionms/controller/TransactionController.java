package org.taller01.transactionms.controller;

import org.taller01.transactionms.dto.request.DepositRequest;
import org.taller01.transactionms.dto.request.TransferRequest;
import org.taller01.transactionms.dto.request.WithdrawRequest;
import org.taller01.transactionms.dto.response.TransactionResponse;
import org.taller01.transactionms.mapper.TransactionMapper;
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
  private final TransactionMapper mapper;

  @PostMapping("/deposito")
  public Mono<TransactionResponse> deposit(@Valid @RequestBody DepositRequest req) {
    return service.deposit(req).map(mapper::toResponse);
  }

  @PostMapping("/retiro")
  public Mono<TransactionResponse> withdraw(@Valid @RequestBody WithdrawRequest req) {
    return service.withdraw(req).map(mapper::toResponse);
  }

  @PostMapping("/transferencia")
  public Mono<TransactionResponse> transfer(@Valid @RequestBody TransferRequest req) {
    return service.transfer(req).map(mapper::toResponse);
  }

  @GetMapping("/historial")
  public Flux<TransactionResponse> history(@RequestParam String accountId) {
    return service.getHistory(accountId).map(mapper::toResponse);
  }
}
