package transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class TransactionalFileInputStream extends InputStream implements Serializable{
    /**
     * requires a void suspend(void) method which will be called before the
     * object is serialized to allow an opportunity for the process to enter
     * a known safe state
     * @return
     * @throws IOException
     */
    public int read() throws IOException {
        return 0;
    }
}
