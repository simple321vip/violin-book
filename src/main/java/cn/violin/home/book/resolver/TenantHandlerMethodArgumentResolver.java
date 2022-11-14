package cn.violin.home.book.resolver;

import cn.violin.home.book.annotation.CurrentUser;
import cn.violin.home.book.entity.Tenant;
import cn.violin.home.book.service.TenantService;
import cn.violin.home.book.utils.JedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@Slf4j
public class TenantHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    TenantService tenantService;

    @Autowired
    private JedisUtils redis;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) &&
                parameter.getParameterType().isAssignableFrom(Tenant.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {

        String Authorization = webRequest.getHeader("Authorization");
        String tenantId = webRequest.getHeader("tenantId");
        String pageNo = webRequest.getParameter("page_number");
        String pageSize = webRequest.getParameter("page_size");
        Tenant tenant = new Tenant();
        tenant.setTenantId(tenantId);
        if (StringUtils.hasLength(pageNo)) {
            tenant.setPageNo(Integer.parseInt(pageNo));
        }
        if (StringUtils.hasLength(pageSize)) {
            tenant.setPageNo(Integer.parseInt(pageSize));
        }
        log.info("Authorization : [ " + Authorization + " ]");
        log.info("tenantId : [ " + tenantId + " ]");
        return tenant;
    }
}
