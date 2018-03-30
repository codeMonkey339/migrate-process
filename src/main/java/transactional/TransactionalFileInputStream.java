package transactional;

import java.io.*;
import java.util.Random;

public class TransactionalFileInputStream extends InputStream implements Serializable{
    private final String filename;
    private int offset;
    private RandomAccessFile f;

    public TransactionalFileInputStream(String filename){
        this.filename = filename;
        offset = 0;
    }

    /**
     * @return
     * @throws IOException
     */
    public int read() throws IOException {
        int b = f.read();
        offset++;
        return b;
    }

    /**
     * read desired number of bytes into the provided buffer
     * @param buffer
     * @return
     * @throws IOException
     */
    @Override
    public int read(byte buffer[]) throws IOException{
        f = new RandomAccessFile(filename, "r");
        f.seek(offset);
        int readn= super.read(buffer);
        f.close();
        return readn;
    }

}
