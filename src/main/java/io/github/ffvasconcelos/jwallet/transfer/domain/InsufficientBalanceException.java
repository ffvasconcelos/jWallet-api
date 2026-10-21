package io.github.ffvasconcelos.jwallet.transfer.domain;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException() {
        super("Source wallet has insufficient balance");
    }
}
