package pl.btsoftware.backend.csvimport.application;

import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.users.domain.UserId;

import java.io.InputStream;

public record ParseCsvCommand(
        InputStream csvFile,
        UserId userId,
        AccountId accountId
) {
}
