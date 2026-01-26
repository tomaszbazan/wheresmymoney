package pl.btsoftware.backend.shared.pagination;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationValidatorTest {

    private final PaginationValidator validator = new PaginationValidator();

    @Test
    void shouldReturnOriginalSizeWhenBelowMaximum() {
        // given
        int requestedSize = 50;

        // when
        int validatedSize = validator.validatePageSize(requestedSize);

        // then
        assertThat(validatedSize).isEqualTo(50);
    }

    @Test
    void shouldReturnOriginalSizeWhenEqualToMaximum() {
        // given
        int requestedSize = 100;

        // when
        int validatedSize = validator.validatePageSize(requestedSize);

        // then
        assertThat(validatedSize).isEqualTo(100);
    }

    @Test
    void shouldReturnMaximumSizeWhenAboveMaximum() {
        // given
        int requestedSize = 150;

        // when
        int validatedSize = validator.validatePageSize(requestedSize);

        // then
        assertThat(validatedSize).isEqualTo(100);
    }

    @Test
    void shouldReturnMaximumSizeForExtremelyLargeValues() {
        // given
        int requestedSize = Integer.MAX_VALUE;

        // when
        int validatedSize = validator.validatePageSize(requestedSize);

        // then
        assertThat(validatedSize).isEqualTo(100);
    }
}
