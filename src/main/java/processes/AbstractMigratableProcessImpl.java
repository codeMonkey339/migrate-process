package processes;

import lombok.Getter;

import java.util.Arrays;

public abstract class AbstractMigratableProcessImpl implements MigratableProcess {
    @Getter
    private final String[] args;
    protected boolean running;

    public AbstractMigratableProcessImpl(String[] arguments){
        args = arguments;
    }

    @Override
    public String toString(){
        StringBuffer builder = new StringBuffer();
        builder.append(this.getClass().getName());
        builder.append(" ");
        Arrays.asList(args).stream()
                .forEach(arg -> {builder.append(arg);builder.append(" ");});
        return builder.substring(0, builder.length() - 1);
    }

    @Override
    public void suspend() {
        //todo: does this operation have to be synchronized?
        running = false;
    }
}
