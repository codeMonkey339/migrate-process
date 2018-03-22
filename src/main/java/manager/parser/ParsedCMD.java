package manager.parser;


import lombok.Data;

@Data
public class ParsedCMD implements ParsedEntity{
    public enum CMD{
        PROCESS, PS, QUIT
    }
    /**
     * the parsed command
     */
    public CMD cmd;
    /**
     * input arguments to the parsed command
     */
    public String[] args;
}
