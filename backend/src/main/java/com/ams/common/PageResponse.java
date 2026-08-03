package com.ams.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Paginated response wrapper for list endpoints.
 * Matches 05_REST_API_Specification Section 3.2 exactly.
 * 
 * @param <T> The type of elements in the content list
 */
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    
    /**
     * Create a PageResponse from a Spring Data Page.
     *
     * @param page the Spring Data Page
     * @return PageResponse with extracted pagination data
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
}
