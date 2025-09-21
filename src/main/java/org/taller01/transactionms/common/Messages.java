package org.taller01.transactionms.common;

public final class Messages {

    private Messages() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ==== Éxitos ====
    public static final String DEPOSIT_SUCCESS = "Depósito aplicado";
    public static final String WITHDRAW_SUCCESS = "Retiro aplicado";
    public static final String TRANSFER_SUCCESS = "Transferencia aplicada";

    // ==== Errores ====
    public static final String SAME_ACCOUNT_TRANSFER = "No se puede transferir entre la misma cuenta";
    public static final String INSUFFICIENT_BALANCE = "Saldo insuficiente en la cuenta de origen";
}