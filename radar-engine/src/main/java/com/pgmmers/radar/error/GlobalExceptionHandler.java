package com.pgmmers.radar.error;

import com.pgmmers.radar.service.common.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * global exception handle.
 * @author feihu.wang
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        logger.error("handleMethodArgumentNotValidException: ", e.getMessage());
        CommonResult result = handleBindingResult(e.getBindingResult());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity handleRuntimeException(RuntimeException e) {
        logger.error("handleRuntimeException: ", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    @ResponseBody
    public ResponseEntity handleHttpMessageConversionException(HttpMessageConversionException e) {
        logger.error("handleHttpMessageConversionException: ", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity handleException(Exception e) {
        logger.error("Exception: ", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    private CommonResult handleBindingResult(BindingResult bindingResult) {
        CommonResult result = new CommonResult();
        List<String> errorList = new ArrayList<>();
        if (bindingResult.hasErrors()) {
            List<ObjectError> allErrors = bindingResult.getAllErrors();
            for (ObjectError objectError : allErrors) {
                String message = objectError.getDefaultMessage();
                errorList.add(message);
            }
        }
        result.getData().put("errList", errorList);
        return result;
    }
}