package com.example.payment_service.exception;

public abstract class BaseException extends RuntimeException{
 public BaseException(String message){
     super(message);
 }
}
