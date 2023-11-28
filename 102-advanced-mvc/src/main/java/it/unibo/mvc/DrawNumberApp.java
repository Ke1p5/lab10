package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private int min;
    private int max;
    private int attempts;
    private static final String CONFIG_FILE = "src/main/resources/config.yml";

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    private void getConfig(final String filePath) throws IOException{
        try (
            final BufferedReader br = new BufferedReader(new FileReader(filePath));
        ) {
            String line = null;
            final ArrayList<String> configs = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String last = line.substring(line.lastIndexOf(' ') + 1);    
                configs.add(last);       
            }
            min = Integer.parseInt(configs.get(0));
            max = Integer.parseInt(configs.get(1));
            attempts = Integer.parseInt(configs.get(2));
        }
    }

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
        try {
            this.getConfig(CONFIG_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.model = new DrawNumberImpl(min, max, attempts);
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
        new DrawNumberApp(new DrawNumberViewImpl(), new DrawNumberViewImpl(), new PrintStreamView(System.out), new PrintStreamView("output.log"));
    }

}
