package manager;

import manager.cmd_monitor.CMDMonitor;
import manager.entity.SocketConn;
import manager.msg.Message;
import manager.parser.ArgParser;
import manager.parser.ParsedArgs;
import processes.AbstractMigratableProcessImpl;
import processes.MigratableProcess;
import utils.ExceptionUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static manager.msg.Message.TYPE.MigOut;
import static manager.msg.Message.TYPE.Num;

public class Slave extends AbstractProcessManager {
    private Socket server;
    private SocketConn conn;
    private final Logger LOGGER = Logger.getLogger(Slave.class.getName());
    private Thread queryThread;
    private Thread cmdThread;

    private class QueryListener extends Thread{
        @Override
        public void run(){
            LOGGER.log(INFO, "Starting a query listening");
            while(running){
                try{
                    Thread.sleep(AbstractProcessManager.getDURATION());
                    ObjectInputStream is = conn.getIn();
                    Message query = (Message)is.readObject();
                    handleQuery(query);
                }catch (IOException e){
                    LOGGER.log(INFO, "IOException in handling queries " +
                            "from server {0}\n{1}", new Object[]{e.toString(),
                            ExceptionUtils.stackTrace2String(e)});
                }catch (ClassNotFoundException e){
                    LOGGER.log(INFO, "ClassNotFoundException occurred " +
                            "when handling incoming query {0}", e.toString());
                }catch (InterruptedException e){
                    LOGGER.log(INFO, "Sleep interrupted");
                }
            }
            LOGGER.log(INFO, "Quiting the slave query listener");
        }

    }

    private void handleQuery(Message query){
        switch(query.getType()){
            case QUERY:
                handleNumQuery(query);
                break;
            case GET:
                handleGetQuery(query);
                break;
            case MigIn:
                handleMiginQuery(query);
                break;
            default:
                LOGGER.log(INFO,"Unknown type of incoming query {0}", query
                        .getType());
        }
    }

    private void handleNumQuery(Message query){
        LOGGER.log(INFO, "Slave handling incoming query for number of " +
                "running processes");
        synchronized (processes){
            Message reply = Message.builder()
                    .type(Num)
                    .objNum(processes.size())
                    .processes(new ArrayList<>())
                    .build();
            ObjectOutputStream os = conn.getOut();
            try{
                os.writeObject(reply);
            }catch (IOException e){
                LOGGER.log(WARNING, "IOException occurred when reply numer of " +
                        "processes running on the slave");
            }
        }
    }

    /**
     * reply to master the number of processes running on the slave
     * @param query
     */
    private void handleGetQuery(Message query){
        LOGGER.log(INFO, "Slave handling incoming query to pull overloaded " +
                "processes");
        synchronized (processes){
            Integer num = query.getObjNum();
            List<Thread> shifts = new ArrayList<>(processes.subList
                    (0, num));
            List<MigratableProcess> procs = shifts.stream()
                    .map(t -> {
                        AbstractMigratableProcessImpl p =threads.get(t);
                        p.suspend();
                        threads.remove(t);
                        return p;
                    })
                    .collect(Collectors.toList());
            ObjectOutputStream os = conn.getOut();
            Message reply = Message.builder()
                    .type(MigOut)
                    .objNum(num)
                    .processes(procs)
                    .build();
            try{
                os.writeObject(reply);
                processes.removeAll(shifts);
                LOGGER.log(INFO, "Transferring out {0} processes", shifts.size());
            }catch (IOException e){
                LOGGER.log(WARNING, "IOException occurred when reply numer of " +
                        "processes running on the slave.\n{0}", ExceptionUtils
                        .stackTrace2String(e));
            }
        }
    }

    private void handleMiginQuery(Message query){
        LOGGER.log(INFO, "Slave receiving {0} incoming processes", query
                .getProcesses().size());
        synchronized (processes){
            List<MigratableProcess> newProcs = (List)query.getProcesses();
            List<Thread> newThreads = newProcs.stream()
                    .map(p -> {
                        Thread t = new Thread(p);
                        t.start();
                        return t;
                    })
                    .collect(Collectors.toList());
            processes.addAll(newThreads);
            LOGGER.log(INFO, "Tranferring in {0} processes", newProcs.size());
        }

    }


    @Override
    public void run(String[] args){
        LOGGER.log(INFO, "Staring a slave");
        super.init();
        connectoToMaster(args[1]);
        queryThread = new QueryListener();
        cmdThread = new CMDMonitor(this);
        queryThread.start();
        cmdThread.start();
        try{
            queryThread.join();
            cmdThread.join();
            conn.getClient().close();
            LOGGER.log(INFO, "quiting the slave process manager");
        }catch (InterruptedException e){
            LOGGER.log(INFO, "query thread is interrupted {0}", e
                    .toString());
        }catch(IOException e){
            LOGGER.log(INFO, "IOException in closing socket connected to " +
                    "server");
        }
    }

    @Override
    public void quit(){
        running = false;
        queryThread.interrupt();
    }

    private void connectoToMaster(String arg){
        ArgParser argParser = new ArgParser();
        ParsedArgs parsedArgs = argParser.parse(arg);
        LOGGER.log(INFO, "Slave connecting to host at {0}:{1}", new Object[]{parsedArgs
                .getIp(), parsedArgs.getPort()});
        try{
            server = new Socket(parsedArgs.getIp(), Integer.parseInt(parsedArgs
                    .getPort()));
            ObjectOutputStream os = new ObjectOutputStream(server
                    .getOutputStream());
            ObjectInputStream is = new ObjectInputStream(server
                    .getInputStream());
            conn = SocketConn.builder()
                    .client(server)
                    .in(is)
                    .out(os)
                    .build();
            LOGGER.log(INFO, "Slave established socket connection with " +
                    "master at {0}:{1}", new Object[]{parsedArgs.getIp(),
                    parsedArgs.getPort()});
        }catch (IOException e){
            LOGGER.log(Level.WARNING, "Failed to connect to server");
            throw new RuntimeException(e);
        }
    }

}
