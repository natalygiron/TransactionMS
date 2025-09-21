package org.taller01.transactionms.integration.account;

import java.math.BigDecimal;

public record AccountResponse(String id,String accountNumber,BigDecimal balance,AccountType type,String clientId){}
