package manager;

import lombok.Getter;
import manager.cmd_monitor.CMDMonitor;
import manager.entity.SocketConn;
import manager.msg.Message;
import processes.MigratableProcess;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static manager.msg.Message.TYPE.GET;
import static manager.msg.Message.TYPE.MigIn;
import static manager.msg.Message.TYPE.MigOut;

public class Master extends AbstractProcessManager{
    private final Logger LOGGER = Logger.getLogger(Master.class.getName());
    @Getter
    private String DEFAULT_PORT = "9999";
    private List<SocketConn> clients = new ArrayList();



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
            //todo: concurrency problem? accessing the clients at the same
            // time as ConnListener
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

                List<Integer> shifts = findTransNums(clients);
                transportProcesses(clients, shifts);
            }

        }

        private void transportProcesses(List<SocketConn> clients,
                                        List<Integer> shifts){
            List res = pullProcesses(clients, shifts);
            pushProcesses(clients, res, shifts);
        }

        /**
         * pull processes from overloaded clients
         * @param clients
         * @param shifts
         * @return
         */
        private List<MigratableProcess> pullProcesses(List<SocketConn>
                                                clients, List<Integer> shifts){
            List<MigratableProcess> results = new ArrayList<>();
            for (final ListIterator<Integer> it = shifts.listIterator(); it
                    .hasNext();){
                int idx = it.nextIndex();
                Integer shift = it.next();
                if (shift > 0){
                    SocketConn conn = clients.get(idx);
                    try{
                        conn.getOut().writeObject(Message.builder()
                                .type(GET)
                                .objNum(shift)
                                .build());
                        Message reply = (Message) conn.getIn().readObject();
                        if (reply.getType() != MigOut){
                            LOGGER.log(Level.INFO, "Slaved sent wrong type " +
                                    "of response to GET request {0}", reply
                                    .getType());
                        }
                        results.addAll(reply.getProcesses());
                    }catch(IOException e){
                        LOGGER.log(Level.WARNING, "Failed to pull processes " +
                                "from connection {0}", idx);
                    }catch(ClassNotFoundException e){
                        LOGGER.log(Level.WARNING, "Reading object into a not" +
                                " found Class");
                    }
                }
            }
            return results;
        }

        /**
         * push processes to underloaded clients
         * @param clients
         * @param processes
         * @param shifts
         */
        private void pushProcesses(List<SocketConn> clients,
                       List<MigratableProcess> processes, List<Integer> shifts){
            for (final ListIterator<Integer> it = shifts.listIterator(); it
                    .hasNext();){
                int idx = it.nextIndex();
                Integer shift = it.next();
                if (shift < 0){
                    SocketConn conn = clients.get(idx);
                    try{
                        List<MigratableProcess> procs;
                        if (processes.size() > (-1 * shift)){
                            procs = processes.subList
                                    (0, -1 * shift);
                            processes = processes.subList(-1 * shift,
                                    processes.size());
                        }else{
                            procs = processes.subList(0, processes.size());
                            processes.removeAll(processes);
                        }
                        conn.getOut().writeObject(Message.builder()
                                .type(MigIn)
                                .objNum(-1 * shift)
                                .processes(procs));
                        if (processes.size() == 0){
                            return;
                        }
                    }catch (IOException e){
                        LOGGER.log(Level.WARNING, "IOException when " +
                                "transferring processes to client {0}", idx);
                    }
                }
            }
        }



        private List<Integer> findTransNums(List<SocketConn> clients){
            List<Integer> processNums = clients.stream()
                    .map(socketConn -> getProcessNum(socketConn))
                    .collect(Collectors.toList());
            //todo: assume server itself won't process processes now
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
                socketConn.getOut().writeObject(query);
                Message result = (Message)socketConn.getIn().readObject();
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
