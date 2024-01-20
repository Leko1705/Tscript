package runtime.type;

public class TBoolean extends PrimitiveObject<Boolean> {

    private static final TType type = new TType("Boolean", null);

    public static final TBoolean TRUE = new TBoolean(true);
    public static final TBoolean FALSE = new TBoolean(false);

    public static TBoolean of(boolean b){
        return b ? TRUE : FALSE;
    }

    public static TBoolean of(int i){
        return i == 0 ? FALSE : TRUE;
    }

    private TBoolean(Boolean value) {
        super(value);
    }

    @Override
    public TType getType() {
        return type;
    }
}
