package manager.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Serializer {

    public static byte[] serialize(Object obj) throws IOException{
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bs);
        os.writeObject(obj);

        return bs.toByteArray();
    }

    public static Object deserialize(byte[] data) throws IOException{
        //todo:
        return null;
    }
}
