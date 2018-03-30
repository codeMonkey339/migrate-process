package manager.cmd_monitor;

import com.sun.org.apache.xpath.internal.Arg;
import manager.AbstractProcessManager;
import manager.parser.ArgParser;
import manager.parser.CMDParser;
import manager.parser.ParsedCMD;
import processes.AbstractMigratableProcessImpl;
import utils.ExceptionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.AccessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CMDMonitor extends Thread {
    private final AbstractProcessManager manager;
    private final CMDParser cmdParser = new CMDParser();
    private final Logger LOGGER = Logger.getLogger(CMDMonitor.class.getName());
    private boolean running = true;
    private final String MIGRATABLE_PROCESS_PACKAGE = "processes.impl";

    public CMDMonitor(AbstractProcessManager manager){
        this.manager = manager;
    }

    @Override
    public void run(){
        monitor();
    }

    /**
     * monitor the standard in for new requests. Master will query the other
     * instances for their load and migrate processes to balance the load as
     * well as possible. These queries should be made at a rate of once every
     * 5 seconds
     */
    protected void monitor(){
        BufferedReader reader = new BufferedReader(new InputStreamReader
                (System.in));
        while(running){
            //todo: how can this loop be more efficient
            try{
                Thread.sleep(AbstractProcessManager.getDURATION());
                String cmd = reader.readLine();
                ParsedCMD parsedRes = cmdParser.parse(cmd);
                processCMD(parsedRes);
            }catch (IOException e){
                LOGGER.log(Level.WARNING, e.toString());
            }catch (InterruptedException e){
                LOGGER.log(Level.INFO, "Interrupted when sleep in CMDMonitor");
            }
        }
        LOGGER.log(Level.INFO, "quit the CMDMonitor");
    }


    private void processCMD(ParsedCMD parsedRes){
        switch(parsedRes.cmd){
            case PROCESS:
                processNewProcess(parsedRes);
                break;
            case PS:
                processPS(parsedRes);
                break;
            case QUIT:
                processQUIT(parsedRes);
                break;
            default:
                LOGGER.log(Level.WARNING, "Undefined cmd type{0}", parsedRes
                        .cmd);
        }
    }

    /**
     * Requests come in the form <processName> [arg1] [arg2] ...
     * If an invalid class is named(does not exist or does not implement
     * MigratableProcess, then an appropriate message should be printed and
     * operations should continue as normal
     *
     * @param parsedCMD
     */
    private void processNewProcess(ParsedCMD parsedCMD){
        String args[] = parsedCMD.getArgs();
        String className = args[0];

        try{
            Class c = Class.forName(MIGRATABLE_PROCESS_PACKAGE.concat(".")
                    .concat(className));
            List<Class> interfaces = getInterfaces(c);
            List<String> requiredInterfaces = Arrays.asList("Runnable",
                    "Serializable");
            interfaces.forEach(i -> {
                        if (requiredInterfaces.contains(i)){
                            requiredInterfaces.remove(i);
                        }
                    });
            if (requiredInterfaces.size() == 0){
                String[] params = Arrays.copyOfRange(args, 1, args.length);
                List<Class> paramsTypes = Arrays.stream(params)
                        .map(i -> i.getClass())
                        .collect(Collectors.toList());
                try{
                    Constructor ctr = c.getConstructor( (Class[])paramsTypes.toArray
                            ());
                    AbstractMigratableProcessImpl proc = (AbstractMigratableProcessImpl)ctr.newInstance
                            (params);
                    proc.run();
                }catch(NoSuchMethodException e){
                    LOGGER.log(Level.INFO, "Constructor of {0} taking {1} " +
                            "doesn't exist", new Object[]{className, paramsTypes});
                }catch(InstantiationException e){
                    LOGGER.log(Level.INFO, "{0}", ExceptionUtils
                            .stackTrace2String(e));
                }catch(InvocationTargetException e){
                    LOGGER.log(Level.INFO, "{0}", ExceptionUtils
                            .stackTrace2String(e));
                }catch(IllegalAccessException e){
                    LOGGER.log(Level.INFO, "{0}", ExceptionUtils
                            .stackTrace2String(e));
                }
            }else{
                LOGGER.log(Level.INFO, "Instantiating a class that doesn't " +
                        "implement the required Runnable and Serializable " +
                        "interface");
            }
        }catch(ClassNotFoundException e){
            LOGGER.log(Level.INFO, "Instantiating a not found class {0}", className);
        }
    }

    private void processPS(ParsedCMD parsedCMD){
        if (manager.getProcesses().size() == 0){
            System.out.println("no running processes");
        }else{
            manager.getProcesses().stream()
                    .forEach(proc -> System.out.println(proc.toString()));
        }
    }

    private void processQUIT(ParsedCMD parsedCMD){
        manager.quit();
        running = false;
    }

    /**
     * get the interfaces implemented by input class and its super class
     * recursively
     * @param c
     * @return
     */
    private List<Class> getInterfaces(Class c){
        Class superClass = c.getSuperclass();
        List<Class> result = new ArrayList<>();
        List<Class> parentInterfaces = new ArrayList<>();
        if (Objects.nonNull(superClass)){
            parentInterfaces = getInterfaces(superClass);
        }
        Class interfaces[] = c.getInterfaces();
        result = Arrays.asList(interfaces);
        if (parentInterfaces.size() > 0){
            return Stream.concat(parentInterfaces.stream(), result.stream())
                    .collect(Collectors.toList());
        }else{
            return result;
        }
    }

}
