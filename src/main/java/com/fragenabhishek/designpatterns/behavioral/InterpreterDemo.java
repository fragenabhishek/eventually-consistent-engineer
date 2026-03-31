package com.fragenabhishek.designpatterns.behavioral;


import java.util.HashMap;
import java.util.Map;

class Context{
    private Map<String, Object> varibales;

    public Context(Map<String, Object> varibales){
        this.varibales = varibales;
    }
    public Object getValue(String key){
        return varibales.get(key);
    }
}
interface Expression{
    boolean interpret(Context context);
}

class SalaryExpression implements Expression{
    public int threshold;
    public SalaryExpression(int threshold){
        this.threshold = threshold;
    }
    @Override
    public boolean interpret(Context context) {
        int salary = (int) context.getValue("salary");
        return salary > threshold;
    }
}

class DepartmentExpression implements Expression{
    private String department;
    public DepartmentExpression(String department){
        this.department =department;
    }
    @Override
    public boolean interpret(Context context) {
        String dept = (String) context.getValue("department");
        return dept.equals(department);
    }
}

class AndExpression implements Expression {
    private Expression expr1;
    private Expression expr2;

    public AndExpression(Expression expr1, Expression expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    @Override
    public boolean interpret(Context context) {
        return expr1.interpret(context) && expr2.interpret(context);
    }
}

// OR expression (optional, but similar)
class OrExpression implements Expression {
    private Expression expr1;
    private Expression expr2;

    public OrExpression(Expression expr1, Expression expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    @Override
    public boolean interpret(Context context) {
        return expr1.interpret(context) || expr2.interpret(context);
    }
}
public class InterpreterDemo {
    public static void main(String[] args) {
        Map<String, Object> employeeData = new HashMap<>();
        employeeData.put("salary", 6000);
        employeeData.put("department", "IT");

        Context context = new Context(employeeData);

// Terminal expressions
        Expression salaryCheck = new SalaryExpression(5000);
        Expression deptCheck = new DepartmentExpression("IT");

// Non-terminal expression
        Expression rule = new AndExpression(salaryCheck, deptCheck);

// Evaluate
        boolean eligibleForBonus = rule.interpret(context);
        System.out.println("Eligible for bonus? " + eligibleForBonus);
    }
}
