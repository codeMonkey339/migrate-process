package manager;

import manager.load_balancer.LoadBalancer;
import manager.parser.CMDParser;
import manager.parser.ParsedCMD;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract public class AbstractProcessManager {
    private CMDParser parser;
    private LoadBalancer loadBalancer;
    private final static Logger LOGGER = Logger.getLogger(AbstractProcessManager
            .class.getName());

    private static final int DURATION = 5; // frequency of load balancing

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

    /**
     * monitor the standard in for new requests. Master will query the other
     * instances for their load and migrate processes to balance the load as
     * well as possible. These queries should be made at a rate of once every
     * 5 seconds
     */
    protected void monitor(){
        BufferedReader reader = new BufferedReader(new InputStreamReader
                (System.in));
        while(true){
            //todo: how can this loop be more efficient
            try{
                String cmd = reader.readLine();
                ParsedCMD parsedRes = parser.parse(cmd);
                processCMD(parsedRes);
            }catch (IOException e){
                LOGGER.log(Level.WARNING, e.toString());
            }
        }
    }

    private void processCMD(ParsedCMD parsedRes){
        switch(parsedRes.cmd){
            case PROCESS:
                processNewProcess(parsedRes);
            case PS:
                processPS(parsedRes);
            case QUIT:
                processQUIT(parsedRes);
            default:
                LOGGER.log(Level.WARNING, "Undefined cmd type{0}", parsedRes
                        .cmd);
        }
    }

    private void processNewProcess(ParsedCMD parsedCMD){
        //todo:
    }

    private void processPS(ParsedCMD parsedCMD){
        //todo:
    }

    private void processQUIT(ParsedCMD parsedCMD){
        //todo:
    }




    public static void main(String[] args){
        //todo: main in an abstract class?
        //todo: based on args, instantiate different type

    }
}
