package com.sparta.myselectshop.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class RestApiException {  //dto´À³¦
    private String errorMessage;
    private HttpStatus httpStatus;
}