package com.influencerportal.ui;

/** Thrown when standard input is closed (Ctrl+D or the end of a piped script). */
public class EndOfInputException extends RuntimeException {
    public EndOfInputException() {
        super("End of input");
    }
}
