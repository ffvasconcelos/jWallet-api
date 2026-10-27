package io.github.ffvasconcelos.jwallet.transfer.api;

import io.github.ffvasconcelos.jwallet.transfer.application.TransferCommand;
import io.github.ffvasconcelos.jwallet.transfer.application.TransferService;
import io.github.ffvasconcelos.jwallet.transfer.domain.Transfer;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/transfers")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping
    public ResponseEntity<List<TransferResponse>> listTransfers() {
        List<TransferResponse> response = new ArrayList<>();
        transferService.getTransfers().forEach(transfer -> response.add(toResponse(transfer)));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponse> getTransfer(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(transferService.findTransfer(id)));
    }

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody CreateTransferRequest transfer) {
        TransferCommand command = new TransferCommand(
                transfer.sourceWalletId(),
                transfer.targetWalletId(),
                transfer.amount()
        );

        Transfer transferFinished = this.transferService.transfer(command);

        TransferResponse response = toResponse(transferFinished);

        URI location = URI.create("/transfers/" + transferFinished.id());
        return ResponseEntity.created(location).body(response);
    }

    private TransferResponse toResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.id(),
                transfer.sourceWalletId(),
                transfer.targetWalletId(),
                transfer.amount(),
                transfer.status(),
                transfer.createdAt()
        );
    }

}
