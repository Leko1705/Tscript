package runtime.type;

public final class TNull extends PrimitiveObject<Void> {

    private static final TType type = new TType("NullType", null);

    public static final TNull NULL = new TNull();

    public static TNull getInstance(){
        return NULL;
    }

    private TNull() {
        super(null);
    }

    @Override
    public TType getType() {
        return type;
    }
}
