package dev.thural.quietspacebackend.utils;

import org.springframework.data.domain.PageRequest;

public class CustomPageProvider {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 25;

    public static PageRequest buildCustomPageRequest(Integer pageNumber, Integer pageSize) {
        int queryPageNumber;
        int queryPageSize;

        if (pageNumber != null && pageNumber > 0) queryPageNumber = pageNumber - 1;
        else queryPageNumber = DEFAULT_PAGE;

        if (pageSize == null) queryPageSize = DEFAULT_PAGE_SIZE;
        else {
            if (pageSize > 1000) queryPageSize = 1000;
            else queryPageSize = pageSize;
        }

        return PageRequest.of(queryPageNumber, queryPageSize);
    }
}
