package org.taller01.transactionms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record WithdrawRequest(@NotBlank String accountId,@NotNull @Positive BigDecimal amount){}
