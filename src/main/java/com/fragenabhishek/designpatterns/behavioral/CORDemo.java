package com.fragenabhishek.designpatterns.behavioral;

/*
 * =====================================================
 *  CHAIN OF RESPONSIBILITY PATTERN (Behavioral)
 * =====================================================
 *
 *  Intent:   Pass a request along a chain of handlers. Each handler decides
 *            to handle it or forward it to the next handler in the chain.
 *
 *  Problem:  A customer support system has 3 levels: System → Agent → Supervisor.
 *            Each level handles specific issue types. Without CoR, the client
 *            would need if-else logic to route to the right handler.
 *
 *  Solution: Each handler holds a reference to the next handler. When a request arrives,
 *            the handler either processes it or passes it along. The client only
 *            talks to the first handler in the chain.
 *
 *  Structure:
 *    Handler             →  Abstract handler (holds next, defines handle method)
 *    SystemHandler       →  Concrete handler (handles system-level issues)
 *    AgentHandler        →  Concrete handler (handles agent-level issues)
 *    SupervisorHandler   →  Concrete handler (terminal — handles everything remaining)
 *
 *  Key Design: setNext() returns Handler for fluent chaining:
 *              system.setNext(agent).setNext(supervisor)
 *
 *  Real-world: Spring Security filter chain, Servlet filters, logging levels, middleware pipelines
 * =====================================================
 */

// --- Abstract Handler ---
abstract class Handler {
    protected Handler next;

    // Returns Handler for fluent chaining
    public Handler setNext(Handler next) {
        this.next = next;
        return next;
    }

    public abstract void handle(String issue);
}

// --- Concrete Handlers ---

class SystemHandler extends Handler {
    @Override
    public void handle(String issue) {
        if (issue.equals("system_check")) {
            System.out.println("SystemHandler: Resolved the issue automatically");
        } else if (next != null) {
            next.handle(issue);    // can't handle → pass to next
        }
    }
}

class AgentHandler extends Handler {
    @Override
    public void handle(String issue) {
        if (issue.equals("agent_check")) {
            System.out.println("AgentHandler: Agent resolved the issue");
        } else if (next != null) {
            next.handle(issue);    // can't handle → pass to next
        }
    }
}

class SupervisorHandler extends Handler {
    @Override
    public void handle(String issue) {
        if (issue.equals("supervisor_check")) {
            System.out.println("SupervisorHandler: Supervisor resolved the issue");
        } else {
            System.out.println("SupervisorHandler: Issue could not be resolved");
        }
    }
}

// --- Demo ---
public class CORDemo {
    public static void main(String[] args) {
        // Build the chain: System → Agent → Supervisor
        Handler system = new SystemHandler();
        Handler agent = new AgentHandler();
        Handler supervisor = new SupervisorHandler();
        system.setNext(agent).setNext(supervisor);    // fluent chaining

        // All requests enter through the first handler
        System.out.println("--- Issue: system_check ---");
        system.handle("system_check");       // handled by SystemHandler

        System.out.println("--- Issue: agent_check ---");
        system.handle("agent_check");        // skipped by System → handled by Agent

        System.out.println("--- Issue: supervisor_check ---");
        system.handle("supervisor_check");   // skipped by System → Agent → handled by Supervisor

        System.out.println("--- Issue: unknown ---");
        system.handle("unknown");            // nobody handles → Supervisor reports unresolved
    }
}
