package manager.msg;

import lombok.Builder;
import lombok.Data;
import processes.MigratableProcess;

import java.io.Serializable;
import java.util.List;


@Data
@Builder
public class Message implements Serializable{
    private static final long serialVersionUID = 4181753163487752139L;

    public enum TYPE{
        QUERY, GET, Num, MigOut, MigIn
    }

    /**
     * the type of the message
     */
    private TYPE type;
    /**
     * the # of processes running on a slave
     */
    private int objNum;
    /**
     * the processes migrating to server
     */
    private List<MigratableProcess> processes;
}
