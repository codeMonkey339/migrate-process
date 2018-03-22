package manager.cmd_monitor;

import manager.AbstractProcessManager;

public class CMDMonitor extends Thread {
    private final AbstractProcessManager manager;

    public CMDMonitor(AbstractProcessManager manager){
        this.manager = manager;
    }

    @Override
    public void run(){
        //todo: monitor the system in
    }
}
