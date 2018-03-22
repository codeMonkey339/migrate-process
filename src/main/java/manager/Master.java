package manager;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import manager.cmd_monitor.CMDMonitor;
import manager.msg.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Master extends AbstractProcessManager{
    private final Logger LOGGER = Logger.getLogger(Master.class.getName());
    @Getter
    private String DEFAULT_PORT = "9999";
    private List<SocketConn> clients = new ArrayList();

    @Builder
    @Data
    private class SocketConn{
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private Socket client;
    }


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
                    ObjectInputStream is = new ObjectInputStream(client
                            .getInputStream());
                    ObjectOutputStream os = new ObjectOutputStream(client
                            .getOutputStream());
                    clients.add(SocketConn.builder()
                            .client(client)
                            .in(is)
                            .out(os)
                            .build());
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
            while(true){
                try{
                    Thread.sleep(AbstractProcessManager.getDURATION());
                }catch(InterruptedException e){
                    LOGGER.log(Level.INFO, "Thread Interrupted from " +
                            "sleep");
                }
                List<Integer> processNums = clients.stream()
                        .map(socketConn -> getProcessNum(socketConn))
                        .collect(Collectors.toList());
                //todo: assume server won't process processes now
                Integer totalNum = processNums.stream()
                        .mapToInt(Integer::intValue)
                        .sum();

            }

        }


        /**
         * query the # of processes running a slave
         * @param socketConn
         * @return
         */
        private int getProcessNum(SocketConn socketConn){
            Message query = Message.builder()
                    .type(Message.TYPE.QUERY)
                    .build();
            try{
                socketConn.out.writeObject(query);
                Message result = (Message)socketConn.in.readObject();
                return result.getObjNum();
            }catch(IOException e){
                LOGGER.log(Level.WARNING, "Failed to serialize a query " +
                        "message {0}", e.toString());
                        return 0;
            }catch (ClassNotFoundException e){
                LOGGER.log(Level.WARNING, "Failed to read object from client" +
                        " {0}", e.toString());
                return 0;
            }
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
