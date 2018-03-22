package manager;

import lombok.Getter;
import manager.cmd_monitor.CMDMonitor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Master extends AbstractProcessManager{
    private final Logger LOGGER = Logger.getLogger(Master.class.getName());
    @Getter
    private String DEFAULT_PORT = "9999";
    private List<Socket> clients = new ArrayList<Socket>();

    private class ConnListener extends Thread{
        @Override
        public void run(){
            ServerSocket sock;
            try{
                sock = new ServerSocket(Integer.parseInt
                        (DEFAULT_PORT));
            }catch (IOException e){
                LOGGER.log(Level.WARNING,"Failed to create the listening " +
                        "socket on the master instance");
                throw new RuntimeException(e);
            }

            while(true){
                try{
                    Socket client = sock.accept();
                    clients.add(client);
                }catch (IOException e){
                    LOGGER.log(Level.INFO, "Failed to accept a connection " +
                            "from a new client {0}", e.toString());
                }
            }
        }
    }

    private class LoadDistributor extends Thread{
        @Override
        public void run(){
            //todo: query the load between instances and re-balance
        }
    }

    @Override
    public void run(String[] args){
        super.init();
        Thread connThread = new ConnListener();
        Thread loadThread = new LoadDistributor();
        new CMDMonitor(this).start();
        loadThread.start();
        connThread.start();
        try{
            loadThread.join();
            connThread.join();
        }catch(InterruptedException e){
            LOGGER.log(Level.INFO, "the waiting load thread is interrupted " +
                    "{0}", e.toString());

        }
    }
}
