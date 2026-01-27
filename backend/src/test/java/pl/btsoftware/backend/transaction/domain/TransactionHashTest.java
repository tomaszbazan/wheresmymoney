package pl.btsoftware.backend.transaction.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.transaction.domain.error.TransactionHashInvalidException;

class TransactionHashTest {

    @Test
    void shouldThrowExceptionWhenHashIsNull() {
        // given & when & then
        assertThatThrownBy(() -> new TransactionHash(null))
                .isInstanceOf(TransactionHashInvalidException.class);
    }

    @Test
    void shouldThrowExceptionWhenHashIsNotHexadecimal() {
        // given & when & then
        assertThatThrownBy(() -> new TransactionHash("not-a-hash"))
                .isInstanceOf(TransactionHashInvalidException.class);
    }

    @Test
    void shouldThrowExceptionWhenHashIsTooShort() {
        // given & when & then
        assertThatThrownBy(() -> new TransactionHash("a".repeat(63)))
                .isInstanceOf(TransactionHashInvalidException.class);
    }

    @Test
    void shouldThrowExceptionWhenHashIsTooLong() {
        // given & when & then
        assertThatThrownBy(() -> new TransactionHash("a".repeat(65)))
                .isInstanceOf(TransactionHashInvalidException.class);
    }

    @Test
    void shouldThrowExceptionWhenHashContainsUppercaseLetters() {
        // given & when & then
        assertThatThrownBy(() -> new TransactionHash("A".repeat(64)))
                .isInstanceOf(TransactionHashInvalidException.class);
    }

    @Test
    void shouldCreateTransactionHashWhenValidHashProvided() {
        // given
        var validHash = "a1b2c3d4e5f67890".repeat(4);

        // when
        var hash = new TransactionHash(validHash);

        // then
        assertThat(hash.value()).isEqualTo(validHash);
    }

    @Test
    void shouldSupportEqualitySemantics() {
        // given
        var hashValue = "a1b2c3d4e5f67890".repeat(4);

        // when
        var hash1 = new TransactionHash(hashValue);
        var hash2 = new TransactionHash(hashValue);

        // then
        assertThat(hash1).isEqualTo(hash2);
    }
}
