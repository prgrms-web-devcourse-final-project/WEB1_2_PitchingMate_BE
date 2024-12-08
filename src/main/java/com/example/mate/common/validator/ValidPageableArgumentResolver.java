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

    // @ValidPageable 이 있을 때만 실행
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ValidPageable.class);
    }

    @Override
    public Pageable resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer,
                                    NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        // 클라이언트 요청값 생성
        String pageRequest = webRequest.getParameter(this.getParameterNameToUse(this.getPageParameterName(), methodParameter));
        String sizeRequest = webRequest.getParameter(this.getParameterNameToUse(this.getSizeParameterName(), methodParameter));

        // @ValidPageable 어노테이션 확인
        ValidPageable validPageable = methodParameter.getParameterAnnotation(ValidPageable.class);
        assert validPageable != null;

        int page = extractPageValue(pageRequest, validPageable);
        int size = extractSizeValue(sizeRequest, validPageable);

        return PageRequest.of(page, size);
    }

    // parameter 에 page 값이 있을 경우, 유효성 검증
    private int extractPageValue(String pageRequest, ValidPageable validPageable) {
        try {
            return pageRequest != null ? Math.max(0, Integer.parseInt(pageRequest)) : validPageable.page();
        } catch (NumberFormatException e) {
            return validPageable.page();
        }
    }

    // parameter 에 size 값이 있을 경우, 유효성 검증
    private int extractSizeValue(String sizeRequest, ValidPageable validPageable) {
        try {
            return sizeRequest != null ? Math.max(1, Integer.parseInt(sizeRequest)) : validPageable.size();
        } catch (NumberFormatException e) {
            return validPageable.size();
        }
    }
}