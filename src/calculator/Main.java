package calculator;

import calculator.Main.Operation.OperationFactory;
import calculator.Main.Operation.OperationType;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class Main extends Application {

    private static final Font defaultFont = Font.font(30);

    private Memory memory;
    private Settable<Boolean> clearOnNextInput;
    private GridPane grid;
    private Map<String, Button> buttons;
    private TextField input;

    private static class Memory {
        private double left;
        private Operation action;

        Memory() {
            this.clear();
        }

        void remember(double value, Operation action) {
            this.left = value;
            this.action = action;
        }

        void clear() {
            this.left = 0;
            this.action = OperationFactory.getOperation(OperationType.NOOP);
        }

        Double getLeft() {
            return this.left;
        }

        Operation getAction() {
            return this.action;
        }
    }

    private static class Settable<T> {
        private T value;

        Settable(T value) {
            this.value = value;
        }

        Settable() {
            this(null);
        }

        T get() {
            return value;
        }

        void set(T value) {
            this.value = value;
        }
    }

    @FunctionalInterface
    public interface Operation {

        enum OperationType {
            PLUS, MINUS, MULT, DIV, NOOP
        }

        Double apply(double left, double right);

        class OperationFactory {

            private OperationFactory() {}

            private static Operation getOperation(OperationType operationType) {
                switch (operationType)  {
                    case PLUS: return (left, right) -> left + right;
                    case MINUS: return (left, right) -> left - right;
                    case MULT: return (left, right) -> left * right;
                    case DIV: return (left, right) -> left / right;
                    case NOOP:
                    default: return (left, right) -> right;
                }
            }
        }
    }


    private void createNumPadButton(final Integer n) {
        Button button = new Button(n.toString());
        button.setFont(defaultFont);
        button.setMinSize(100, 100);
        button.setMaxSize(100, 100);

        button.setOnAction(event -> {
            if (clearOnNextInput.get()) {
                input.clear();
                clearOnNextInput.set(false);
            }
            input.appendText(n.toString());
        });

        grid.add(button, (n - 1) % 3, ((n - 1) / 3) + 1);
        buttons.put(n.toString(), button);
    }

    private void doAction(OperationType opType) {
        memory.remember(memory.getAction().apply(memory.getLeft(), Double.parseDouble(input.getText())), OperationFactory.getOperation(opType));
        input.setText(memory.getLeft().toString());
        clearOnNextInput.set(true);
    }

    private void createOperationButton(String symbol, OperationType operationType, int row) {
        Button button = new Button(symbol);
        button.setFont(defaultFont);
        button.setMinSize(100, 100);
        button.setMaxSize(100, 100);
        button.setOnAction(event -> doAction(operationType));
        grid.add(button, 3, row);
        buttons.put(symbol, button);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        memory = new Memory();
        clearOnNextInput = new Settable<>(false);
        buttons = new HashMap<>();

        primaryStage.setTitle("Calculator");

        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setPadding(new Insets(0));

        Scene scene = new Scene(grid);

        input = new TextField();
        input.setMinSize(400, 100);
        input.setMaxSize(400, 100);
        input.setFont(defaultFont);
        grid.add(input, 0, 0, 4, 1);

        for (Integer i = 1; i < 10; i++) {
            createNumPadButton(i);
        }

        Button zero = new Button("0");
        zero.setFont(defaultFont);
        zero.setMinSize(200, 100);
        zero.setMaxSize(200, 100);
        zero.setOnAction(event -> {
            if (clearOnNextInput.get()) {
                input.clear();
                clearOnNextInput.set(false);
            }
            input.appendText("0");
        });
        grid.add(zero, 0, 4, 2, 1);
        buttons.put("0", zero);

        Button dot = new Button(".");
        dot.setFont(defaultFont);
        dot.setMinSize(100, 100);
        dot.setMaxSize(100, 100);
        dot.setOnAction(event -> {
            if (input.getText().contains(".")) return;
            if (clearOnNextInput.get()) {
                input.clear();
                clearOnNextInput.set(false);
            }
            if (input.getText().isEmpty()) {
                input.setText("0");
            }
            input.appendText(".");
        });
        grid.add(dot, 2, 4);
        buttons.put(".", dot);

        Button changeSign = new Button("\u00B1");
        changeSign.setFont(defaultFont);
        changeSign.setMinSize(100, 100);
        changeSign.setMaxSize(100, 100);
        changeSign.setOnAction(event -> {
            if (input.getText().isEmpty()) return;
            if (input.getText().charAt(0) == '-') {
                input.setText(input.getText().substring(1));
            } else {
                input.setText("-" + input.getText());
            }
        });
        grid.add(changeSign, 0, 5);
        buttons.put("%", changeSign);
        buttons.put("\u00B1", changeSign);

        Button clear = new Button("C");
        clear.setFont(defaultFont);
        clear.setMinSize(100, 100);
        clear.setMaxSize(100, 100);
        clear.setOnAction(event -> input.clear());
        grid.add(clear, 1, 5);
        buttons.put("C", clear);
        buttons.put("c", clear);

        Button allClear = new Button("AC");
        allClear.setFont(defaultFont);
        allClear.setMinSize(100, 100);
        allClear.setMaxSize(100, 100);
        allClear.setOnAction(event -> {
            input.clear();
            memory.clear();
        });
        grid.add(allClear, 2, 5);
        buttons.put("A", allClear);
        buttons.put("a", allClear);

        createOperationButton("+", OperationType.PLUS, 1);
        createOperationButton("-", OperationType.MINUS, 2);
        createOperationButton("*", OperationType.MULT, 3);
        createOperationButton("/", OperationType.DIV, 4);

        Button equals = new Button("=");
        equals.setFont(defaultFont);
        equals.setMinSize(100, 100);
        equals.setMaxSize(100, 100);
        equals.setOnAction(event -> {
            input.setText(memory.getAction().apply(memory.getLeft(), Double.parseDouble(input.getText())).toString());
            memory.clear();
            clearOnNextInput.set(true);
        });
        grid.add(equals, 3, 5);
        buttons.put("=", equals);

        input.setOnKeyTyped(event -> {
            if (buttons.containsKey(event.getCharacter())) {
                buttons.get(event.getCharacter()).fire();
            }
            event.consume();
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
