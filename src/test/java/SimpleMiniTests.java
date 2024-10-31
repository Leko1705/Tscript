import com.tscript.runtime.utils.Conversion;
import org.junit.jupiter.api.Test;

public class SimpleMiniTests {

    @Test
    public void test4ByteConversion() {
        byte[] bytes = Conversion.getBytes(146);
        System.out.println(Conversion.fromBytes(bytes[0], bytes[1], bytes[2], bytes[3]));
    }

}
