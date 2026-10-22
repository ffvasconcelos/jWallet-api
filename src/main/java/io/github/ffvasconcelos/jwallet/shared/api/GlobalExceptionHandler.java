package io.github.ffvasconcelos.jwallet.shared.api;

import io.github.ffvasconcelos.jwallet.transfer.domain.InsufficientBalanceException;
import io.github.ffvasconcelos.jwallet.transfer.domain.InvalidTransferAmountException;
import io.github.ffvasconcelos.jwallet.transfer.domain.SameWalletTransferException;
import io.github.ffvasconcelos.jwallet.wallet.domain.WalletNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleWalletNotFound(WalletNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({InsufficientBalanceException.class, SameWalletTransferException.class})
    public ResponseEntity<ProblemDetail> handleUnprocessableRequest(RuntimeException exception) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage());
    }

    @ExceptionHandler(InvalidTransferAmountException.class)
    public ResponseEntity<ProblemDetail> handleInvalidAmount(InvalidTransferAmountException exception) {
        return problem(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);

        return ResponseEntity.status(status).body(problemDetail);
    }
}
