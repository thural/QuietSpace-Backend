package dev.thural.quietspace.exception;

import java.net.SocketException;

public class CustomSocketException extends SocketException {

    public CustomSocketException(String msg) {
        super("socket exception occurred: ".concat(msg));
    }
    
}
