# BMI Advisor

A desktop Body Mass Index (BMI) calculator written in Java Swing. You enter height in **total inches** and weight in **pounds**, click **Proceed**, and the window shows a BMI (one decimal place) plus a WHO adult weight category.

BMI is a screening index, not a diagnosis. It does not distinguish fat from muscle, and this program is for adult CDC/WHO ranges only.

## Requirements

- **JDK 8 or newer** (the program uses a lambda in `main` to start Swing)
- `javac` and `java` on your `PATH`

Check with:

```bash
java -version
javac -version
```

## How to run

From the project root (the folder that contains `BMI.java`):

```bash
javac BMI.java
java BMI
```

`javac` writes `BMI.class` in the same folder (this file is gitignored). `java BMI` launches `BMI.main`, which opens the window on Swing’s Event Dispatch Thread.

To compile and run again after editing, repeat both commands. Closing the window or clicking **Exit** ends the program.

### Example

| Height | Weight | BMI  | Advice                    |
|--------|--------|------|---------------------------|
| 70 in  | 170 lb | 24.4 | You are a normal weight.  |

Height must be **total inches**, not feet. 5 ft 10 in is `70`, not `5.10` or `5'10`.

## How it works

The app is one public class in [`BMI.java`](BMI.java): a `JFrame` window plus the calculation logic.

### User flow

1. `main` calls `SwingUtilities.invokeLater(() -> new BMI())` so the window is created on the Swing UI thread.
2. The constructor builds labels, text fields, and buttons, places them, and registers a click listener.
3. **Proceed** reads the two fields, validates them, computes BMI, and updates the BMI and Advice labels.
4. **Exit** (or the window close button) ends the Java process.

Invalid input does **not** fall through into the formula. Non-numeric text and values ≤ 0 show an error, clear the BMI field, and return.

### Formula

Imperial CDC/NIH formula, which reports BMI on the same kg/m² scale as the metric definition:

```text
BMI = 703 × weight(lb) / [height(in)]²
```

The factor `703` converts pounds and inches into that metric scale. The result is rounded to one decimal place **before** classification so the number on screen and the advice cannot disagree (for example a raw `24.96` displays as `25.0` and is treated as `25.0`).

### Categories (WHO adult)

| BMI           | Message                      |
|---------------|------------------------------|
| < 18.5        | You are underweight.         |
| 18.5 – 24.9   | You are a normal weight.     |
| 25.0 – 29.9   | You are overweight.          |
| ≥ 30.0        | You are obese.               |

Boundaries are inclusive of the lower end of each band: `18.5` is normal, `25.0` is overweight, `30.0` is obese.

### Code map

| Piece | Role |
|-------|------|
| `BMI` (extends `JFrame`) | Window, widgets, and layout |
| `MyListener` | Inner `ActionListener` for Proceed and Exit |
| `calculateBmi` | `703 × lb / in²` |
| `roundToOneDecimal` | Half-up rounding to one decimal |
| `classifyBmi` | Mutually exclusive WHO ranges |
| `showResult` / `showError` | Write success or error text to the labels |

Layout is still absolute (`setLayout(null)` + `setBounds`). That is easy to follow and brittle if you resize the window; the form is fixed-size for that reason.

## Project layout

```text
BMI.java      source (public class name must match this file name)
README.md     this file
.gitignore    ignores *.class and other Java build artifacts
```
