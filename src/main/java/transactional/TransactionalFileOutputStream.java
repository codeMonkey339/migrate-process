package transactional;

import java.io.*;
import java.util.Random;

public class TransactionalFileOutputStream extends OutputStream implements Serializable{
    private final String filename;
    private int offset;
    private RandomAccessFile f;

    public TransactionalFileOutputStream(String filname){
        this.filename = filname;
        offset = 0;
    }

    /**
     * provides an implementation for the abstract write method
     * @return
     * @throws IOException
     */
    public void write(int b) throws IOException {
        f.write(b);
        offset++;
    }

    /**
     * actually this overriding method is unnecesary
     * @param data
     * @throws IOException
     */
    @Override
    public void write(byte[] data) throws IOException{
        f = new RandomAccessFile(filename, "rw");
        super.write(data);
        f.close();
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException{
        f = new RandomAccessFile(filename, "rw");
        super.write(b, off, len);
        f.close();
    }

}
