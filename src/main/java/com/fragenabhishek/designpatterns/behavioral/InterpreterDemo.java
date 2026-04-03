package com.fragenabhishek.designpatterns.behavioral;

import java.util.HashMap;
import java.util.Map;

/*
 * =====================================================
 *  INTERPRETER PATTERN (Behavioral)
 * =====================================================
 *
 *  Intent:   Define a representation for a grammar and provide an interpreter
 *            to evaluate sentences in that language.
 *
 *  Problem:  We need to evaluate dynamic business rules like:
 *            (salary > 5000) AND (department == "IT")
 *            Hardcoding such conditions leads to rigid and non-extensible code.
 *
 *  Solution: Represent each rule as an Expression object.
 *            Build a tree of expressions (AST) and evaluate it using interpret().
 *
 *  Structure:
 *    Expression            → Interface defining interpret()
 *    TerminalExpression    → Basic rules (Salary, Department)
 *    NonTerminalExpression → Combines rules (AND, OR)
 *    Context               → Holds input data for evaluation
 *
 *  Key Principle: Encapsulate grammar rules as objects and evaluate them recursively.
 *
 *  Real-world:
 *    - Rule engines (Drools)
 *    - SQL query parsing
 *    - Expression evaluators (SpEL, OGNL)
 *
 * =====================================================
 */

// ===================== CONTEXT =====================
class Context {
    private final Map<String, Object> variables;

    public Context(Map<String, Object> variables) {
        this.variables = variables;
    }

    public Object getValue(String key) {
        return variables.get(key);
    }
}

// ===================== EXPRESSION =====================
interface Expression {
    boolean interpret(Context context);
}

// ===================== TERMINAL EXPRESSIONS =====================

// Salary > threshold
class SalaryGreaterThanExpression implements Expression {
    private final int threshold;

    public SalaryGreaterThanExpression(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean interpret(Context context) {
        Integer salary = (Integer) context.getValue("salary");
        return salary != null && salary > threshold;
    }
}

// department == value
class DepartmentEqualsExpression implements Expression {
    private final String expectedDepartment;

    public DepartmentEqualsExpression(String expectedDepartment) {
        this.expectedDepartment = expectedDepartment;
    }

    @Override
    public boolean interpret(Context context) {
        String dept = (String) context.getValue("department");
        return expectedDepartment.equals(dept);
    }
}

// ===================== NON-TERMINAL EXPRESSIONS =====================

// AND expression
class AndExpression implements Expression {
    private final Expression left;
    private final Expression right;

    public AndExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean interpret(Context context) {
        return left.interpret(context) && right.interpret(context);
    }
}

// OR expression
class OrExpression implements Expression {
    private final Expression left;
    private final Expression right;

    public OrExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean interpret(Context context) {
        return left.interpret(context) || right.interpret(context);
    }
}

// NOT expression (added improvement)
class NotExpression implements Expression {
    private final Expression expression;

    public NotExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public boolean interpret(Context context) {
        return !expression.interpret(context);
    }
}

// ===================== DEMO =====================
public class InterpreterDemo {
    public static void main(String[] args) {

        // Input data
        Map<String, Object> employeeData = new HashMap<>();
        employeeData.put("salary", 6000);
        employeeData.put("department", "IT");

        Context context = new Context(employeeData);

        // Build expression tree:
        // (salary > 5000) AND (department == "IT")
        Expression salaryRule = new SalaryGreaterThanExpression(5000);
        Expression deptRule = new DepartmentEqualsExpression("IT");

        Expression rule = new AndExpression(salaryRule, deptRule);

        // Evaluate
        boolean eligibleForBonus = rule.interpret(context);

        System.out.println("Eligible for bonus? " + eligibleForBonus);
    }
}