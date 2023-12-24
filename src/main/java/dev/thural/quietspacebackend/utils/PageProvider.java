package dev.thural.quietspacebackend.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class PageProvider {
    private final static Integer DEFAULT_PAGE = 0;
    private final static Integer DEFAULT_PAGE_SIZE = 25;

    public static PageRequest buildPageRequest(Integer pageNumber, Integer pageSize) {
        int queryPageNumber;
        int queryPageSize;

        if (pageNumber != null && pageNumber > 0) queryPageNumber = pageNumber - 1;
        else queryPageNumber = DEFAULT_PAGE;

        if (pageSize == null) queryPageSize = DEFAULT_PAGE_SIZE;
        else queryPageSize = pageSize > 1000 ? 1000 : pageSize;

        Sort sort = Sort.by(Sort.Order.asc("username"));

        return PageRequest.of(queryPageNumber, queryPageSize, sort);
    }
}
