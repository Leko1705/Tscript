package runtime.jit;

import com.tscript.runtime.core.ALU;
import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.jit.comp.ir.types.UnknownType;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Checks that constant folding and type inference of the JIT agree with the interpreter (ALU).
 */
public class CanonicalizerTest {

    private static final List<TObject> VALUES = List.of(
            new TInteger(-3), new TInteger(0), new TInteger(1), new TInteger(7), new TInteger(Integer.MAX_VALUE),
            new TReal(-2.5), new TReal(0.0), new TReal(-0.0), new TReal(1.0), new TReal(Double.NaN),
            TBoolean.TRUE, TBoolean.FALSE,
            new TString(""), new TString("ab"),
            Null.INSTANCE
    );

    private static final List<Opcode> BINARY = List.of(
            Opcode.ADD, Opcode.SUB, Opcode.MUL, Opcode.DIV, Opcode.IDIV, Opcode.MOD, Opcode.POW,
            Opcode.AND, Opcode.OR, Opcode.XOR, Opcode.SHL, Opcode.SHR, Opcode.USHR,
            Opcode.LT, Opcode.GT, Opcode.LEQ, Opcode.GEQ);

    private static BiFunction<IRValue, IRValue, IRValue> operation(Graph.Builder b, Opcode op) {
        return switch (op) {
            case ADD -> b::add;
            case SUB -> b::sub;
            case MUL -> b::mul;
            case DIV -> b::div;
            case IDIV -> b::idiv;
            case MOD -> b::mod;
            case POW -> b::pow;
            case AND -> b::and;
            case OR -> b::or;
            case XOR -> b::xor;
            case SHL -> b::shl;
            case SHR -> b::shr;
            case USHR -> b::ushr;
            case LT -> b::lt;
            case GT -> b::gt;
            case LEQ -> b::leq;
            case GEQ -> b::geq;
            default -> throw new AssertionError(op);
        };
    }

    private static IRValue literal(Graph.Builder b, TObject obj) {
        if (obj instanceof TInteger i) return b.integer(i.getValue());
        if (obj instanceof TReal r) return b.real(r.getValue());
        if (obj instanceof TBoolean bool) return b.bool(bool.getValue());
        if (obj instanceof TString s) return b.string(s.getValue());
        return b.nullValue();
    }

    private static TObject toObject(Literal<?> literal) {
        if (literal instanceof Int i) return new TInteger(i.value);
        if (literal instanceof Real r) return new TReal(r.value);
        if (literal instanceof Bool b) return TBoolean.of(b.value);
        if (literal instanceof Str s) return new TString(s.value);
        return Null.INSTANCE;
    }

    private static TObject alu(Opcode op, TObject l, TObject r) {
        try {
            return ALU.performBinaryOperation(l, r, op, null);
        }
        catch (ArithmeticException e) {
            return null;
        }
    }

    private static void assertFolded(String what, TObject expected, IRValue folded) {
        if (expected == null) {
            Assertions.assertFalse(folded instanceof Literal<?>, what + " is invalid but was folded to " + folded);
            return;
        }
        if (folded instanceof Literal<?> literal) {
            TObject actual = toObject(literal);
            Assertions.assertEquals(expected.getClass(), actual.getClass(), what + ": wrong result class");
            Assertions.assertEquals(expected, actual, what + ": wrong result");
        }
    }

    @Test
    public void binaryFoldingMatchesInterpreter() {
        for (Opcode op : BINARY) {
            for (TObject l : VALUES) {
                for (TObject r : VALUES) {
                    Graph.Builder b = new Graph().builder();
                    IRValue folded = operation(b, op).apply(literal(b, l), literal(b, r));
                    String what = l + " " + op + " " + r;
                    TObject expected = alu(op, l, r);
                    assertFolded(what, expected, folded);
                    if (expected != null && op != Opcode.IDIV && op != Opcode.MOD) {
                        Assertions.assertInstanceOf(Literal.class, folded, what + " should be folded");
                    }
                }
            }
        }
    }

