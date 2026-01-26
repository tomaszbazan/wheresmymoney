package pl.btsoftware.backend.shared.pagination;

import org.springframework.stereotype.Component;

@Component
public class PaginationValidator {
    private static final int MAX_PAGE_SIZE = 100;

    public int validatePageSize(int size) {
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
