package com.stc.inspireu.authorization;

import com.stc.inspireu.exceptions.CustomRunTimeException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Objects;

@Aspect
@Component
public class ValidatePermittedRoles {

    private final HttpServletRequest request;

    public ValidatePermittedRoles(@NonNull HttpServletRequest request) {
        this.request = request;
    }

    @Before("within(@com.stc.inspireu.authorization.PermittedRoles *) ||@annotation(com.stc.inspireu.authorization.PermittedRoles)")
    public void validateAspect(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        PermittedRoles permittedRoles = methodSignature.getMethod().getAnnotation(PermittedRoles.class);
        if (Objects.isNull(permittedRoles)) {
            permittedRoles = (PermittedRoles) joinPoint.getSignature().getDeclaringType().getAnnotation(PermittedRoles.class);
        }
        String role = request.getHeader("roleName");
        if (Objects.isNull(role) || Arrays.stream(permittedRoles.roles()).noneMatch(s -> s.name().equalsIgnoreCase(role)))
            throw new CustomRunTimeException("You have no access to this resource", HttpStatus.FORBIDDEN);
    }
}
