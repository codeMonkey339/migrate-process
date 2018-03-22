package manager.msg;

import lombok.Builder;
import lombok.Data;
import processes.MigratableProcess;

import java.util.List;


@Data
@Builder
public class Message {
    public enum TYPE{
        QUERY, GET, NUM, MigOut, MigIn
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
