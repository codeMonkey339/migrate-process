package manager.parser;


import lombok.Data;

@Data
public class ParsedArgs implements ParsedEntity{
    private String ip;
    private String port;
}
