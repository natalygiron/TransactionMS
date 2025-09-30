package org.taller01.transactionms.infrastructure.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.taller01.transactionms.domain.port.in.TransactionUseCase;
import org.taller01.transactionms.dto.request.DepositRequest;
import org.taller01.transactionms.dto.request.TransferRequest;
import org.taller01.transactionms.dto.request.WithdrawRequest;
import org.taller01.transactionms.dto.response.TransactionResponse;
import org.taller01.transactionms.infrastructure.mapper.TransactionMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/transacciones")
@RequiredArgsConstructor
public class TransactionController {

  private final TransactionUseCase service; // depende del puerto (no de la impl)
  private final TransactionMapper mapper;

  @PostMapping("/deposito")
  public Mono<TransactionResponse> deposit(@Valid @RequestBody DepositRequest request) {
    return service.deposit(request).map(mapper::toResponse);
  }

  @PostMapping("/retiro")
  public Mono<TransactionResponse> withdraw(@Valid @RequestBody WithdrawRequest request) {
    return service.withdraw(request).map(mapper::toResponse);
  }

  @PostMapping("/transferencia")
  public Mono<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
    return service.transfer(request).map(mapper::toResponse);
  }

  @GetMapping("/historial")
  public Flux<TransactionResponse> history(@RequestParam String accountId) {
    return service.getHistory(accountId).map(mapper::toResponse);
  }
}
