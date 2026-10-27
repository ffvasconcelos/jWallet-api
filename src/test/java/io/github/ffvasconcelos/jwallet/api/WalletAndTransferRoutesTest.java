package io.github.ffvasconcelos.jwallet.api;

import io.github.ffvasconcelos.jwallet.wallet.application.WalletService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// No surrounding test transaction: exercise the real HTTP and persistence boundaries.
@SpringBootTest
@AutoConfigureMockMvc
class WalletAndTransferRoutesTest {

    @Autowired private MockMvc mvc;
    @Autowired private WalletService walletService;
    @Autowired private JdbcTemplate jdbc;

    private final Set<UUID> walletIds = new HashSet<>();

    @AfterEach
    void cleanUp() {
        for (UUID id : walletIds) {
            jdbc.update("DELETE FROM transfers WHERE source_wallet_id = ? OR target_wallet_id = ?", id, id);
        }
        for (UUID id : walletIds) {
            jdbc.update("DELETE FROM wallets WHERE id = ?", id);
        }
    }

    @Test
    void createsWalletAndReturnsItsQueryableLocation() throws Exception {
        MvcResult result = mvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"balance\":100.50}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.balance").value(100.50))
                .andReturn();
        String location = result.getResponse().getHeader("Location");
        assertNotNull(location);
        UUID id = UUID.fromString(location.substring("/wallets/".length()));
        walletIds.add(id);

        mvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(100.50));
        assertEquals(0, new BigDecimal("100.50").compareTo(jdbc.queryForObject(
                "SELECT balance FROM wallets WHERE id = ?", BigDecimal.class, id)));
    }

    @Test
    void createsWalletWithZeroBalance() throws Exception {
        MvcResult result = mvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"balance\":0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balance").value(0))
                .andReturn();
        String location = result.getResponse().getHeader("Location");
        assertNotNull(location);
        walletIds.add(UUID.fromString(location.substring("/wallets/".length())));
    }

    @Test
    void rejectsInvalidWalletBalancesWithFieldErrors() throws Exception {
        for (String body : new String[]{"{}", "{\"balance\":-1}", "{\"balance\":1.001}",
                "{\"balance\":100000000000000000}"}) {
            mvc.perform(post("/wallets").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.errors.balance").exists());
        }
    }

    @Test
    void retrievesPersistedTransferWithWalletIdsAndTimestamp() throws Exception {
        UUID sourceId = createWallet("100.00");
        UUID targetId = createWallet("20.00");
        MvcResult created = mvc.perform(post("/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourceWalletId":"%s","targetWalletId":"%s","amount":25.50}
                                """.formatted(sourceId, targetId)))
                .andExpect(status().isCreated()).andReturn();
        String location = created.getResponse().getHeader("Location");
        assertNotNull(location);
        String transferId = location.substring("/transfers/".length());

        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transferId))
                .andExpect(jsonPath("$.sourceWalletId").value(sourceId.toString()))
                .andExpect(jsonPath("$.targetWalletId").value(targetId.toString()))
                .andExpect(jsonPath("$.amount").value(25.50))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void returnsProblemDetailForMissingTransfer() throws Exception {
        mvc.perform(get("/transfers/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void rejectsMalformedTransferId() throws Exception {
        mvc.perform(get("/transfers/not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void listsWalletsUsingResponseDtos() throws Exception {
        UUID firstId = createWallet("100.00");
        UUID secondId = createWallet("0.00");

        mvc.perform(get("/wallets"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].id").value(hasItem(firstId.toString())))
                .andExpect(jsonPath("$[*].id").value(hasItem(secondId.toString())))
                .andExpect(jsonPath("$[?(@.id == '%s')].balance", firstId).value(hasItem(100.0)))
                .andExpect(jsonPath("$[?(@.id == '%s')].balance", secondId).value(hasItem(0.0)));
    }

    @Test
    void listsTransfersWithWalletIdsOutsidePersistenceSession() throws Exception {
        UUID sourceId = createWallet("100.00");
        UUID targetId = createWallet("20.00");
        MvcResult created = mvc.perform(post("/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourceWalletId":"%s","targetWalletId":"%s","amount":25.50}
                                """.formatted(sourceId, targetId)))
                .andExpect(status().isCreated()).andReturn();
        String location = created.getResponse().getHeader("Location");
        assertNotNull(location);
        String transferId = location.substring("/transfers/".length());

        mvc.perform(get("/transfers"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.id == '%s')].sourceWalletId", transferId)
                        .value(hasItem(sourceId.toString())))
                .andExpect(jsonPath("$[?(@.id == '%s')].targetWalletId", transferId)
                        .value(hasItem(targetId.toString())))
                .andExpect(jsonPath("$[?(@.id == '%s')].amount", transferId).value(hasItem(25.50)))
                .andExpect(jsonPath("$[?(@.id == '%s')].status", transferId).value(hasItem("COMPLETED")))
                .andExpect(jsonPath("$[?(@.id == '%s')].createdAt", transferId).isNotEmpty())
                .andExpect(jsonPath("$[*].sourceWallet").isEmpty())
                .andExpect(jsonPath("$[*].targetWallet").isEmpty());
    }

    private UUID createWallet(String balance) {
        UUID id = UUID.randomUUID();
        walletIds.add(id);
        walletService.createWallet(id, new BigDecimal(balance));
        return id;
    }
}
