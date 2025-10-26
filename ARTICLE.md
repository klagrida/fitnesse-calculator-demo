# Building Cross-Platform FitNesse Tests with GitHub Actions

A deep dive into creating a FitNesse acceptance testing framework that runs seamlessly on Ubuntu, Windows, and macOS using GitHub Actions CI/CD.

## Introduction

FitNesse is a powerful acceptance testing framework that allows you to write executable specifications in wiki format. This article walks through building a complete FitNesse project with cross-platform continuous integration, covering the challenges and solutions encountered along the way.

## Project Overview

**Goal**: Create a calculator application with FitNesse acceptance tests that run on all major platforms.

**Tech Stack**:
- Java 11
- Maven (via Maven Wrapper)
- FitNesse 20240707
- GitHub Actions for CI/CD
- Git Bash for cross-platform scripting

**Final Results**:
- 36 passing test assertions
- Runs on Ubuntu, Windows, and macOS
- Fully automated CI/CD pipeline
- Zero platform-specific code duplication

## Architecture

### Application Structure

```
fitnesse-calculator-demo/
├── src/
│   ├── main/java/
│   │   └── com/example/calculator/
│   │       └── Calculator.java              # Core application
│   └── test/java/
│       └── com/example/calculator/
│           └── fixtures/
│               └── CalculatorFixture.java   # FitNesse bridge
├── FitNesseRoot/
│   └── CalculatorTests/                     # Test specifications
│       ├── content.txt                      # Suite definition
│       ├── BasicOperations/
│       │   └── content.txt                  # 20 basic tests
│       └── EdgeCases/
│           └── content.txt                  # 16 edge case tests
└── .github/workflows/
    └── fitnesse-tests.yml                   # CI/CD workflow
```

### The Calculator Application

The `Calculator` class provides four simple operations:

```java
public class Calculator {
    public double add(double a, double b) {
        return a + b;
    }

    public double subtract(double a, double b) {
        return a - b;
    }

    public double multiply(double a, double b) {
        return a * b;
    }

    public double divide(double a, double b) {
        if (b == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return a / b;
    }
}
```

### The FitNesse Fixture

The fixture bridges FitNesse tables to the Calculator. **Critical lesson learned**: FitNesse Slim requires **setter methods**, not public fields.

```java
public class CalculatorFixture {
    private double firstNumber;
    private double secondNumber;
    private String operation;
    private Calculator calculator = new Calculator();

    // FitNesse calls these setters for each column
    public void setFirstNumber(double firstNumber) {
        this.firstNumber = firstNumber;
    }

    public void setSecondNumber(double secondNumber) {
        this.secondNumber = secondNumber;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    // FitNesse calls this for columns ending with '?'
    public Object result() {
        try {
            switch (operation.toLowerCase()) {
                case "add": return calculator.add(firstNumber, secondNumber);
                case "subtract": return calculator.subtract(firstNumber, secondNumber);
                case "multiply": return calculator.multiply(firstNumber, secondNumber);
                case "divide": return calculator.divide(firstNumber, secondNumber);
                default: return "Unknown operation: " + operation;
            }
        } catch (ArithmeticException e) {
            return "error: " + e.getMessage();
        }
    }
}
```

**Why setter methods?**
- FitNesse Slim Decision Tables map columns to setter methods
- Column "first number" → `setFirstNumber(value)`
- Column "result?" → `result()` method
- Public fields don't work with Slim's method calling convention

## FitNesse Test Syntax

### Decision Tables

FitNesse tests use **Slim Decision Tables**:

```
|Decision:com.example.calculator.fixtures.CalculatorFixture|
|first number|second number|operation|result?  |
|5           |3            |add      |8.0      |
|10          |2            |divide   |5.0      |
|-5          |3            |add      |-2.0     |
```

**Execution Flow**:
1. FitNesse creates a fixture instance
2. Calls `setFirstNumber(5)`, `setSecondNumber(3)`, `setOperation("add")`
3. Calls `result()` and gets `8.0`
4. Compares with expected `8.0`
5. Colors cell green (pass) or red (fail)

