package io.github.ffvasconcelos.jwallet.wallet.infrastructure;

import io.github.ffvasconcelos.jwallet.wallet.domain.Wallet;
import io.github.ffvasconcelos.jwallet.wallet.domain.WalletRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.UUID;

@Configuration
@Profile("demo")
public class DemoWalletDataConfiguration {

    private static final UUID SOURCE_WALLET_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TARGET_WALLET_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Bean
    CommandLineRunner loadDemoWallets(WalletRepository walletRepository) {
        return arguments -> {
            walletRepository.save(new Wallet(SOURCE_WALLET_ID, new BigDecimal("1000.00")));
            walletRepository.save(new Wallet(TARGET_WALLET_ID, new BigDecimal("250.00")));
        };
    }
}
