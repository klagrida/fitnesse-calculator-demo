package com.example.calculator.fixtures;

import com.example.calculator.Calculator;

/**
 * FitNesse fixture that bridges FitNesse test tables to the Calculator application.
 * This class uses the ColumnFixture pattern to map table columns to test inputs and outputs.
 */
public class CalculatorFixture {

    // Input fields that will be set by FitNesse from the test table
    public double firstNumber;
    public double secondNumber;
    public String operation;

    // Calculator instance to perform the actual operations
    private Calculator calculator = new Calculator();

    /**
     * This method is called by FitNesse to get the expected result.
     * It performs the specified operation on the two input numbers.
     *
     * @return the result of the calculation, or a special value for errors
     */
    public Object result() {
        try {
            switch (operation.toLowerCase()) {
                case "add":
                    return calculator.add(firstNumber, secondNumber);
                case "subtract":
                    return calculator.subtract(firstNumber, secondNumber);
                case "multiply":
                    return calculator.multiply(firstNumber, secondNumber);
                case "divide":
                    return calculator.divide(firstNumber, secondNumber);
                default:
                    return "Unknown operation: " + operation;
            }
        } catch (ArithmeticException e) {
            // Return error message for divide by zero and other arithmetic errors
            return "error: " + e.getMessage();
        }
    }
}
