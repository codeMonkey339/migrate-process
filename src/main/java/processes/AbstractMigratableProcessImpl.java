package processes;

public abstract class AbstractMigratableProcessImpl implements MigratableProcess {
    private final String[] args;
    public AbstractMigratableProcessImpl(String[] arguments){
        args = arguments;
    }
}
