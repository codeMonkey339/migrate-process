package manager;

import manager.cmd_monitor.CMDMonitor;
import manager.parser.ArgParser;
import manager.parser.ParsedArgs;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Slave extends AbstractProcessManager {
    private Socket server;
    private final Logger LOGGER = Logger.getLogger(Slave.class.getName());

    private class QueryListener extends Thread{
        @Override
        public void run(){
            BufferedReader in;
            try{
                in = new BufferedReader(new
                        InputStreamReader(server.getInputStream()));
            }catch (IOException e){
                LOGGER.log(Level.WARNING, "Failed to create buffered input " +
                        "from the server stream");
                throw new RuntimeException(e);
            }

            while(true){
                //todo: need to handle incoming query. consider serialized objs
            }
        }
    }


    @Override
    public void run(String[] args){
        super.init();
        connectoToMaster(args[1]);
        Thread queryThread = new QueryListener();
        new CMDMonitor(this).start();
        queryThread.start();
        try{
            queryThread.join();
        }catch (InterruptedException e){
            LOGGER.log(Level.INFO, "query thread is interrupted {0}", e
                    .toString());;
        }
    }


    private void connectoToMaster(String arg){
        ArgParser argParser = new ArgParser();
        ParsedArgs parsedArgs = argParser.parse(arg);
        try{
            server = new Socket(parsedArgs.getIp(), Integer.parseInt(parsedArgs
                    .getPort()));
        }catch (IOException e){
            LOGGER.log(Level.WARNING, "Failed to connect to server");
            throw new RuntimeException(e);
        }
    }


}
