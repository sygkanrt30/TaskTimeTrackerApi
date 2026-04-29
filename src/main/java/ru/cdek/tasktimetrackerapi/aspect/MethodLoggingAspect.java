package ru.cdek.tasktimetrackerapi.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class MethodLoggingAspect {

    @Pointcut("""
                    execution(* ru.cdek.tasktimetrackerapi..*.*(..)) &&
                    !within(ru.cdek.tasktimetrackerapi.ecxeption..*) &&
                    !within(ru.cdek.tasktimetrackerapi.config..*)
            """)
    public void applicationPackageMethods() {}

    @Around("applicationPackageMethods()")
    public Object logMethodEntryExit(ProceedingJoinPoint joinPoint) throws Throwable {
        var signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        if (log.isTraceEnabled()) {
            Object[] args = joinPoint.getArgs();
            String params = Arrays.stream(args)
                    .map(arg -> Objects.nonNull(arg) ? arg.toString() : "null")
                    .collect(Collectors.joining(", "));
            log.trace("-> Вход в {}.{}({})", className, methodName, params);
        }

        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (log.isTraceEnabled()) {
                String resultStr = Objects.nonNull(result) ? result.toString() : "null";
                if (resultStr.length() > 500) {
                    resultStr = resultStr.substring(0, 500) + "... [обрезано]";
                }
                log.trace("<- Выход из {}.{} -> {} ({} мс)", className, methodName, resultStr, duration);
            }
            if (duration > 1000 && log.isInfoEnabled()) {
                log.info("Медленный метод {}.{}: {} мс", className, methodName, duration);
            }
        }
    }
}
