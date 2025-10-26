# FitNesse Calculator Demo

![FitNesse Tests](https://github.com/klagrida/fitnesse-calculator-demo/workflows/FitNesse%20Tests/badge.svg)

A demonstration project showing FitNesse acceptance testing with a simple calculator application. This project illustrates how to write executable specifications that serve as both documentation and automated tests.

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [Running Tests](#running-tests)
- [Continuous Integration](#continuous-integration)
- [Understanding the Code](#understanding-the-code)
- [FitNesse Test Syntax](#fitnesse-test-syntax)
- [Resources](#resources)

## Overview

This project demonstrates:
- A simple Java calculator application with basic arithmetic operations
- FitNesse acceptance tests written as executable specifications
- Integration between FitNesse and Java code using fixtures
- Automated testing with GitHub Actions CI
- Best practices for acceptance test-driven development (ATDD)

## Prerequisites

- **Java**: JDK 11 or higher
- **Maven**: 3.6 or higher (optional - project includes Maven Wrapper)
- **Git**: For version control

To verify your Java installation:
```bash
java -version
```

**Note**: This project includes the Maven Wrapper, so you don't need to install Maven separately. You can use `./mvnw` (Linux/Mac) or `mvnw.cmd` (Windows) instead of `mvn`.

## Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/klagrida/fitnesse-calculator-demo.git
cd fitnesse-calculator-demo
```

### 2. Make Maven Wrapper Executable (Linux/Mac only)
```bash
chmod +x mvnw
```

**Note**: On Windows, you can skip this step and use `mvnw.cmd` directly.

### 3. Build the Project

Using Maven Wrapper (recommended):
```bash
# Linux/Mac
./mvnw clean compile

# Windows
mvnw.cmd clean compile
```

Or with your local Maven installation:
```bash
mvn clean compile
```

### 4. Start the FitNesse Server

Using Maven Wrapper:
```bash
# Linux/Mac
./mvnw exec:java -Dexec.mainClass="fitnesseMain.FitNesseMain" -Dexec.args="-p 8080"

# Windows
mvnw.cmd exec:java -Dexec.mainClass="fitnesseMain.FitNesseMain" -Dexec.args="-p 8080"
```

Or with local Maven:
```bash
mvn exec:java -Dexec.mainClass="fitnesseMain.FitNesseMain" -Dexec.args="-p 8080"
```

### 5. Access Tests in Browser
Open your web browser and navigate to:
```
http://localhost:8080/CalculatorTests
```

### 6. Run the Test Suite
Click the **Suite** button on the CalculatorTests page to run all tests.

## Project Structure

```
fitnesse-calculator-demo/
├── pom.xml                           # Maven build configuration
├── .github/
│   └── workflows/
│       └── fitnesse-tests.yml        # GitHub Actions CI workflow
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── example/
│   │               └── calculator/
│   │                   └── Calculator.java              # Application code
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── calculator/
│                       ├── fixtures/
│                       │   └── CalculatorFixture.java   # FitNesse fixture
│                       └── CalculatorTest.java          # Unit tests
├── FitNesseRoot/
│   └── CalculatorTests/              # FitNesse test pages
│       ├── content.txt               # Suite page
│       ├── BasicOperations/          # Basic operation tests
│       │   └── content.txt
│       └── EdgeCases/                # Edge case tests
│           └── content.txt
└── README.md                         # This file
```

## Running Tests

### Running Tests Locally via Browser

1. Start the FitNesse server (see Quick Start step 3)
2. Navigate to http://localhost:8080/CalculatorTests
3. Click the **Suite** button to run all tests
4. Click individual test pages (BasicOperations, EdgeCases) to run specific tests

**Expected Output:**
- Green cells indicate passing tests
- Red cells indicate failing tests
- The summary shows total tests, passes, and failures

### Running Tests via Command Line

For headless execution (useful for CI/CD):

```bash
# Start FitNesse server in background (Linux/Mac)
./mvnw exec:java -Dexec.mainClass="fitnesseMain.FitNesseMain" \
  -Dexec.args="-p 8080 -e 0" \
  -Dexec.classpathScope=test &

# Or on Windows
mvnw.cmd exec:java -Dexec.mainClass="fitnesseMain.FitNesseMain" ^
  -Dexec.args="-p 8080 -e 0" ^
  -Dexec.classpathScope=test

# Wait for server to start
sleep 10

# Run tests via HTTP API
curl "http://localhost:8080/CalculatorTests?suite&format=text"
```

### Running Unit Tests

Using Maven Wrapper:
```bash
# Linux/Mac
./mvnw test

# Windows
mvnw.cmd test
```

Or with local Maven:
```bash
mvn test
```

## Continuous Integration

This project includes a GitHub Actions workflow that:

1. Builds the project on every push/PR to the main branch
2. Starts FitNesse server
3. Executes the complete test suite
4. Reports test results
5. Uploads test artifacts

The workflow configuration is in `.github/workflows/fitnesse-tests.yml`.

### Viewing CI Results

- Check the **Actions** tab in your GitHub repository
- Each workflow run shows build status and test results
- Test result artifacts are available for download

## Understanding the Code

### Calculator.java

The main application class with four methods:
- `add(a, b)` - Addition
- `subtract(a, b)` - Subtraction
- `multiply(a, b)` - Multiplication
- `divide(a, b)` - Division (throws ArithmeticException for division by zero)

Location: `src/main/java/com/example/calculator/Calculator.java`

### CalculatorFixture.java

The bridge between FitNesse and the Calculator application:
- Accepts inputs from FitNesse test tables
- Calls the Calculator methods
- Returns results back to FitNesse
- Handles errors gracefully

Location: `src/test/java/com/example/calculator/fixtures/CalculatorFixture.java`

### FitNesse Test Pages

Test specifications written in FitNesse wiki syntax:
- **CalculatorTests** - Suite page containing all tests
- **BasicOperations** - Tests for typical use cases (25 test cases)
- **EdgeCases** - Tests for boundary conditions and error cases (12+ test cases)

## FitNesse Test Syntax

FitNesse tests use decision tables with the following structure:

```
|Table: com.example.calculator.fixtures.CalculatorFixture|
|first number|second number|operation|result?                |
|5           |3            |add      |8.0                    |
|10          |2            |divide   |5.0                    |
```

**Explanation:**
- First row: Fixture class name
- Second row: Column headers (map to fixture fields and methods)
- Remaining rows: Test data
- Columns ending with `?` indicate expected results

## Test Cases

### BasicOperations Tests (25 cases)
- Addition: 5 test cases including positive, negative, and zero
- Subtraction: 5 test cases including positive, negative, and zero
- Multiplication: 5 test cases including zero and negative numbers
- Division: 5 test cases with various divisors

### EdgeCases Tests (12+ cases)
- Operations with zero
- Negative number operations
- Decimal/floating-point numbers
- Large numbers
- Division by zero (error handling)

## Troubleshooting

### FitNesse Server Won't Start
- Ensure port 8080 is not in use
- Check Java version (must be 11+)
- Verify Maven dependencies are downloaded

### Tests Won't Run
- Ensure project is compiled: `mvn clean compile`
- Check that classpath is configured in FitNesse pages
- Verify fixture class name matches package structure

### Tests Fail Unexpectedly
- Check for floating-point precision issues
- Verify expected values in test tables
- Review error messages in red cells

## Resources

### FitNesse Documentation
- [Official FitNesse Website](http://fitnesse.org/)
- [FitNesse User Guide](http://fitnesse.org/FitNesse.UserGuide)
- [Slim Test System](http://fitnesse.org/FitNesse.UserGuide.WritingAcceptanceTests.SliM)

### Related Concepts
- [Acceptance Test-Driven Development (ATDD)](https://en.wikipedia.org/wiki/Acceptance_test-driven_development)
- [Behavior-Driven Development (BDD)](https://en.wikipedia.org/wiki/Behavior-driven_development)
- [Executable Specifications](https://martinfowler.com/bliki/SpecificationByExample.html)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is open source and available under the [MIT License](LICENSE).

## Author

Created as a demonstration project for teaching FitNesse acceptance testing.

---

**Note**: This is a demo project intended for educational purposes. For production use, consider adding error handling, logging, and more comprehensive test coverage.
