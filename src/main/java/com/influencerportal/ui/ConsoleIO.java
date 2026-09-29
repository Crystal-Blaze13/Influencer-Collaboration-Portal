package com.influencerportal.ui;

import com.influencerportal.exception.ValidationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * The single place that reads from the keyboard. The legacy program created a new Scanner in every menu,
 * which loses buffered input when stdin is piped; here one reader is shared by every menu.
 */
public class ConsoleIO {
    private final BufferedReader in;
    private final PrintStream out;

    public ConsoleIO(InputStream in, PrintStream out) {
        this.in = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.out = out;
    }

    public void println(String text) {
        out.println(text);
    }

    public void println() {
        out.println();
    }

    public String ask(String prompt) {
        out.print(prompt + ": ");
        out.flush();
        try {
            String line = in.readLine();
            if (line == null) {
                throw new EndOfInputException();
            }
            return line.trim();
        } catch (IOException e) {
            throw new EndOfInputException();
        }
    }

    /** Asks until the user types an integer; the menu never crashes on bad input. */
    public long askLong(String prompt) {
        while (true) {
            String text = ask(prompt);
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException e) {
                println("Please enter a whole number.");
            }
        }
    }

    public int askInt(String prompt) {
        while (true) {
            long value = askLong(prompt);
            if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                return (int) value;
            }
            println("That number is too large.");
        }
    }

    public double askDouble(String prompt) {
        while (true) {
            String text = ask(prompt);
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException e) {
                println("Please enter a number.");
            }
        }
    }

    public BigDecimal askMoney(String prompt) {
        while (true) {
            String text = ask(prompt);
            try {
                return new BigDecimal(text);
            } catch (NumberFormatException e) {
                println("Please enter an amount such as 1500.00.");
            }
        }
    }

    /** Reads a comma-separated list such as "Instagram, YouTube". */
    public java.util.List<String> askList(String prompt) {
        java.util.List<String> items = new java.util.ArrayList<>();
        for (String part : ask(prompt).split(",")) {
            if (!part.isBlank()) {
                items.add(part.trim());
            }
        }
        if (items.isEmpty()) {
            throw new ValidationException("Please enter at least one value.");
        }
        return items;
    }
}
