package org.taller01.transactionms.infrastructure.external.account;

import java.math.BigDecimal;

public record AccountResponse(
        String id,
        String accountNumber,
        BigDecimal balance,
        AccountType type,
        String clientId
) {}
