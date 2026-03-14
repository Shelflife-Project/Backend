package com.shelflife.project.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class PaginatedResponse<T> {
    private List<T> data;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;

    public PaginatedResponse(Page<T> page) {
        setData(page.getContent());
        setCurrentPage(page.getNumber());
        setTotalPages(page.getTotalPages());
        setTotalItems(page.getTotalElements());
        setPageSize(page.getSize());
        setHasNext(page.hasNext());
        setHasPrevious(page.hasPrevious());
    }
}