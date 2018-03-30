package processes.impl;

import processes.AbstractMigratableProcessImpl;
import transactional.TransactionalFileInputStream;
import transactional.TransactionalFileOutputStream;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZipMigratableProcessImpl extends AbstractMigratableProcessImpl {
    private static final int SLEEP_TIME = 6000; // in milliseconds
    private static final int READ_SIZE = 100; // # of bytes for each read
    Logger LOGGER = Logger.getLogger(ZipMigratableProcessImpl.class.getName());
    private boolean running;

    public ZipMigratableProcessImpl(String[] arguments) {
        super(arguments);
        running = true;
    }

    public void suspend() {
        //todo: does this operation have to be synchronized?
        running = false;
    }

    public void run() {
        zipFile(super.getArgs()[1], super.getArgs()[2]);
    }

    private void zipFile(String input, String output){
        //todo: currently only write the input to output. Need to zip!
        LOGGER.log(Level.INFO, "Start zipping a file");
        TransactionalFileOutputStream os = new TransactionalFileOutputStream
                (output);
        TransactionalFileInputStream is = new TransactionalFileInputStream
                (input);
        while(running){
            try{
                Thread.sleep(SLEEP_TIME);
                byte buffer[] = new byte[READ_SIZE];
                int readn = is.read(buffer);
                if (readn < 0){
                    LOGGER.log(Level.INFO, "Finished zipping a file");
                    return;
                }else{
                    os.write(buffer, 0, readn);
                }
            }catch(InterruptedException e){
                LOGGER.log(Level.INFO, "Interrupted in sleep when zipping a file");
            }catch (IOException e){
                throw new RuntimeException("IO error when zipping the file");
            }
        }
    }
}