### Test Coverage

**BasicOperations (20 tests)**:
- Addition: positive, negative, zero
- Subtraction: various combinations
- Multiplication: including zero
- Division: various divisors

**EdgeCases (16 tests)**:
- Zero operations
- Negative numbers
- Decimal precision
- Large numbers (scientific notation)
- Error handling (division by zero)

## Cross-Platform CI/CD Challenge

The biggest challenge was making FitNesse tests work identically on Ubuntu, Windows, and macOS.

### Initial Problems

1. **PowerShell vs Bash**: Windows defaults to PowerShell, Unix uses bash
2. **Classpath separators**: Windows uses `;`, Unix uses `:`
3. **Background processes**: Different approaches on each OS
4. **File paths**: Backslash vs forward slash
5. **Maven wrapper**: `.mvnw` vs `mvnw.cmd`

### The Solution: Git Bash

**Key insight**: All GitHub-hosted runners include Git Bash!

By using `shell: bash` on all platforms, we achieved a unified implementation:

```yaml
- name: Start FitNesse server in background
  shell: bash
  run: |
    # Detect OS and set classpath separator
    if [ "$RUNNER_OS" == "Windows" ]; then
      CLASSPATH="libs/*;target/classes;target/test-classes"
    else
      CLASSPATH="libs/*:target/classes:target/test-classes"
    fi

    nohup java -cp "$CLASSPATH" fitnesseMain.FitNesseMain -p 8080 -e 0 -o > fitnesse.log 2>&1 &
    echo $! > fitnesse.pid
```

**Benefits**:
- Single code path for all platforms
- Standard Unix tools (`curl`, `grep`, `tail`, `kill`)
- No PowerShell/CMD complexity
- Eliminated 130+ lines of duplicate code

### Complete Workflow

```yaml
jobs:
  test:
    runs-on: ${{ matrix.os }}
    strategy:
      matrix:
        os: [ubuntu-latest, windows-latest, macos-latest]
      fail-fast: false

    steps:
    - uses: actions/checkout@v4

    - uses: actions/setup-java@v4
      with:
        java-version: '11'
        distribution: 'temurin'
        cache: maven

    - name: Build project
      shell: bash
      run: ./mvnw clean test-compile

    - name: Download FitNesse dependencies
      shell: bash
      run: ./mvnw dependency:copy-dependencies -DincludeScope=test -DoutputDirectory=libs

    - name: Start FitNesse server
      shell: bash
      run: |
        if [ "$RUNNER_OS" == "Windows" ]; then
          CLASSPATH="libs/*;target/classes;target/test-classes"
        else
          CLASSPATH="libs/*:target/classes:target/test-classes"
        fi
        nohup java -cp "$CLASSPATH" fitnesseMain.FitNesseMain -p 8080 -e 0 -o > fitnesse.log 2>&1 &
        echo $! > fitnesse.pid

    - name: Wait for FitNesse
      shell: bash
      run: |
        for i in {1..60}; do
          if curl -f -s http://localhost:8080/ > /dev/null 2>&1; then
            echo "FitNesse is ready!"
            exit 0
          fi
          sleep 2
        done
        exit 1

    - name: Run tests
      shell: bash
      run: |
        curl -f --http0.9 "http://localhost:8080/CalculatorTests?suite&format=text" > test-results.txt
        cat test-results.txt

    - name: Validate results
      shell: bash
      run: |
        if grep -q "0 Failures" test-results.txt; then
          echo "All tests passed!"
        else
          exit 1
        fi
```

## Lessons Learned

### 1. FitNesse Slim Requires Setter Methods

**Problem**: Initial implementation used public fields:
```java
public double firstNumber;  // Doesn't work!
```

**Error**: `No Method setFirstNumber[1] in class`

**Solution**: Implement setter methods for Slim's reflection-based calling:
```java
public void setFirstNumber(double firstNumber) {
    this.firstNumber = firstNumber;
}
```

### 2. Scientific Notation for Large Numbers

**Problem**: `1000000 * 1000000` returns `1.0E12`, not `1000000000000.0`

