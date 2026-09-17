/*
 * =============================================================================
 * BMI Advisor — Phase 1 (correct results in the original Swing window)
 * =============================================================================
 *
 * WHAT THIS PROGRAM IS
 * --------------------
 * A small desktop window written in Java Swing. The user types height and
 * weight, clicks Proceed, and the program shows:
 *   1. a Body Mass Index (BMI) number, and
 *   2. a short WHO weight-category message.
 *
 * Swing is Java's built-in GUI toolkit (buttons, labels, text fields, windows).
 * This file is intentionally still ONE class, matching the original student
 * program. Later phases can extract the math into a separate testable class
 * and replace the pixel-by-pixel layout. Phase 1 only makes the *answers* true.
 *
 * WHAT BMI ACTUALLY MEASURES
 * --------------------------
 * BMI estimates body mass relative to height. It is a screening index, not a
 * diagnosis. It does not distinguish fat from muscle, and it is a poor fit
 * for children, some athletes, and some older adults. This program reports
 * the adult WHO/CDC number and category only.
 *
 * UNITS (imperial — matching the on-screen labels "in" and "lbs")
 * ----------------------------------------------------------------
 * Height must be TOTAL INCHES, not feet. 5 ft 10 in is 70, not 5.10.
 * Weight is in pounds.
 *
 * The CDC/NIH imperial formula is:
 *
 *     BMI = 703 × weight(lb) / [height(in)]²
 *
 * Why 703? Metric BMI is kilograms / meters². Pounds and inches must be
 * converted into that same kg/m² scale:
 *
 *     1 lb  = 0.45359237 kg
 *     1 in  = 0.0254 m
 *
 *     BMI = (lb × 0.45359237) / (in × 0.0254)²
 *         = (lb / in²) × (0.45359237 / 0.00064516)
 *         ≈ (lb / in²) × 703.06957
 *
 * Medical calculators round the conversion factor to 703. That is the
 * standard used here. (The original program used weight / (2 × height),
 * which is not BMI in any unit system.)
 *
 * WHO ADULT CATEGORIES (used by CDC charts as well)
 * -------------------------------------------------
 *     BMI < 18.5          underweight
 *     18.5 ≤ BMI < 25.0   normal (healthy) weight
 *     25.0 ≤ BMI < 30.0   overweight
 *     BMI ≥ 30.0          obese
 *
 * Boundaries matter: 18.5 is normal, not underweight. 25.0 is overweight,
 * not normal. 30.0 is obese. We round BMI to one decimal place *before*
 * classifying so the number on screen and the advice cannot disagree
 * (example: 24.96 would display as 25.0 and must be classified as 25.0).
 *
 * HOW TO COMPILE AND RUN
 * ----------------------
 * Java requires the file name to match the public class: BMI.java
 *
 * Java 8 or newer is required (main() uses a lambda to start Swing).
 *
 *     javac BMI.java
 *     java BMI
 *
 * javac produces BMI.class (ignored by .gitignore). java BMI launches main().
 *
 * WHAT WAS WRONG IN THE ORIGINAL (and is fixed here)
 * --------------------------------------------------
 *  • File had no .java extension, so javac could not compile class BMI.
 *  • Formula was weight / (2 × height).
 *  • Categories used bmi > 18.5 as "underweight" and chained ifs so
 *    underweight never stuck and obesity never appeared.
 *  • The BMI label was never given a value (setText was never called).
 *  • Invalid text still ran the formula with 0.0; zero height → Infinity.
 *  • Advice was drawn in a 40-pixel-wide label, so the sentence was clipped.
 */

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.util.Locale;

/**
 * The application window.
 *
 * extends JFrame means this class *is* a window: title bar, close button,
 * and a content pane where we place labels, fields, and buttons.
 *
 * Fields (the JLabel / JTextField / JButton variables) are instance members
 * so the inner listener can read what the user typed and write results back
 * onto the same window. If they were local variables inside the constructor,
 * actionPerformed() would not be able to see them.
 */
public class BMI extends JFrame {

    /*
     * ----- Domain constants -------------------------------------------------
     * Named constants beat "magic numbers" scattered through if-statements.
     * If a guideline ever changes, you edit one place.
     *
     * IMPERIAL_CONVERSION is the 703 factor derived in the file header.
     * The three cutoffs are exclusive upper bounds of each WHO band, except
     * obesity which has no upper bound in this simple screening tool.
     */
    private static final double IMPERIAL_CONVERSION = 703.0;
    private static final double UNDERWEIGHT_EXCLUSIVE_MAX = 18.5;
    private static final double NORMAL_EXCLUSIVE_MAX = 25.0;
    private static final double OVERWEIGHT_EXCLUSIVE_MAX = 30.0;

