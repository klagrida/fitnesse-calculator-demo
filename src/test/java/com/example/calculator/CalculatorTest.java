package com.example.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Calculator class.
 * These tests verify the basic functionality of calculator operations.
 */
class CalculatorTest {

    private Calculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Calculator();
    }

    @Test
    @DisplayName("Addition of two positive numbers")
    void testAddPositiveNumbers() {
        assertEquals(8.0, calculator.add(5, 3));
        assertEquals(30.0, calculator.add(10, 20));
    }

    @Test
    @DisplayName("Addition with negative numbers")
    void testAddNegativeNumbers() {
        assertEquals(-2.0, calculator.add(-5, 3));
        assertEquals(-8.0, calculator.add(-5, -3));
    }

    @Test
    @DisplayName("Subtraction of two numbers")
    void testSubtract() {
        assertEquals(5.0, calculator.subtract(10, 5));
        assertEquals(-5.0, calculator.subtract(5, 10));
        assertEquals(-1.0, calculator.subtract(-3, -2));
    }

    @Test
    @DisplayName("Multiplication of two numbers")
    void testMultiply() {
        assertEquals(20.0, calculator.multiply(4, 5));
        assertEquals(-12.0, calculator.multiply(-3, 4));
        assertEquals(0.0, calculator.multiply(0, 100));
    }

    @Test
    @DisplayName("Division of two numbers")
    void testDivide() {
        assertEquals(5.0, calculator.divide(10, 2));
        assertEquals(5.0, calculator.divide(15, 3));
        assertEquals(25.0, calculator.divide(100, 4));
    }

    @Test
    @DisplayName("Division by zero throws ArithmeticException")
    void testDivideByZero() {
        ArithmeticException exception = assertThrows(
            ArithmeticException.class,
            () -> calculator.divide(10, 0)
        );
        assertEquals("Cannot divide by zero", exception.getMessage());
    }

    @Test
    @DisplayName("Operations with zero")
    void testOperationsWithZero() {
        assertEquals(5.0, calculator.add(0, 5));
        assertEquals(0.0, calculator.multiply(0, 100));
        assertEquals(5.0, calculator.subtract(5, 0));
    }

    @Test
    @DisplayName("Operations with decimal numbers")
    void testDecimalOperations() {
        assertEquals(7.8, calculator.add(5.5, 2.3), 0.0001);
        assertEquals(10.0, calculator.multiply(2.5, 4.0), 0.0001);
    }

    @Test
    @DisplayName("Operations with large numbers")
    void testLargeNumbers() {
        assertEquals(1000000000000.0, calculator.multiply(1000000, 1000000));
        assertEquals(1000000.0, calculator.add(999999, 1));
    }
}
