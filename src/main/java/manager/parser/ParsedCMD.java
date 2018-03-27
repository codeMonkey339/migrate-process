package manager.parser;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
