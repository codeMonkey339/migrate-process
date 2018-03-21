package manager.parser;

public class CMDParser {

    /**
     * breaks a string apart. Should not crash on unrecognized input
     * e.g "This lab is fun" -> {"This", "lab", "is", "fun"}
     *
     * currently only 3 ops are supported:
     * 1. <processName> arg1 arg2 ...
     *  1.1 if "-c <hostname>", then this instance should be run as a slave
     *  and connect to the master running on <hostname>, otherwise run as a
     *  master
     * 2. ps (prints a list of local running processes and their arguments)
     * 3. quit (exit the ProcessManager)
     *
     * @param input
     * @return
     */
    public String[] parse(String input){
        //todo: parse the input string into
        return null;
    }
}
