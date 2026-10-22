package io.github.ffvasconcelos.jwallet.wallet.api;

import io.github.ffvasconcelos.jwallet.wallet.application.WalletService;
import io.github.ffvasconcelos.jwallet.wallet.domain.Wallet;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/wallets")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> getWallet(@Valid @PathVariable UUID id) {
        Wallet wallet = walletService.getWalletById(id);

        return ResponseEntity.ok().body(new WalletResponse(wallet.id(), wallet.balance()));
    }
}
