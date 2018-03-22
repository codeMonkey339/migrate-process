package processes;

import java.io.Serializable;

public interface MigratableProcess extends Runnable, Serializable {
    /**
     * called before the object is serialized to allow an opportunity for the
     * process to enter a known safe state
     */
    void suspend();

    /**
     * print the name of the process as well as the original set of arguments
     * with which it was called
     * @return
     */
    String toString();
}