    /*
     * DecimalFormat("0.0") always shows one digit after the decimal
     * (24.0 not 24, 24.4 not 24.40). Locale.US forces a dot, not a comma,
     * so a machine set to a European locale still shows 24.4 rather than 24,4.
     *
     * static final means one shared formatter for every BMI window, created
     * once when the class is loaded — not on every button click.
     */
    private static final DecimalFormat BMI_FORMAT =
            new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.US));

    /*
     * ----- Swing widgets ----------------------------------------------------
     * JLabel     = text the user does not edit (titles, units, results).
     * JTextField = a box the user types into.
     * JButton    = a clickable control that fires an ActionEvent.
     *
     * We keep the original names where they still make sense (txtHeight,
     * btnProceed) so this stays recognizable as the same program. Unit
     * labels are renamed from lblM / lblKg because the form is inches and
     * pounds, not meters and kilograms.
     */
    private JLabel lblHeader;
    private JLabel lblInstructions;

    private JLabel lblHeight;
    private JTextField txtHeight;
    private JLabel lblInches;

    private JLabel lblWeight;
    private JTextField txtWeight;
    private JLabel lblPounds;

    private JLabel lblBmi;
    private JLabel lblBmiOutput;

    private JLabel lblAdvice;
    private JLabel lblAdviceOutput;

    private JButton btnProceed;
    private JButton btnExit;

    /**
     * Builds the window: create widgets, place them, hook up click handlers,
     * then show the frame.
     *
     * A constructor runs once, when someone writes {@code new BMI()}.
     * It should set up the UI. It should NOT compute a BMI — there is no
     * input yet. Calculation happens later, in the listener, when Proceed
     * is clicked.
     */
    public BMI() {
        super("BMI Advisor");

        /*
         * EXIT_ON_CLOSE tells Swing: when the user clicks the window's X,
         * also end the Java process. The JFrame default is HIDE_ON_CLOSE,
         * which only hides the window and leaves the JVM running in the
         * background — a common source of "I closed it but java.exe is
         * still running" confusion.
         */
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createWidgets();
        layoutWidgets();
        wireEvents();

        /*
         * Pack is the usual way to size a window from its layout manager.
         * We are still on a null (absolute) layout, so pack() cannot infer
         * a size from constraints. We set an explicit pixel size instead.
         *
         * 480×300 is a little larger than the original 400×250 so the
         * instruction line and the full advice sentence can be seen.
         */
        setSize(480, 300);
        setResizable(false);
        setLocationRelativeTo(null); // center on the screen; null = screen origin
        setVisible(true);
    }

    /**
     * Constructs every widget and sets fonts / starting text.
     * Separated from layout so "what exists" is not mixed with "where it sits".
     */
    private void createWidgets() {
        lblHeader = new JLabel("BMI Advisor");
        lblHeader.setFont(new Font("Arial", Font.BOLD, 28));

        lblInstructions = new JLabel("Height is total inches (70, not 5 ft 10 in). Weight is pounds.");
        lblInstructions.setFont(new Font("Arial", Font.PLAIN, 11));
        lblInstructions.setForeground(Color.DARK_GRAY);

        lblHeight = new JLabel("Height");
        txtHeight = new JTextField(5);
        lblInches = new JLabel("in");

        lblWeight = new JLabel("Weight");
        txtWeight = new JTextField(5);
        lblPounds = new JLabel("lbs");

        lblBmi = new JLabel("BMI");
        lblBmiOutput = new JLabel("");
        lblBmiOutput.setFont(new Font("Arial", Font.BOLD, 14));

        lblAdvice = new JLabel("Advice");
        lblAdviceOutput = new JLabel("");

        btnProceed = new JButton("Proceed");
        btnExit = new JButton("Exit");
    }

    /**
     * Places widgets with absolute pixel coordinates.
     *
     * setLayout(null) is called a "null layout". You then call setBounds(x, y,
     * width, height) on each component. It is easy to start with, and it is
     * also brittle: resize the window, change font size, or run on a high-DPI
     * display and the controls no longer line up.
     *
     * Phase 1 keeps this layout on purpose (do not redesign the form yet).
     * The only layout change is that result labels are wide enough to show
     * "24.4" and "You are underweight." — the original 40×20 advice box
     * clipped those sentences.
     *
     * Coordinates are pixels from the top-left of the content pane:
     *   x increases to the right, y increases downward.
     */
    private void layoutWidgets() {
        JPanel panel = (JPanel) getContentPane();
        panel.setLayout(null);

        lblHeader.setBounds(140, 12, 240, 32);
        lblInstructions.setBounds(28, 48, 420, 18);

        lblHeight.setBounds(28, 80, 50, 22);
        txtHeight.setBounds(88, 80, 56, 22);
        lblInches.setBounds(150, 80, 40, 22);

        lblWeight.setBounds(28, 110, 50, 22);
        txtWeight.setBounds(88, 110, 56, 22);
        lblPounds.setBounds(150, 110, 40, 22);

        lblBmi.setBounds(28, 148, 50, 22);
        lblBmiOutput.setBounds(88, 148, 80, 22);

        btnProceed.setBounds(340, 80, 100, 24);
        btnExit.setBounds(340, 110, 100, 24);

        lblAdvice.setBounds(28, 188, 50, 22);
        /*
         * Advice sits on its own row under the buttons so a full sentence
         * does not collide with Exit. ~380 px is enough for the longest
         * message we produce, including error text.
         */
        lblAdviceOutput.setBounds(88, 188, 360, 22);

        panel.add(lblHeader);
        panel.add(lblInstructions);
        panel.add(lblHeight);
        panel.add(txtHeight);
        panel.add(lblInches);
        panel.add(lblWeight);
        panel.add(txtWeight);
        panel.add(lblPounds);
        panel.add(lblBmi);
        panel.add(lblBmiOutput);
        panel.add(lblAdvice);
        panel.add(lblAdviceOutput);
        panel.add(btnProceed);
        panel.add(btnExit);
    }

    /**
     * Registers listeners. A listener is an object Swing calls when something
     * happens (a click, a key, a window close).
     *
     * We use one inner class for both buttons. actionPerformed() then asks
     * e.getSource() which widget was clicked. Two separate listeners would
     * also work; one class keeps the original structure.
     *
     * addActionListener(...) does not call our code now. It *stores* the
     * listener. Later, when the user clicks, Swing calls actionPerformed.
     * That is event-driven programming: the program waits, then reacts.
     */
    private void wireEvents() {
        MyListener listener = new MyListener();
        btnProceed.addActionListener(listener);
        btnExit.addActionListener(listener);
    }

    /**
     * Imperial BMI: 703 × lb / in².
     *
     * Caller must already have rejected height ≤ 0. Dividing by height² of
     * zero would yield Infinity (floating-point has no exception for that).
     *
     * We square with height * height rather than Math.pow(height, 2) because
     * it is the same result for a positive double and makes the formula
     * look like the paper definition.
     *
     * @param heightInches total height in inches (must be &gt; 0)
     * @param weightPounds body weight in pounds (must be &gt; 0)
     * @return BMI on the kg/m² scale (the 703 factor performs the conversion)
     */
    public double calculateBmi(double heightInches, double weightPounds) {
        return IMPERIAL_CONVERSION * weightPounds / (heightInches * heightInches);
    }

    /**
     * Rounds to one decimal place using half-up arithmetic.
     *
     * BMI is conventionally shown as one decimal (24.4, not 24.387...).
     * Math.round(x * 10) / 10.0 moves the decimal one place right, rounds
     * to the nearest integer, then moves it back:
     *
     *     24.44 → 244.4 → 244 → 24.4
     *     24.45 → 244.5 → 245 → 24.5
     *
     * We classify on this rounded value so a displayed "25.0" cannot be
     * labeled "normal weight".
     */
    public double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    /**
     * Maps a (already rounded) BMI onto a WHO adult category sentence.
     *
     * if / else if / else is a chain of mutually exclusive branches: the
     * first true condition wins, and the rest are skipped. That is the
     * opposite of the original program, which used two independent ifs so
     * the second assignment overwrote the first.
     *
     * Using &lt; on the *next* cutoff is the clean way to write a half-open
     * interval. After we know bmi is not &lt; 18.5, "bmi &lt; 25" means
     * 18.5 ≤ bmi &lt; 25 without writing both ends.
     *
     * @param bmi BMI rounded to one decimal place
     * @return a short user-facing sentence
     */
    public String classifyBmi(double bmi) {
        if (bmi < UNDERWEIGHT_EXCLUSIVE_MAX) {
            return "You are underweight.";
        } else if (bmi < NORMAL_EXCLUSIVE_MAX) {
            return "You are a normal weight.";
        } else if (bmi < OVERWEIGHT_EXCLUSIVE_MAX) {
            return "You are overweight.";
        } else {
            return "You are obese.";
        }
    }

    /**
     * Turns a successful BMI into on-screen text.
     */
    private void showResult(double roundedBmi, String advice) {
        lblBmiOutput.setText(BMI_FORMAT.format(roundedBmi));
        lblBmiOutput.setForeground(Color.BLACK);
        lblAdviceOutput.setText(advice);
        lblAdviceOutput.setForeground(Color.BLACK);
    }

    /**
     * Shows a validation error and clears the BMI so a previous good result
     * cannot be mistaken for the current (failed) attempt.
     */
    private void showError(String message) {
        lblBmiOutput.setText("");
        lblAdviceOutput.setText(message);
        lblAdviceOutput.setForeground(new Color(0xB00020)); // dark red; still readable
    }

    /**
     * Inner class = a class declared inside another class.
     *
     * Because it is not static, it holds a hidden reference to the enclosing
     * BMI window. That is why it can mention txtHeight, btnProceed, and
     * showError() as if they were its own members.
     *
     * ActionListener is an *interface*: a contract with one method,
     * actionPerformed. "implements ActionListener" means "I promise to
     * provide that method." Swing will call it on the Event Dispatch Thread
     * (EDT), the single thread that is allowed to touch Swing widgets.
     */
    public class MyListener implements ActionListener {

        /**
         * Called by Swing after a registered button is clicked.
         *
         * ActionEvent is a small object describing the click. getSource()
         * returns the widget that fired it. We compare with == (same object
         * in memory), not .equals(), because we want identity: was it
         * *this* Proceed button?
         *
         * @param e the click (or keyboard activation) Swing delivered
         */
        @Override
        public void actionPerformed(ActionEvent e) {

            if (e.getSource() == btnExit) {
                /*
                 * Ends the JVM immediately. That also closes the window.
                 * Phase 2 can switch to dispose() so other windows in a
                 * larger app would keep running; for a one-window tool this
                 * matches the original Exit button.
                 */
                System.exit(0);
                return;
            }

            if (e.getSource() != btnProceed) {
                return;
            }

            /*
             * ----- 1. Read and parse ---------------------------------------
             * getText() always returns a String, even if the box looks empty
             * (empty string "", not null). Double.parseDouble("70") → 70.0.
             *
             * trim() strips leading/trailing spaces so " 70 " still works.
             *
             * parseDouble throws NumberFormatException for "", "abc", "70in",
             * and locale commas. We catch that and STOP. The original bug
             * was catching the error, then falling through and computing
             * BMI from the leftover 0.0 values.
             */
            final String heightText = txtHeight.getText().trim();
            final String weightText = txtWeight.getText().trim();

            final double heightInches;
            final double weightPounds;
            try {
                heightInches = Double.parseDouble(heightText);
                weightPounds = Double.parseDouble(weightText);
            } catch (NumberFormatException ex) {
                showError("Enter numeric height (inches) and weight (pounds).");
                return;
            }

            /*
             * ----- 2. Reject physically impossible values -------------------
             * Negative height/weight are numbers, so parse succeeds, but BMI
             * would be nonsense. Zero height would divide by zero in IEEE
             * floating point and produce Infinity, which then fails every
             * category test in surprising ways.
             *
             * We use <= 0, not == 0, to catch negatives in the same check.
             */
            if (heightInches <= 0.0 || weightPounds <= 0.0) {
                showError("Height and weight must be greater than 0.");
                return;
            }

            /*
             * ----- 3. Compute, round, classify, display ---------------------
             * Work is split across three methods so each has one job:
             *   calculateBmi   → the 703 formula
             *   roundToOneDecimal → display precision
             *   classifyBmi    → WHO sentence
             *
             * Example: 70 in, 170 lb
             *   703 × 170 / 70² = 119510 / 4900 ≈ 24.3898 → 24.4 → normal
             */
            final double rawBmi = calculateBmi(heightInches, weightPounds);
            final double roundedBmi = roundToOneDecimal(rawBmi);
            final String advice = classifyBmi(roundedBmi);
            showResult(roundedBmi, advice);
        }
    }

    /**
     * Program entry point. The JVM calls this after {@code java BMI}.
     *
     * Swing widgets must be created and touched on the Event Dispatch Thread.
     * SwingUtilities.invokeLater(runnable) posts our constructor to that
     * thread's queue. The original {@code new BMI()} inside main() often
     * "works" on simple forms, but it is a race: main() is not the EDT.
     *
     * The lambda {@code () -> new BMI()} is a Runnable whose run() method
     * constructs the window. invokeLater returns immediately; the window
     * appears a moment later when the EDT picks up the task.
     *
     * @param args command-line arguments (unused; required by the JVM signature)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BMI());
    }
}
