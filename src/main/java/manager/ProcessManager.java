package manager;

import manager.load_balancer.LoadBalancer;
import manager.parser.CMDParser;

public class ProcessManager {
    private CMDParser parser;
    private LoadBalancer loadBalancer;

    private static final int DURATION = 5; // frequency of load balancing

    /**
     * start monitoring the standard in for requests to launch processes
     */
    public void run(){
        init();
        monitor();
    }

    /**
     * initialize the process manager
     */
    private void init(){
        parser = new CMDParser();
        loadBalancer = new LoadBalancer(this);
    }

    /**
     * monitor the standard in for new requests. Master will query the other
     * instances for their load and migrate processes to balance the load as
     * well as possible. These queries should be made at a rate of once every
     * 5 seconds
     */
    private void monitor(){
        while(true){
            //todo: scan system in for input string
        }
    }


    public static void main(String[] args){
        //todo: try to use log4j
        ProcessManager manager = new ProcessManager();
        manager.run();
    }
}
