package manager.parser;

public class ArgParser implements Parser{
    @Override
    public ParsedArgs parse(String args){
        String[] server = args.split(":");
        return ParsedArgs.builder()
                .ip(server[0])
                .port(server[1])
                .build();
    }
}
