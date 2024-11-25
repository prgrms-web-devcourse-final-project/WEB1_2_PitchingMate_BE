package com.example.mate.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> content;         // 실제 데이터 목록
    private int totalPages;          // 총 페이지 수
    private long totalElements;      // 총 데이터 수
    private boolean hasNext;         // 다음 페이지 존재 여부
    private int pageNumber;          // 현재 페이지 번호
    private int pageSize;           // 페이지 크기
}
