package processes;

import lombok.Getter;

import java.util.Arrays;

public abstract class AbstractMigratableProcessImpl implements MigratableProcess {
    @Getter
    private final String[] args;
    public AbstractMigratableProcessImpl(String[] arguments){
        args = arguments;
    }

    @Override
    public String toString(){
        StringBuffer builder = new StringBuffer();
        Arrays.asList(args).stream()
                .forEach(arg -> {builder.append(args);builder.append(" ");});
        return builder.substring(0, builder.length() - 1);
    }
}
