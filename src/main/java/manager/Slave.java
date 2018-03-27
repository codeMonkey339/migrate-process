package manager;

import manager.cmd_monitor.CMDMonitor;
import manager.entity.SocketConn;
import manager.msg.Message;
import manager.parser.ArgParser;
import manager.parser.ParsedArgs;
import processes.MigratableProcess;
import utils.ExceptionUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static manager.msg.Message.TYPE.MigOut;
import static manager.msg.Message.TYPE.Num;

public class Slave extends AbstractProcessManager {
    private Socket server;
    private SocketConn conn;
    private final Logger LOGGER = Logger.getLogger(Slave.class.getName());
    List<MigratableProcess> processes = new ArrayList<>();

    private class QueryListener extends Thread{
        @Override
        public void run(){
            LOGGER.log(INFO, "Starting a query listening");
            while(true){
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
                handleMigoutQuery(query);
                break;
            default:
                LOGGER.log(INFO,"Unknown type of incoming query {0}", query
                        .getType());
        }
    }

    private void handleNumQuery(Message query){
        //todo: need to add a block here to avoid concurrent access
        LOGGER.log(INFO, "Slave handling incoming query for number of " +
                "running processes");
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

    /**
     * reply to master the number of processes running on the slave
     * @param query
     */
    private void handleGetQuery(Message query){
        //todo: synchronization problem
        LOGGER.log(INFO, "Slave handling incoming query to pull overloaded " +
                "processes");
        Integer num = query.getObjNum();
        List<MigratableProcess> shifts = processes.subList(0, num);
        ObjectOutputStream os = conn.getOut();
        Message reply = Message.builder()
                .type(MigOut)
                .objNum(num)
                .processes(shifts)
                .build();
        try{
            os.writeObject(reply);
        }catch (IOException e){
            LOGGER.log(WARNING, "IOException occurred when reply numer of " +
                    "processes running on the slave");
        }

    }

    private void handleMigoutQuery(Message query){
        //todo: synchronization
        LOGGER.log(INFO, "Slave receiving incoming processes");
        List<MigratableProcess> newProcs = (List)query.getProcesses();
        processes.addAll(newProcs);
    }


    @Override
    public void run(String[] args){
        LOGGER.log(INFO, "Staring a slave");
        super.init();
        connectoToMaster(args[1]);
        Thread queryThread = new QueryListener();
        new CMDMonitor(this).start();
        queryThread.start();
        try{
            queryThread.join();
        }catch (InterruptedException e){
            LOGGER.log(INFO, "query thread is interrupted {0}", e
                    .toString());
        }
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
