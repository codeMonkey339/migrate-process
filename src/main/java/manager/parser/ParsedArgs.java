package manager.parser;


import lombok.Data;
import lombok.Getter;

@Data
public class ParsedArgs implements ParsedEntity{
    @Getter
    private String ip;
    @Getter
    private String port;
}
