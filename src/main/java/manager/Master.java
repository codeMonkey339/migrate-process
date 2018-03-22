package manager;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import manager.cmd_monitor.CMDMonitor;
import manager.msg.Message;
import processes.MigratableProcess;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
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
            //todo: concurrency problem? accessing the clients
            while(true){
                try{
                    Thread.sleep(AbstractProcessManager.getDURATION());
                }catch(InterruptedException e){
                    LOGGER.log(Level.INFO, "Thread Interrupted from " +
                            "sleep");
                }

                if (clients.size() < 2){
                    continue;
                }

                findTransNums(clients);
                transportProcesses(clients);
            }

        }

        private void transportProcesses(List<SocketConn> clients){
            List res = pullProcesses(clients);
            pushProcesses(clients, res);
        }

        private List<MigratableProcess> pullProcesses(List<SocketConn>
                                                              clients){
            //todo:
            return null;
        }


        private void pushProcesses(List<SocketConn> clients,
                                   List<MigratableProcess> processes){
            //todo:
        }



        private List<Integer> findTransNums(List<SocketConn> clients){
            List<Integer> processNums = clients.stream()
                    .map(socketConn -> getProcessNum(socketConn))
                    .collect(Collectors.toList());
            //todo: assume server won't process processes now
            Double avg = processNums.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .getAsDouble();
            List<Integer> transportNums = processNums.subList(0,
                    processNums.size() - 1).stream()
                    .map(i -> (int)Math.ceil(i - avg))
                    .collect(Collectors.toList());
            Integer offset = transportNums.stream()
                    .mapToInt(Integer::intValue)
                    .sum();
            transportNums.add(-1 * offset);
            return transportNums;
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
