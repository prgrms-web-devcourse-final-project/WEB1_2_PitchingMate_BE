package com.example.mate.common.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    // Pageable 검증 메서드
    public static Pageable validatePageable(Pageable pageable) {
        // pageNumber 검증: 0보다 작은 값은 0으로 처리
        int pageNumber = Math.max(pageable.getPageNumber(), 0);

        // pageSize 검증: 0 이하이면 기본값 10으로 설정
        int pageSize = pageable.getPageSize() <= 0 ? 10 : pageable.getPageSize();
        return PageRequest.of(pageNumber, pageSize, pageable.getSort());
    }
}
