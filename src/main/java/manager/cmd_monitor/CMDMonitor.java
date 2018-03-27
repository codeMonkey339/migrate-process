package manager.cmd_monitor;

import com.sun.org.apache.xpath.internal.Arg;
import manager.AbstractProcessManager;
import manager.parser.ArgParser;
import manager.parser.CMDParser;
import manager.parser.ParsedCMD;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CMDMonitor extends Thread {
    private final AbstractProcessManager manager;
    private final CMDParser cmdParser = new CMDParser();
    private final Logger LOGGER = Logger.getLogger(CMDMonitor.class.getName());

    public CMDMonitor(AbstractProcessManager manager){
        this.manager = manager;
    }

    @Override
    public void run(){
        monitor();
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
                Thread.sleep(AbstractProcessManager.getDURATION());
                String cmd = reader.readLine();
                ParsedCMD parsedRes = cmdParser.parse(cmd);
                processCMD(parsedRes);
            }catch (IOException e){
                LOGGER.log(Level.WARNING, e.toString());
            }catch (InterruptedException e){
                LOGGER.log(Level.INFO, "Interrupted when sleep in CMDMonitor");
            }
        }
    }


    private void processCMD(ParsedCMD parsedRes){
        switch(parsedRes.cmd){
            case PROCESS:
                processNewProcess(parsedRes);
                break;
            case PS:
                processPS(parsedRes);
                break;
            case QUIT:
                processQUIT(parsedRes);
                break;
            default:
                LOGGER.log(Level.WARNING, "Undefined cmd type{0}", parsedRes
                        .cmd);
        }
    }

    private void processNewProcess(ParsedCMD parsedCMD){
        //todo:
    }

    private void processPS(ParsedCMD parsedCMD){
        if (manager.getProcesses().size() == 0){
            System.out.println("no running processes");
        }else{
            manager.getProcesses().stream()
                    .forEach(proc -> System.out.println(proc.toString()));
        }
    }

    private void processQUIT(ParsedCMD parsedCMD){
        //todo:
    }

}
