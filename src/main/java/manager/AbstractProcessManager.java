package manager;

import lombok.Getter;
import manager.load_balancer.LoadBalancer;
import manager.parser.CMDParser;
import manager.parser.ParsedCMD;
import processes.MigratableProcess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract public class AbstractProcessManager {
    private CMDParser parser;
    private LoadBalancer loadBalancer;
    private final static Logger LOGGER = Logger.getLogger(AbstractProcessManager
            .class.getName());
    @Getter
    protected List<MigratableProcess> processes = new ArrayList<>();


    @Getter
    private static final int DURATION = 500; // sleep time

    /**
     * start monitoring the standard in for requests to launch processes
     */
    abstract public void run(String[] args);

    /**
     * initialize the process manager
     */
    protected void init(){
        parser = new CMDParser();
        loadBalancer = new LoadBalancer(this);
    }


    public static void main(String[] args){
        //todo: main in an abstract class?
        //todo: based on args, instantiate different type
        if (args.length <= 1){
            new Master().run(args);
        }else{
            new Slave().run(args);
        }

    }
    
}
