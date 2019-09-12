package com.pgmmers.radar.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * method log and cost time.
 * @author by feihu.wang
 */
@Aspect
@Component
public class LogAspect {
    public static Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Pointcut("execution(public * com.pgmmers.radar.controller.*.*(..))")
    public void logControllerAspect(){}

    @Pointcut("execution(public * com.pgmmers.radar.service.*.*.*(..))")
    public void logServiceAspect(){}


    @Pointcut("logControllerAspect() || logServiceAspect()")
    public void logAspect(){
    }


    @Around("logAspect()")
    public Object deAround(ProceedingJoinPoint joinPoint) throws Throwable{
        long begin = System.currentTimeMillis();
        logger.info("start exec:{}", joinPoint.getSignature().toString());
        Object object = joinPoint.proceed();
        long end = System.currentTimeMillis();
        logger.info("finished...{}, exec cost {}", joinPoint.getSignature().toString(), end-begin);

        return object;
    }

}
