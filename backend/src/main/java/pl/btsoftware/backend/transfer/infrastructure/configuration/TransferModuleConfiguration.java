package pl.btsoftware.backend.transfer.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.btsoftware.backend.transfer.TransferModuleFacade;
import pl.btsoftware.backend.transfer.application.TransferService;

@Configuration
class TransferModuleConfiguration {

    @Bean
    TransferModuleFacade transferModuleFacade(TransferService transferService) {
        return new TransferModuleFacade(transferService);
    }
}
