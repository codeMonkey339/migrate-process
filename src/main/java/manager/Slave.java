package manager;

import manager.parser.ArgParser;
import manager.parser.ParsedArgs;

import java.net.Socket;

public class Slave extends AbstractProcessManager {
    private Socket server;

    @Override
    public void run(String[] args){
        super.init();
        connectoToMaster(args[1]);
        super.monitor();
    }


    private void connectoToMaster(String arg){
        ArgParser argParser = new ArgParser();
        ParsedArgs parsedArgs = argParser.parse(arg);
        //todo:
    }


}
