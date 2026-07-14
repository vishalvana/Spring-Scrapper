package com.example.webscraper.exception;

public class RobotsDisallowedException extends RuntimeException {

    public RobotsDisallowedException(String message) {
        super(message);
    }
}