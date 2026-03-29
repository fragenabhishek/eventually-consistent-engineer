package com.fragenabhishek.designpatterns.behavioral;

abstract class Handler{
    protected Handler next;

    public void setNext(Handler next){
        this.next = next;
    }
    public abstract void handle(String orderStatus);
}

class SystemHandler extends Handler{

    @Override
    public void handle(String orderStatus) {
        if(orderStatus.equals("system_check")){
            System.out.println("System resolve the order issue");
        }else if(next != null){
            next.handle(orderStatus);
        }
    }
}

class AgentHandler extends Handler{

    @Override
    public void handle(String orderStatus) {
        if(orderStatus.equals("agent_check")){
            System.out.println("Agent resolve the order issue");
        }else if(next != null){
            next.handle(orderStatus);
        }
    }
}
class SupervisorHandler extends Handler{

    @Override
    public void handle(String orderStatus) {
        if(orderStatus.equals("supervisor_check")){
            System.out.println("Supervisor resolve the order issue");
        }else {
            System.out.println("Order issue not resolved");
        }
    }
}



public class CORDemo {
    public static void main(String[] args) {
        Handler system = new SystemHandler();
        Handler agent = new AgentHandler();
        Handler supervisor = new SupervisorHandler();

        system.setNext(agent);
        agent.setNext(supervisor);

        system.handle("system_check");
        system.handle("agent_check");
        system.handle("supervisor_check");
    }
}
