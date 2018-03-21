package transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

public class TransactionalFileOutputStream extends OutputStream implements Serializable{
    /**
     * requires a void suspend(void) method which will be called before the
     * object is serialized to allow an opportunity for the process to enter
     * a known safe state
     * @return
     * @throws IOException
     */
    public void write(int b) throws IOException {

    }
}
