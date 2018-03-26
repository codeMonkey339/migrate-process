package manager.parser;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParsedArgs implements ParsedEntity{
    private String ip;
    private String port;
}
