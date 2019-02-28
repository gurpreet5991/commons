package io.mosip.kernel.auth.adapter;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/***********************************************************************************************************************
 * Adds latest token to the response headers before it is committed
 *
 * @author Sabbu Uday Kumar
 * @since 1.0.0
 **********************************************************************************************************************/


@RestControllerAdvice
public class AuthControllerAdvice implements ResponseBodyAdvice<Object> {

    private AuthUserDetails getAuthUserDetails() {
    	AuthUserDetails authUserDetails = null;
    	Object details = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	if(details instanceof String){
    		
    	}
    	else 
    	{
    		 authUserDetails = (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	}
        return authUserDetails;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
    	if(getAuthUserDetails()!=null)
        response.getHeaders().add("Set-Cookie:", AuthAdapterConstant.AUTH_COOOKIE_HEADER+getAuthUserDetails().getToken());
        return body;
    }
}