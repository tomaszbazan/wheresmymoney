package pl.btsoftware.backend.transaction.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionHashTest {

    @Test
    void shouldThrowExceptionWhenHashIsNull() {
        // given & when & then
        assertThatThrownBy(() -> new TransactionHash(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenHashIsNotHexadecimal() {
        // given & when & then
        assertThatThrownBy(() -> new TransactionHash("not-a-hash"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenHashIsTooShort() {
        // given & when & then
        assertThatThrownBy(() -> new TransactionHash("a".repeat(63)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenHashIsTooLong() {
        // given & when & then
        assertThatThrownBy(() -> new TransactionHash("a".repeat(65)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenHashContainsUppercaseLetters() {
        // given & when & then
        assertThatThrownBy(() -> new TransactionHash("A".repeat(64)))
                .isInstanceOf(IllegalArgumentException.class);
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
