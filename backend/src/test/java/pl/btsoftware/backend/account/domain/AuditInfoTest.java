package pl.btsoftware.backend.account.domain;

import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

class AuditInfoTest {

    @Test
    void shouldCreateAuditInfoFromStringAndUUID() {
        var userId = "user123";
        var groupId = randomUUID();

        var auditInfo = AuditInfo.create(userId, groupId);

        assertThat(auditInfo.who()).isEqualTo(new UserId(userId));
        assertThat(auditInfo.fromGroup()).isEqualTo(new GroupId(groupId));
        assertThat(auditInfo.when()).isNotNull();
        assertThat(auditInfo.when().getOffset()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    void shouldCreateAuditInfoFromUserIdAndGroupId() {
        var userId = new UserId("user123");
        var groupId = new GroupId(randomUUID());

        var auditInfo = AuditInfo.create(userId, groupId);

        assertThat(auditInfo.who()).isEqualTo(userId);
        assertThat(auditInfo.fromGroup()).isEqualTo(groupId);
        assertThat(auditInfo.when()).isNotNull();
        assertThat(auditInfo.when().getOffset()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    void shouldCreateAuditInfoWithSpecificTimestamp() {
        var userId = "user123";
        var groupId = randomUUID();
        var timestamp = OffsetDateTime.of(2024, 1, 15, 10, 30, 0, 0, ZoneOffset.UTC);

        var auditInfo = AuditInfo.create(userId, groupId, timestamp);

        assertThat(auditInfo.who()).isEqualTo(new UserId(userId));
        assertThat(auditInfo.fromGroup()).isEqualTo(new GroupId(groupId));
        assertThat(auditInfo.when()).isEqualTo(timestamp);
    }

    @Test
    void shouldUpdateTimestamp() throws InterruptedException {
        var userId = new UserId("user123");
        var groupId = new GroupId(randomUUID());
        var originalTimestamp = OffsetDateTime.of(2024, 1, 15, 10, 30, 0, 0, ZoneOffset.UTC);
        var auditInfo = new AuditInfo(userId, groupId, originalTimestamp);

        Thread.sleep(10);
        var updatedAuditInfo = auditInfo.updateTimestamp();

        assertThat(updatedAuditInfo.who()).isEqualTo(userId);
        assertThat(updatedAuditInfo.fromGroup()).isEqualTo(groupId);
        assertThat(updatedAuditInfo.when()).isAfter(originalTimestamp);
        assertThat(updatedAuditInfo.when().getOffset()).isEqualTo(ZoneOffset.UTC);
        assertThat(auditInfo.when()).isEqualTo(originalTimestamp);
    }
}
