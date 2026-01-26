package pl.btsoftware.backend.transfer.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.config.WebConfig;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.transfer.TransferModuleFacade;
import pl.btsoftware.backend.transfer.api.dto.CreateTransferRequest;
import pl.btsoftware.backend.transfer.application.CreateTransferCommand;
import pl.btsoftware.backend.transfer.domain.Transfer;
import pl.btsoftware.backend.transfer.domain.error.TransferDescriptionTooLongException;
import pl.btsoftware.backend.transfer.domain.error.TransferNotFoundException;
import pl.btsoftware.backend.transfer.domain.error.TransferToSameAccountException;
import pl.btsoftware.backend.users.domain.UserId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.btsoftware.backend.shared.Currency.PLN;
import static pl.btsoftware.backend.shared.JwtTokenFixture.createTokenFor;

@WebMvcTest(controllers = TransferController.class)
@Import(WebConfig.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransferModuleFacade transferModuleFacade;

    @Test
    void shouldCreateTransferSuccessfully() throws Exception {
        // given
        var userId = UserId.generate();
        var sourceId = UUID.randomUUID();
        var targetId = UUID.randomUUID();
        var request = new CreateTransferRequest(
                sourceId,
                targetId,
                BigDecimal.valueOf(100),
                null,
                "Test transfer");
        var transfer = createTransfer(new AccountId(sourceId), new AccountId(targetId), BigDecimal.valueOf(100),
                PLN,
                BigDecimal.valueOf(100), PLN, BigDecimal.ONE);

        when(transferModuleFacade.createTransfer(any(CreateTransferCommand.class)))
                .thenReturn(transfer);

        // when & then
        mockMvc.perform(post("/api/transfers")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(createTokenFor(userId.value())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.sourceAccountId").value(sourceId.toString()))
                .andExpect(jsonPath("$.targetAccountId").value(targetId.toString()))
                .andExpect(jsonPath("$.sourceAmount").value(100))
                .andExpect(jsonPath("$.sourceCurrency").value("PLN"))
                .andExpect(jsonPath("$.targetAmount").value(100))
                .andExpect(jsonPath("$.targetCurrency").value("PLN"))
                .andExpect(jsonPath("$.exchangeRate").value(1))
                .andExpect(jsonPath("$.description").value("Test description"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldGetTransfers() throws Exception {
        // given
        var userId = UserId.generate();
        var transfer = createTransfer(AccountId.generate(), AccountId.generate(), BigDecimal.valueOf(100), PLN,
                BigDecimal.valueOf(100), PLN, BigDecimal.ONE);

        when(transferModuleFacade.getTransfers(any(UserId.class)))
                .thenReturn(List.of(transfer));

        // when & then
        mockMvc.perform(get("/api/transfers")
                        .with(createTokenFor(userId.value())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transfers").isArray())
                .andExpect(jsonPath("$.transfers[0].id").value(transfer.id().value().toString()))
                .andExpect(jsonPath("$.transfers[0].sourceAccountId").value(transfer.sourceAccountId().value().toString()))
                .andExpect(jsonPath("$.transfers[0].targetAccountId").value(transfer.targetAccountId().value().toString()))
                .andExpect(jsonPath("$.transfers[0].sourceAmount").value(100))
                .andExpect(jsonPath("$.transfers[0].sourceCurrency").value("PLN"))
                .andExpect(jsonPath("$.transfers[0].targetAmount").value(100))
                .andExpect(jsonPath("$.transfers[0].targetCurrency").value("PLN"))
                .andExpect(jsonPath("$.transfers[0].exchangeRate").value(1))
                .andExpect(jsonPath("$.transfers[0].description").value("Test description"))
                .andExpect(jsonPath("$.transfers[0].createdAt").exists());
    }

    @Test
    void shouldGetTransferById() throws Exception {
        // given
        var userId = UserId.generate();
        var transfer = createTransfer(AccountId.generate(), AccountId.generate(), BigDecimal.valueOf(100), PLN,
                BigDecimal.valueOf(100), PLN, BigDecimal.ONE);

        when(transferModuleFacade.getTransfer(eq(transfer.id()), any(UserId.class)))
                .thenReturn(transfer);

        // when & then
        mockMvc.perform(get("/api/transfers/" + transfer.id().value())
                        .with(createTokenFor(userId.value())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transfer.id().value().toString()))
                .andExpect(jsonPath("$.sourceAccountId").value(transfer.sourceAccountId().value().toString()))
                .andExpect(jsonPath("$.targetAccountId").value(transfer.targetAccountId().value().toString()))
                .andExpect(jsonPath("$.sourceAmount").value(100))
                .andExpect(jsonPath("$.sourceCurrency").value("PLN"))
                .andExpect(jsonPath("$.targetAmount").value(100))
                .andExpect(jsonPath("$.targetCurrency").value("PLN"))
                .andExpect(jsonPath("$.exchangeRate").value(1))
                .andExpect(jsonPath("$.description").value("Test description"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldReturnBadRequestWhenTransferringToSameAccount() throws Exception {
        // given
        var userId = UserId.generate();
        var sourceId = UUID.randomUUID();
        var request = new CreateTransferRequest(
                sourceId,
                sourceId,
                BigDecimal.valueOf(100),
                null,
                "Same account");

        when(transferModuleFacade.createTransfer(any(CreateTransferCommand.class)))
                .thenThrow(new TransferToSameAccountException());

        // when & then
        mockMvc.perform(post("/api/transfers")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(createTokenFor(userId.value())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenTransferDoesNotExist() throws Exception {
        var userId = UserId.generate();
        var transferId = UUID.randomUUID();

        when(transferModuleFacade.getTransfer(any(TransferId.class), any(UserId.class)))
                .thenThrow(new TransferNotFoundException());

        mockMvc.perform(get("/api/transfers/" + transferId)
                        .with(createTokenFor(userId.value())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenDescriptionTooLong() throws Exception {
        var userId = UserId.generate();
        var sourceId = UUID.randomUUID();
        var targetId = UUID.randomUUID();
        var request = new CreateTransferRequest(
                sourceId,
                targetId,
                BigDecimal.valueOf(100),
                null,
                "A".repeat(201));

        // Assuming Validation acts before facade or Facade throws exception.
        // If Facade throws exceptions:
        when(transferModuleFacade.createTransfer(any(CreateTransferCommand.class)))
                .thenThrow(new TransferDescriptionTooLongException());

        mockMvc.perform(post("/api/transfers")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(createTokenFor(userId.value())))
                .andExpect(status().isBadRequest());
    }

    private Transfer createTransfer(AccountId source, AccountId target, BigDecimal sourceAmount,
                                    Currency sourceCurrency, BigDecimal targetAmount,
                                    Currency targetCurrency, BigDecimal rate) {
        return new Transfer(
                TransferId.generate(),
                source,
                target,
                Money.of(sourceAmount, sourceCurrency),
                Money.of(targetAmount, targetCurrency),
                new ExchangeRate(rate, sourceCurrency, targetCurrency),
                "Test description",
                Instancio.create(AuditInfo.class),
                Instancio.create(AuditInfo.class),
                Tombstone.active());
    }
}
