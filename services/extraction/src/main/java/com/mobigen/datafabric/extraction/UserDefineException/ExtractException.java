package com.mobigen.datafabric.extraction.UserDefineException;

public class ExtractException extends RuntimeException{
    public ExtractException(){ }
    public ExtractException(String message){
        super(message);
    }
}