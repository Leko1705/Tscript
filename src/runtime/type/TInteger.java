package runtime.type;

public class TInteger extends PrimitiveObject<Integer> {

    private static final TType type = new TType("Integer", null);

    public TInteger(Integer value) {
        super(value);
    }

    @Override
    public TType getType() {
        return type;
    }
}
