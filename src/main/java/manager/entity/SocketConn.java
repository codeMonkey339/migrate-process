package manager.entity;

import lombok.Builder;
import lombok.Data;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Builder
@Data
public class SocketConn{
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket client;
}