**Error**: Expected `[1000000000000.0]` but got `[1.0E12]`

**Solution**: Update test expectations to match Java's output format:
```
|1000000|1000000|multiply|1.0E12|
```

### 3. HTTP/0.9 Support

**Problem**: Modern curl rejects FitNesse's HTTP/0.9 responses

**Error**: `curl: (1) Received HTTP/0.9 when not allowed`

**Solution**: Add `--http0.9` flag:
```bash
curl -f --http0.9 "http://localhost:8080/..."
```

### 4. Cross-Platform Shell Selection

**Problem**: Windows defaults to PowerShell for `run:` steps

**Error**: `ParserError: Missing opening '(' after keyword 'for'`

**Solution**: Explicitly specify `shell: bash` on all steps

### 5. Classpath Separator Differences

**Problem**: Windows uses `;`, Unix uses `:`

**Solution**: Conditional variable setting based on `$RUNNER_OS`:
```bash
if [ "$RUNNER_OS" == "Windows" ]; then
  CLASSPATH="libs/*;target/classes;target/test-classes"
else
  CLASSPATH="libs/*:target/classes:target/test-classes"
fi
```

## Performance & Results

### Test Execution Times

- **Ubuntu**: ~1.7 seconds (fastest)
- **macOS**: ~2.0 seconds
- **Windows**: ~2.2 seconds

All platforms consistently pass all 36 assertions.

### CI/CD Workflow Efficiency

- **Parallel execution**: All 3 OS jobs run simultaneously
- **Total workflow time**: ~3 minutes (limited by slowest OS)
- **Artifact size**: ~10KB per platform (logs + results)

## Best Practices

### 1. Use Maven Wrapper

Ensures consistent Maven version across environments:
```bash
./mvnw clean test-compile  # Not 'mvn'
```

### 2. Leverage Git Bash on Windows

Avoid platform-specific scripts by using bash everywhere:
```yaml
shell: bash  # Even on Windows!
```

### 3. Handle FitNesse Startup Properly

Use proper background process handling:
```bash
nohup java -cp "$CLASSPATH" ... > fitnesse.log 2>&1 &
echo $! > fitnesse.pid  # Save PID for cleanup
```

### 4. Implement Robust Health Checks

Don't assume server is ready immediately:
```bash
for i in {1..60}; do
  if curl -f -s http://localhost:8080/ > /dev/null 2>&1; then
    exit 0
  fi
  sleep 2
done
exit 1  # Timeout after 120 seconds
```

### 5. Validate Test Results Programmatically

Don't rely on exit codes alone:
```bash
if grep -q "0 Failures" test-results.txt; then
  echo "All tests passed!"
else
  cat test-results.txt
  exit 1
fi
```

## Conclusion

Building a cross-platform FitNesse testing framework requires careful attention to:

1. **FitNesse Slim conventions** (setter methods, not public fields)
2. **OS-specific differences** (classpath separators, shell defaults)
3. **Unified scripting approach** (Git Bash on all platforms)
4. **Robust CI/CD practices** (health checks, validation, artifacts)

The end result is a maintainable, reliable testing framework that:
- ✅ Runs identically on Ubuntu, Windows, and macOS
- ✅ Uses a single code path (no duplication)
- ✅ Executes 36 assertions in ~2 seconds
- ✅ Provides comprehensive test coverage
- ✅ Integrates seamlessly with GitHub Actions

**Full source code**: [fitnesse-calculator-demo](https://github.com/klagrida/fitnesse-calculator-demo)

## Additional Resources

- [FitNesse Official Documentation](http://fitnesse.org/)
- [Slim Test System Guide](http://fitnesse.org/FitNesse.UserGuide.WritingAcceptanceTests.SliM)
- [GitHub Actions Matrix Strategy](https://docs.github.com/en/actions/using-jobs/using-a-matrix-for-your-jobs)
- [Maven Wrapper Documentation](https://maven.apache.org/wrapper/)

---

*This project demonstrates that with the right approach, acceptance testing frameworks can work seamlessly across all major platforms, enabling truly portable test automation.*
