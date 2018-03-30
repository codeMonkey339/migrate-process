package processes.impl;

import processes.AbstractMigratableProcessImpl;
import transactional.TransactionalFileInputStream;
import transactional.TransactionalFileOutputStream;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZipMigratableProcessImpl extends AbstractMigratableProcessImpl {
    private static final int SLEEP_TIME = 6000; // in milliseconds
    private static final int READ_SIZE = 1000; // # of bytes for each read
    //todo: how to handle transient object in serialization?
    private transient Logger LOGGER = null;
    private TransactionalFileOutputStream os = null;
    private TransactionalFileInputStream is = null;

    public ZipMigratableProcessImpl(String[] arguments) {
        super(arguments);
        running = true;
    }

    @Override
    public void run() {
        zipFile(super.getArgs()[1], super.getArgs()[2]);
    }

    private void zipFile(String input, String output){
        //todo: currently only write the input to output. Need to zip!
        if (Objects.isNull(LOGGER)){
            LOGGER = Logger.getLogger(ZipMigratableProcessImpl.class.getName());
        }
        LOGGER.log(Level.INFO, "Start zipping a file");
        if (Objects.isNull(os)){
            os = new TransactionalFileOutputStream(output);
        }
        if (Objects.isNull(is)){
            is = new TransactionalFileInputStream(input);
        }
        while(running){
            try{
                Thread.sleep(SLEEP_TIME);
                byte buffer[] = new byte[READ_SIZE];
                int readn = is.read(buffer);
                if (readn < 0){
                    LOGGER.log(Level.INFO, "Finished zipping a file");
                    return;
                }else{
                    LOGGER.log(Level.INFO, "Reading char num {0}", readn);
                    os.write(buffer, 0, readn);
                }
            }catch(InterruptedException e){
                LOGGER.log(Level.INFO, "Interrupted in sleep when zipping a " +
                        "file");
            }catch (IOException e){
                throw new RuntimeException("IO error when zipping the file");
            }
        }
    }
}
