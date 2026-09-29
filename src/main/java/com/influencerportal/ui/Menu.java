package com.influencerportal.ui;

import com.influencerportal.exception.PortalException;
import java.util.List;

/** Shared menu loop: print options, read a choice, run it, and show any PortalException as a message. */
abstract class Menu {
    protected final ConsoleIO io;

    protected Menu(ConsoleIO io) {
        this.io = io;
    }

    protected abstract String title();

    protected abstract List<String> options();

    /** Runs option number {@code choice} (1-based); 0 is handled by the loop as "leave". */
    protected abstract void handle(int choice);

    public void run() {
        while (true) {
            io.println();
            io.println("=== " + title() + " ===");
            List<String> options = options();
            for (int i = 0; i < options.size(); i++) {
                io.println((i + 1) + ". " + options.get(i));
            }
            io.println("0. Back / Logout");
            int choice = io.askInt("Choice");
            if (choice == 0) {
                return;
            }
            if (choice < 0 || choice > options.size()) {
                io.println("Invalid choice.");
                continue;
            }
            try {
                handle(choice);
            } catch (PortalException e) {
                io.println("Error: " + e.getMessage());
            }
        }
    }
}
