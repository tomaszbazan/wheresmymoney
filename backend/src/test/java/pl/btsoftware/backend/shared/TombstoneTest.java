package pl.btsoftware.backend.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class TombstoneTest {

    @Test
    void shouldCreateActiveTombstone() {
        var tombstone = Tombstone.active();

        assertThat(tombstone.isDeleted()).isFalse();
        assertThat(tombstone.deletedAt()).isNull();
        assertThat(tombstone.isActive()).isTrue();
    }

    @Test
    void shouldCreateDeletedTombstone() {
        var tombstone = Tombstone.deleted();

        assertThat(tombstone.isDeleted()).isTrue();
        assertThat(tombstone.deletedAt()).isNotNull();
        assertThat(tombstone.deletedAt().getOffset()).isEqualTo(ZoneOffset.UTC);
        assertThat(tombstone.isActive()).isFalse();
    }

    @Test
    void shouldReturnTrueForIsActiveWhenNotDeleted() {
        var tombstone = new Tombstone(false, null);

        assertThat(tombstone.isActive()).isTrue();
    }

    @Test
    void shouldReturnFalseForIsActiveWhenDeleted() {
        var tombstone = Tombstone.deleted();

        assertThat(tombstone.isActive()).isFalse();
    }
}