    @Test
    public void equalityFoldingMatchesInterpreter() {
        for (TObject l : VALUES) {
            for (TObject r : VALUES) {
                Graph.Builder b = new Graph().builder();
                IRValue folded = b.equals(literal(b, l), literal(b, r));
                // the interpreter evaluates rhs.equals(lhs)
                TObject expected = TBoolean.of(TNIUtils.areEqual(r, l));
                assertFolded(l + " == " + r, expected, folded);
                Assertions.assertInstanceOf(Literal.class, folded);
            }
        }
    }

    @Test
    public void unaryFoldingMatchesInterpreter() {
        record Unary(Opcode op, Function<Graph.Builder, Function<IRValue, IRValue>> node) {}
        List<Unary> ops = List.of(
                new Unary(Opcode.NOT, b -> b::not),
                new Unary(Opcode.NEG, b -> b::neg),
                new Unary(Opcode.POS, b -> b::pos));
        for (Unary op : ops) {
            for (TObject v : VALUES) {
                Graph.Builder b = new Graph().builder();
                IRValue folded = op.node.apply(b).apply(literal(b, v));
                TObject expected = ALU.performUnaryOperation(v, op.op);
                assertFolded(op.op + " " + v, expected, folded);
            }
        }
    }

    @Test
    public void truthinessFoldingMatchesInterpreter() {
        for (TObject v : VALUES) {
            Graph.Builder b = new Graph().builder();
            IRValue folded = b.truthy(literal(b, v));
            assertFolded("truthy " + v, TBoolean.of(TNIUtils.isTrue(v)), folded);
        }
    }

    /**
     * For operands of known type (but unknown value), the inferred result type must be the type
     * of the value the interpreter computes (or unknown).
     */
    @Test
    public void typingMatchesInterpreter() {
        for (Opcode op : BINARY) {
            for (TObject l : VALUES) {
                for (TObject r : VALUES) {
                    if ((op == Opcode.IDIV || op == Opcode.MOD) && r instanceof TInteger i && i.getValue() == 0) {
                        continue; // traps in the interpreter and in compiled code
                    }
                    TObject expected = alu(op, l, r);
                    Graph graph = new Graph();
                    Graph.Builder b = graph.builder();
                    IRValue lhs = b.parameter(0, IRType.from(l, false));
                    IRValue rhs = b.parameter(1, IRType.from(r, false));
                    IRValue node = operation(b, op).apply(lhs, rhs);
                    IRType type = graph.typeMap().getType(node);
                    String what = l.getType().getName() + " " + op + " " + r.getType().getName();
                    if (type == UnknownType.INSTANCE) continue;
                    Assertions.assertNotNull(expected, what + " is invalid but typed as " + type);
                    Assertions.assertTrue(IRType.isPrimitive(type), what + ": unexpected type " + type);
                    Assertions.assertSame(expected.getType(), IRType.runtimeTypeOf(type), what + ": wrong type " + type);
                }
            }
        }
    }

    @Test
    public void valueNumberingDistinguishesConstants() {
        Graph.Builder b = new Graph().builder();
        Assertions.assertSame(b.integer(5), b.integer(5));
        Assertions.assertNotSame(b.integer(5), b.integer(6));
        Assertions.assertNotSame(b.real(0.0), b.real(-0.0));
        Assertions.assertNotSame(b.local(0), b.local(1));
        IRValue x = b.parameter(0, IRType.from(new TInteger(1), false));
        Assertions.assertSame(b.add(x, b.integer(2)), b.add(x, b.integer(2)));
        Assertions.assertNotSame(b.sub(x, b.integer(2)), b.sub(b.integer(2), x));
        Assertions.assertTrue(Objects.equals(b.add(x, b.integer(0)), x), "x + 0 should be x for integers");
    }
}
