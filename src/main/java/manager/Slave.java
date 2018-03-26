package manager;

import com.sun.corba.se.pept.encoding.OutputObject;
import manager.cmd_monitor.CMDMonitor;
import manager.entity.SocketConn;
import manager.msg.Message;
import manager.parser.ArgParser;
import manager.parser.ParsedArgs;
import processes.MigratableProcess;

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
            while(true){
                try{
                    ObjectInputStream is = conn.getIn();
                    Message query = (Message)is.readObject();
                    handleQuery(query);
                }catch (IOException e){
                    LOGGER.log(INFO, "IOException in handling queries " +
                            "from server {0}", e.toString());
                }catch (ClassNotFoundException e){
                    LOGGER.log(INFO, "ClassNotFoundException occurred " +
                            "when handling incoming query {0}", e.toString());
                }
            }
        }
    }

    private void handleQuery(Message query){
        switch(query.getType()){
            case QUERY:
                handleNumQuery(query);
            case GET:
                handleGetQuery(query);
            case MigIn:
                handleMigoutQuery(query);
            default:
                LOGGER.log(INFO,"Unknown type of incoming query {0}", query
                        .getType());
        }
    }

    private void handleNumQuery(Message query){
        //todo: need to add a block here to avoid concurrent access
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

    private void handleGetQuery(Message query){
        //todo: synchronization problem
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
        List<MigratableProcess> newProcs = (List)query.getProcesses();
        processes.addAll(newProcs);
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
            LOGGER.log(INFO, "query thread is interrupted {0}", e
                    .toString());
        }
    }


    private void connectoToMaster(String arg){
        ArgParser argParser = new ArgParser();
        ParsedArgs parsedArgs = argParser.parse(arg);
        try{
            server = new Socket(parsedArgs.getIp(), Integer.parseInt(parsedArgs
                    .getPort()));
            ObjectInputStream is = new ObjectInputStream(server
                    .getInputStream());
            ObjectOutputStream os = new ObjectOutputStream(server
                    .getOutputStream());
            conn = SocketConn.builder()
                    .client(server)
                    .in(is)
                    .out(os)
                    .build();
        }catch (IOException e){
            LOGGER.log(Level.WARNING, "Failed to connect to server");
            throw new RuntimeException(e);
        }
    }


}
