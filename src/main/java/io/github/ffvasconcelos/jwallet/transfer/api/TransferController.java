package io.github.ffvasconcelos.jwallet.transfer.api;

import io.github.ffvasconcelos.jwallet.transfer.application.TransferCommand;
import io.github.ffvasconcelos.jwallet.transfer.application.TransferService;
import io.github.ffvasconcelos.jwallet.transfer.domain.Transfer;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/transfers")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping()
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody CreateTransferRequest transfer) {
        TransferCommand command = new TransferCommand(
                transfer.sourceWalletId(),
                transfer.targetWalletId(),
                transfer.amount()
        );

        Transfer transferFinished = this.transferService.transfer(command);

        TransferResponse response = new TransferResponse(
                transferFinished.id(),
                transferFinished.sourceWalletId(),
                transferFinished.targetWalletId(),
                transferFinished.amount(),
                transferFinished.status(),
                transferFinished.createdAt()
        );

        URI location = URI.create("/transfers/" + transferFinished.id());
        return ResponseEntity.created(location).body(response);
    }
}
