package manager.load_balancer;

import manager.ProcessManager;

public class LoadBalancer {
    private final ProcessManager manager;

    public LoadBalancer(ProcessManager m){
        manager = m;
    }

    /**
     * Query the other instances for their load and migrate processes to
     * balance the load as well as possible. Simplified case: load is
     * considered the number of processes on an instance
     */
    public void balance(){
        //todo:
    }

}
