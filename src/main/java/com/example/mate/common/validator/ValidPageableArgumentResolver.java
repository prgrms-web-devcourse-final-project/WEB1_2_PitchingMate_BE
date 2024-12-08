package com.example.mate.common.validator;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 커스텀 Pageable 리졸버로, {@code @ValidPageable} 어노테이션이 붙은 Pageable 매개변수의 유효성 검사를 수행합니다.
 */
@Component
public class ValidPageableArgumentResolver extends PageableHandlerMethodArgumentResolver {

    @Override
    public Pageable resolveArgument(MethodParameter methodParameter,
                                    ModelAndViewContainer mavContainer,
                                    NativeWebRequest webRequest,
                                    WebDataBinderFactory binderFactory) {

        // 기본 Pageable 생성
        Pageable pageable = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

        // @ValidPageable 어노테이션 확인
        ValidPageable validPageable = methodParameter.getParameterAnnotation(ValidPageable.class);

        // 클라이언트가 page 파라미터를 제공했는지 확인
        String pageParam = webRequest.getParameter("page");

        // pageParam이 존재하면 pageable.getPageNumber() 사용
        // pageParam이 없고 validPageable이 존재하면 validPageable.page() 사용
        // 둘 다 없으면 0을 사용
        int pageNumber = pageParam != null ?
                pageable.getPageNumber() :
                (validPageable != null ? validPageable.page() : 0);

        int pageSize = pageable.getPageSize() > 0
                ? pageable.getPageSize()
                : validPageable != null ? validPageable.size() : 10;

        return PageRequest.of(pageNumber, pageSize, pageable.getSort());
    }
}