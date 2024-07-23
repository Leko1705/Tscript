import com.tscript.tscriptc.log.Logger;
import com.tscript.tscriptc.log.VoidLogger;
import com.tscript.tscriptc.parse.TscriptTokenType;
import com.tscript.tscriptc.parse.Token;
import com.tscript.tscriptc.parse.TscriptScanner;
import com.tscript.tscriptc.parse.UnicodeReader;
import com.tscript.tscriptc.utils.Diagnostics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static com.tscript.tscriptc.parse.TscriptTokenType.*;

public class TscriptLexerTest {

    @Test
    public void test(){
        List<TestFeature> features = new ArrayList<>();

        features.add(new TestFeature(List.of(EOF), "", true)); // test empty source
        features.add(new TestFeature(List.of(EOF), " \t", true)); // test empty source (only whitespace)
        features.add(new TestFeature(List.of(FUNCTION, IDENTIFIER), "\tfunction\n\nfoo ", true)); // test keywords and whitespaces
        features.add(new TestFeature(List.of(STRING), "\"\"", true)); // test empty string
        features.add(new TestFeature(List.of(STRING), "\"hello world\"", true)); // test non-empty string

        features.add(new TestFeature(List.of(INTEGER), "12", true)); // test integer
        features.add(new TestFeature(List.of(INTEGER), "0b011", true)); // test binary integer
        features.add(new TestFeature(List.of(INTEGER), "0b021", false)); // test invalid binary integer
        features.add(new TestFeature(List.of(INTEGER), "0o17", true)); // test octal integer
        features.add(new TestFeature(List.of(INTEGER), "0o19", false)); // test invalid octal integer
        features.add(new TestFeature(List.of(INTEGER), "0xF6A", true)); // test hex integer
        features.add(new TestFeature(List.of(INTEGER), "0xF6L", false)); // test invalid hex integer
        features.add(new TestFeature(List.of(FLOAT), "12.5", true)); // test float
        features.add(new TestFeature(List.of(FLOAT), "12.", true)); // test float
        features.add(new TestFeature(List.of(FLOAT), ".12", true)); // test float
        features.add(new TestFeature(List.of(PLUS), "+", true)); // test add
        features.add(new TestFeature(List.of(PLUS_PLUS), "++", true)); // test double add
        features.add(new TestFeature(List.of(PLUS, MINUS), "+-", true)); // test operator difference
        features.add(new TestFeature(List.of(ADD_ASSIGN), "+=", true)); // test op assign

        testFeatures(features);
    }

    private void testFeatures(List<TestFeature> features){
        int index = 0;
        for (TestFeature feature : features){
            var tokenized = tokenize(feature.code, new Logger() {
                @Override
                public void error(Diagnostics.Error error) {
                    if (feature.shouldBeEqual)
                        Assertions.fail("unexpected lexing error: " + error.getMessage());
                }

                @Override
                public void warning(Diagnostics.Warning warning) {

                }
            });
            if (feature.shouldBeEqual) {
                Assertions.assertIterableEquals(feature.tokenTypes, tokenized,
                        "for feature " + (index + 1) + ": " + feature.code + " got " + tokenized);
            }
            else {
                if (tokenized.equals(feature.tokenTypes)){
                    Assertions.assertIterableEquals(feature.tokenTypes, tokenized,
                            "mutation error failure for feature " + (index + 1) + ": " + feature.code + " got " + tokenized);
                }
            }
            index++;
        }
    }

    private List<TscriptTokenType> tokenize(String input, Logger logger){
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
        UnicodeReader unicodeReader = new UnicodeReader(bais);
        TscriptScanner scanner = new TscriptScanner(unicodeReader, logger);

        if (!scanner.hasNext()) return List.of(EOF);

        List<TscriptTokenType> tokens = new ArrayList<>();
        while (scanner.hasNext()){
            Token<TscriptTokenType> token = scanner.consume();
            Assertions.assertNotNull(token);
            tokens.add(token.getTag());
        }
        return tokens;
    }

    private record TestFeature(List<TscriptTokenType> tokenTypes, String code, boolean shouldBeEqual){}

}
