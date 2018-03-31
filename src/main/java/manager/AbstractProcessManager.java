package manager;

import lombok.Getter;
import manager.load_balancer.LoadBalancer;
import manager.parser.CMDParser;
import manager.parser.ParsedCMD;
import processes.AbstractMigratableProcessImpl;
import processes.MigratableProcess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract public class AbstractProcessManager {
    private CMDParser parser;
    private LoadBalancer loadBalancer;
    private final static Logger LOGGER = Logger.getLogger(AbstractProcessManager
            .class.getName());
    protected List<Thread> processes = new ArrayList<>();
    protected Map<Thread, AbstractMigratableProcessImpl> threads = new HashMap<>();


    @Getter
    private static final int DURATION = 3000; // sleep time
    protected boolean running = true;

    /**
     * start monitoring the standard in for requests to launch processes
     */
    abstract public void run(String[] args);

    /**
     * initialize the process manager
     */
    protected void init(){
        parser = new CMDParser();
    }


    public static void main(String[] args){
        if (args.length <= 1){
            new Master().run(args);
        }else{
            new Slave().run(args);
        }

    }

    public void addProcess(Thread t, AbstractMigratableProcessImpl p){
        synchronized (processes){
            processes.add(t);
            threads.put(t, p);
        }
    }

    public List<Thread> getProcesses(){
        synchronized (processes){
            return processes;
        }
    }

    public Map<Thread, AbstractMigratableProcessImpl> getProcMap(){
        synchronized (threads){
            return threads;
        }
    }


    public void quit(){
        running = false;
    }

}
