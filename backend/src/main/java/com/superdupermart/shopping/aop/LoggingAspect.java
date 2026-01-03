package com.superdupermart.shopping.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Around("execution(* com.superdupermart.shopping.controller.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        
        logger.info("Entering {}.{}", className, methodName);
        
        Object proceed = joinPoint.proceed();
        
        long executionTime = System.currentTimeMillis() - start;
        
        logger.info("Exiting {}.{}. Execution time: {} ms", className, methodName, executionTime);
        
        return proceed;
    }
}
