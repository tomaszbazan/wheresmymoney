package pl.btsoftware.backend.csvimport.application;

import java.io.InputStream;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.users.domain.UserId;

public record ParseCsvCommand(InputStream csvFile, UserId userId, AccountId accountId) {}
