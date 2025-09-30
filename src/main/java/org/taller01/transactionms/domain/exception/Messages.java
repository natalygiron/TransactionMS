package org.taller01.transactionms.domain.exception;

public final class Messages {
    private Messages() {}

    public static final String DEPOSIT_SUCCESS = "Depósito realizado con éxito";
    public static final String WITHDRAW_SUCCESS = "Retiro realizado con éxito";
    public static final String TRANSFER_SUCCESS = "Transferencia realizada con éxito";
    public static final String TRANSFER_FAILED = "Error al realizar la transferencia";
    public static final String SAME_ACCOUNT_TRANSFER = "No se puede transferir a la misma cuenta";
    public static final String INSUFFICIENT_BALANCE = "Saldo insuficiente para la transferencia";
}
