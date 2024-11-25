package it.unibo.mvc.controller;

import it.unibo.mvc.model.DrawNumber;
import it.unibo.mvc.model.DrawNumberImpl;
import it.unibo.mvc.model.DrawResult;
import it.unibo.mvc.view.DrawNumberView;
import it.unibo.mvc.view.DrawNumberViewImpl;
import it.unibo.mvc.view.PrintStreamView;

import java.awt.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int ATTEMPTS = 10;
    private static final String PATH = "/Users/gardini/Desktop/Local/Ale/Uni/24-25/FirstSemester/OOP/WeeklyExercise/lab10_Salinatore/102-advanced-mvc/src/main/resources/config.yml";

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        Configuration configuration = getConfiguration();
        this.model = new DrawNumberImpl(configuration.getMin(), configuration.getMax(), configuration.getAttempts());
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(
                new DrawNumberViewImpl(),
                new DrawNumberViewImpl(),
                new PrintStreamView(System.out)
                );
    }

    private Configuration getConfiguration() {
        Configuration.Builder configurationBuilder = new Configuration.Builder();
        try (final BufferedReader file =
                new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(PATH)
                        )
                )
        ){
            StringTokenizer stringTokenizer = new StringTokenizer(file.readLine(), " ");
            stringTokenizer.nextToken();
            configurationBuilder.setMin(Integer.parseInt(stringTokenizer.nextToken()));
            stringTokenizer = new StringTokenizer(file.readLine(), " ");
            stringTokenizer.nextToken();
            configurationBuilder.setMax(Integer.parseInt(stringTokenizer.nextToken()));
            stringTokenizer = new StringTokenizer(file.readLine(), " ");
            stringTokenizer.nextToken();
            configurationBuilder.setAttempts(Integer.parseInt(stringTokenizer.nextToken()));
        } catch (IOException e) {
            this.DisplayError(e.getMessage());
        }
        final Configuration configuration = configurationBuilder.build();
        if (configuration.isConsistent()) {
            return configuration;
        } else {
            this.DisplayError("Using default - data in file not consistent");
            return new Configuration.Builder().build();
        }
    }

    private void DisplayError(String errorMessage) {
        for (var view : this.views) {
            view.displayError(errorMessage);
        }
    }
}
