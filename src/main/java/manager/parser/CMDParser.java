package manager.parser;


public class CMDParser implements Parser{



    /**
     * breaks a string apart. Should not crash on unrecognized input
     * e.g "This lab is fun" -> {"This", "lab", "is", "fun"}
     *
     * currently only 3 ops are supported:
     * 1. <processName> arg1 arg2 ...
     * 2. ps (prints a list of local running processes and their arguments)
     * 3. quit (exit the AbstractProcessManager)
     *
     * @param input
     * @return
     */
    public ParsedCMD parse(String input){
        String[] tokens = input.split(" ");
        switch(tokens[0]){
            case "ps":
                return ParsedCMD.builder()
                        .cmd(ParsedCMD.CMD.PS)
                        .args(new String[0])
                        .build();
            case "quit":
                return ParsedCMD.builder()
                        .cmd(ParsedCMD.CMD.QUIT)
                        .args(new String[0])
                        .build();
            default:
                return ParsedCMD.builder()
                        .cmd(ParsedCMD.CMD.PROCESS)
                        .args(tokens)
                        .build();
        }
    }
}
