package com.example.mate.common.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

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

    /**
     * Page 객체를 기반으로 PageResponse 를 생성하는 팩토리 메서드
     *
     * @param page    Spring Data JPA 의 Page 객체
     * @param content 변환된 데이터 리스트
     * @param <R>     원본 데이터 타입
     * @param <T>     변환된 데이터 타입
     * @return PageResponse
     */
    public static <R, T> PageResponse<T> from(Page<R> page, List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .hasNext(page.hasNext())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }

    /**
     * Page 객체를 기반으로 PageResponse 를 생성하는 팩토리 메서드
     *
     * @param page    Spring Data JPA 의 Page 객체
     * @param <T>     변환된 데이터 타입
     * @return PageResponse
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .hasNext(page.hasNext())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }
}