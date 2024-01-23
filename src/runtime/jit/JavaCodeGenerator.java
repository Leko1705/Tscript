package runtime.jit;

import runtime.core.Pool;
import runtime.core.VirtualFunction;
import runtime.tni.NativeFunction;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;

public class JavaCodeGenerator implements TreeVisitor<Void, Void> {


    private final String javaFileName;

    private final VirtualFunction function;

    private boolean localsUsed = false;

    private final ArrayDeque<String> valueStack = new ArrayDeque<>();

    private final StringBuilder code = new StringBuilder();

    private final Map<String, String> constants = new LinkedHashMap<>();

    private int tryDepth = 0;


    public static String generate(String javaFileName, BytecodeParser.Tree tree, VirtualFunction function){
        JavaCodeGenerator generator = new JavaCodeGenerator(javaFileName, function);
        tree.accept(generator);
        String s = generator.finalized(function);
        System.out.println(s);
        return s;
    }

    private JavaCodeGenerator(String javaFileName, VirtualFunction function){
        this.javaFileName = javaFileName;
        this.function = function;
    }

    int varID = 0;
    private String newVarName(){
        return "var" + varID++;
    }

    private int constID = 0;
    private String getConstantName(String s){
        if (constants.containsKey(s))
            return constants.get(s);
        String newConstName = "const" + constID++;
        constants.put(s, newConstName);
        return newConstName;
    }

    public String finalized(VirtualFunction function){

        String constants = genConstants();

        String fullCode = """
                import runtime.core.*;
                import runtime.type.*;
                import runtime.jit.*;
                import java.util.*;
                import tscriptc.generation.Opcode;
                import static java.util.AbstractMap.SimpleEntry;
                import runtime.tni.*;
                
                public class {1} extends Callable {
                
                    {6}
                    public String getName(){
                        return "{2}";
                    }
                
                    public LinkedHashMap<String, Data> getParameters(){
                        throw new UnsupportedOperationException("getParameters in JIT-optimized code");
                    }
                    
                    public Data eval(TThread caller, LinkedHashMap<String, Data> params){
                        {3}
                {4}
                    }
                    
                }
                """;
        fullCode = fullCode
                .replace("{6}", constants)
                .replace("{1}", javaFileName)
                .replace("{2}", function.getName())
                .replace("{4}", code);
        String localsDef =  !this.function.getParameters().isEmpty() && localsUsed
                ? "Data[] locals = params.values().toArray(new Data[0]);\n"
                : "";
        fullCode = fullCode.replace("{3}", localsDef);

        return fullCode;
    }

    private String genConstants(){
        StringBuilder sb = new StringBuilder();
        for (String value : constants.keySet()){
            sb.append("private static final Data ")
                    .append(constants.get(value)).append(" = ")
                    .append(value).append(";\n");
        }
        return sb.toString();
    }


    @Override
    public Void visitRootTree(BytecodeParser.RootTree rootTree, Void unused) {
        return rootTree.tree.accept(this);
    }

    @Override
    public Void visitSequenceTree(BytecodeParser.SequenceTree sequenceTree, Void unused) {
        for (BytecodeParser.Tree child : sequenceTree.children) {
            child.accept(this);
            if (child instanceof BytecodeParser.ReturnTree
                    || child instanceof BytecodeParser.ThrowTree)
                break;
        }
        return null;
    }

    @Override
    public Void visitReturnTree(BytecodeParser.ReturnTree returnTree, Void unused) {
        returnTree.expression.accept(this);
        code.append("return ").append(valueStack.pop()).append(";\n");
        return null;
    }

    @Override
    public Void visitNullTree(BytecodeParser.NullTree nullTree, Void unused) {
        valueStack.push("TNull.NULL");
        return null;
    }

    @Override
    public Void visitIntegerTree(BytecodeParser.IntegerTree integerTree, Void unused) {
        String val = "new TInteger(" + integerTree.value + ")";
        String s = getConstantName(val);
        valueStack.push(s);
        return null;
    }

    @Override
    public Void visitRealTree(BytecodeParser.RealTree realTree, Void unused) {
        String val = "new TReal(" + realTree.value + ")";
        String s = getConstantName(val);
        valueStack.push(s);
        return null;
    }

    @Override
    public Void visitBooleanTree(BytecodeParser.BooleanTree booleanTree, Void unused) {
        String bool = booleanTree.value ? "TBoolean.TRUE" : "TBoolean.FALSE";
        valueStack.push(bool);
        return null;
    }

    @Override
    public Void visitStringTree(BytecodeParser.StringTree stringTree, Void unused) {
        String value = stringTree.value;
        value = value.replaceAll("\"", "\\\\\"")
                .replaceAll("\n", "\\\\n");
        String val = "new TString(\"" + value + "\")";
        String s = getConstantName(val);
        valueStack.push(s);
        return null;
    }

    @Override
    public Void visitConstantTree(BytecodeParser.ConstantTree constantTree, Void unused) {

        int address = constantTree.address();

        Pool pool = function.getPool();
        try {
            Object o = pool.load(address, null);

            if (o instanceof NativeFunction f){
                String val = "NativeCollection.load(" + f.getName() + ")";
                String name = getConstantName(val);
                valueStack.push(name);
                return null;
            }

        }catch (Exception ignored){}

        valueStack.push("(Data) caller.loadFromPool(" + address + ")");
        return null;
    }

    @Override
    public Void visitLoadLocalTree(BytecodeParser.LoadLocalTree loadLocalTree, Void unused) {
        localsUsed = true;
        valueStack.push("locals[" + loadLocalTree.address() + "]");
        return null;
    }

    @Override
    public Void visitStoreLocalTree(BytecodeParser.StoreLocalTree storeLocalTree, Void unused) {
        localsUsed = true;
        storeLocalTree.child.accept(this);
        code.append("locals[").append(storeLocalTree.address).append("] = ").append(valueStack.pop()).append(";\n");
        return null;
    }

    @Override
    public Void visitLoadGlobalTree(BytecodeParser.LoadGlobalTree loadGlobalTree, Void unused) {
        valueStack.push("caller.loadGlobal(" + loadGlobalTree.address() + ")");
        return null;
    }

    @Override
    public Void visitStoreGlobalTree(BytecodeParser.StoreGlobalTree storeLocalTree, Void unused) {
        storeLocalTree.child.accept(this);
        code.append("caller.storeGlobal(").append(storeLocalTree.address).append(", ").append(valueStack.pop()).append(");\n");
        return null;
    }

    @Override
    public Void visitBinaryOperationTree(BytecodeParser.BinaryOperationTree operationTree, Void unused) {
        String varName = newVarName();
        operationTree.left.accept(this);
        operationTree.right.accept(this);
        String right = valueStack.pop();
        String left = valueStack.pop();

        String operationCode = """
                Data {1} = ALU.performBinaryOperation({2}, {3}, Opcode.{4}, caller);
                if ({1} == null) return null;
                """.replaceAll("\\{1}", varName)
                .replace("{2}", left)
                .replace("{3}", right)
                .replace("{4}", operationTree.operation.toString());

        code.append(operationCode);
        valueStack.push(varName);
        return null;
    }

    @Override
    public Void visitUnaryOperationTree(BytecodeParser.UnaryOperationTree operationTree, Void unused) {
        String varName = newVarName();
        operationTree.exp.accept(this);
        String exp = valueStack.pop();

        String operationCode = """
                Data {1} = ALU.performUnaryOperation({2}, Opcode.{3}, caller);
                if ({1} == null) return null;
                """.replaceAll("\\{1}", varName)
                .replace("{2}", exp)
                .replace("{3}", operationTree.operation.toString());

        code.append(operationCode);
        valueStack.push(varName);
        return null;
    }

    @Override
    public Void visitThisTree(BytecodeParser.ThisTree thisTree, Void unused) {
        valueStack.push("this.getOwner()");
        return null;
    }

    @Override
    public Void visitEqualsTree(BytecodeParser.EqualsTree equalsTree, Void unused) {
        equalsTree.right.accept(this);
        equalsTree.left.accept(this);
        String equalOpCode = valueStack.pop() + ".equals(" + valueStack.pop() + ")";
        if (!equalsTree.equals) equalOpCode = "!" + equalOpCode;
        valueStack.push("TBoolean.of(" + equalOpCode + ")");
        return null;
    }

    @Override
    public Void visitCallTree(BytecodeParser.CallTree callTree, Void unused) {

        for (BytecodeParser.Tree arg : callTree.arguments)
            arg.accept(this);

        String argList = genArgumentList(callTree.arguments.size());

        callTree.called.accept(this);
        String calledName = valueStack.pop();

        String calledResult = newVarName();

        String exHandling = genException(calledName + " + \"is not callable\"");

        code.append("""
                Data {2} = null;
                {
                    if (!(caller.unpack({1}) instanceof Callable c)) {
                            {4}
                        }
                    {2} = caller.call(c, {3});
                    if ({2} == null) return null;
                }
                """.replaceAll("\\{1}", calledName)
                .replaceAll("\\{2}", calledResult)
                .replace("{3}", argList)
                .replace("{4}", exHandling));

        valueStack.push(calledResult);

        return null;
    }

    @Override
    public Void visitCallSuperTree(BytecodeParser.CallSuperTree callSuperTree, Void unused) {

        for (BytecodeParser.Tree arg : callSuperTree.arguments())
            arg.accept(this);

        String argList = genArgumentList(callSuperTree.arguments().size());

        String callCode = """
                {
                    Callable constructor = getOwner().getType().getSuper().getConstructor();
                    constructor.setOwner(getOwner());
                    Data res = caller.call(constructor, {1});
                    if(res == null) {
                       return null;
                   }
                }
                """.replace("{1}", argList);

        code.append(callCode);

        return null;
    }

    private String genArgumentList(int argc){
        StringBuilder argList = new StringBuilder("List.of(");
        for (int i = 0; i < argc; i++){
            String arg = valueStack.pop();
            argList.append(arg).append(" instanceof Argument arg ? arg : new Argument(null, ").append(arg).append(")");

            if (i < argc-1)
                argList.append(", ");
        }
        argList.append(")");
        return argList.toString();
    }

    @Override
    public Void visitArgumentTree(BytecodeParser.ArgumentTree argumentTree, Void unused) {
        argumentTree.exp.accept(this);
        String varName = newVarName();
        code.append("Data ").append(varName).append(" = new Argument((String)caller.loadFromPool(").append(argumentTree.address).append("), ").append(valueStack.pop()).append(");\n");
        valueStack.push(varName);
        return null;
    }

    @Override
    public Void visitGetTypeTree(BytecodeParser.GetTypeTree getTypeTree, Void unused) {
        getTypeTree.exp.accept(this);
        valueStack.push("unpack(" + valueStack.pop() + ").getType()");
        return null;
    }

    @Override
    public Void visitThrowTree(BytecodeParser.ThrowTree throwTree, Void unused) {
        throwTree.exp.accept(this);
        String s = genException(valueStack.pop());
        code.append(s);
        return null;
    }

    @Override
    public Void visitArrayTree(BytecodeParser.ArrayTree arrayTree, Void unused) {
        int argc = arrayTree.arguments().size();
        if (argc == 0) {
            valueStack.push("new TArray()");
            return null;
        }

        for (BytecodeParser.Tree val : arrayTree.arguments())
            val.accept(this);

        StringBuilder argList = new StringBuilder("List.of(");
        for (int i = 0; i < argc; i++){
            argList.append(valueStack.pop());
            if (i < argc-1)
                argList.append(", ");
        }
        argList.append(")");

        String varName = newVarName();
        String value = "new TArray(new ArrayList<Data>(" + argList + "))";
        code.append("Data ").append(varName).append(" = ").append(value).append(";\n");
        valueStack.push(varName);
        return null;
    }

    @Override
    public Void visitDictionaryTree(BytecodeParser.DictionaryTree dictionaryTree, Void unused) {
        String varName = newVarName();

        if (dictionaryTree.arguments.isEmpty()){
            code.append("Data ").append(varName).append(" = new TDictionary();\n");
            valueStack.push(varName);
            return null;
        }

        BytecodeParser.Tree[] keys = dictionaryTree.arguments.keySet().toArray(new BytecodeParser.Tree[0]);
        BytecodeParser.Tree[] values = dictionaryTree.arguments.values().toArray(new BytecodeParser.Tree[0]);

        StringBuilder argList = new StringBuilder("new LinkedHashMap<Data, Data>(Map.ofEntries(");

        for (int i = 0; i < keys.length; i++){
            keys[i].accept(this);
            String key = valueStack.pop();

            values[i].accept(this);
            String value = valueStack.pop();

            argList.append("new SimpleEntry<Data, Data>(").append(key).append(", ").append(value).append(")");
            if (i < keys.length-1)
                argList.append(", ");
        }
        argList.append("))");

        String dict = "new TDictionary(" + argList + ")";
        code.append("Data ").append(varName).append(" = ").append(dict).append(";\n");
        valueStack.push(varName);

       return null;
    }

    @Override
    public Void visitRangeTree(BytecodeParser.RangeTree rangeTree, Void unused) {

        rangeTree.from.accept(this);
        String from = valueStack.pop();

        rangeTree.to.accept(this);
        String to = valueStack.pop();

        if (rangeTree.from instanceof BytecodeParser.IntegerTree
                && rangeTree.to instanceof BytecodeParser.IntegerTree){
            String s = getConstantName("new TRange((TInteger)" + from + ", (TInteger)" + to + ")");
            valueStack.push(s);
            return null;
        }

        String rangeCode = "";

        if (!(rangeTree.from instanceof BytecodeParser.IntegerTree)){
            String fromVal = newVarName();
            String exHandling = genException("\"can not build range from \" + from");

            rangeCode += """
                    TObject from = caller.unpack({1});
                    if(!(from instanceof TInteger)){
                        {3}
                    }
                    TInteger {2} = (TInteger) from;
                    """
                    .replace("{1}", from)
                    .replace("{2}", fromVal)
                    .replace("{3}", exHandling);
            from = fromVal;
        }
        else from = "(TInteger)" + from;


        if (!(rangeTree.to instanceof BytecodeParser.IntegerTree)){
            String toVal = newVarName();
            String exHandling = genException("\"can not build range from \" + to");

            rangeCode += """
                    TObject to = caller.unpack({1});
                    if(!(to instanceof TInteger)){
                        {3}
                    }
                    TInteger {2} = (TInteger) to;
                    """
                    .replace("{1}", to)
                    .replace("{2}", toVal)
                    .replace("{3}", exHandling);
            to = toVal;
        }
        else to = "(TInteger)" + to;

        String rangeName = newVarName();
        String newRange = "new TRange(" + from + ", " + to + ")";
        rangeCode = "Data " + rangeName + " = null;\n" + "{\n" + rangeCode + rangeName + " = " + newRange + ";\n}";

        code.append(rangeCode);
        valueStack.push(rangeName);
        return null;
    }

    @Override
    public Void visitNewLineTree(BytecodeParser.NewLineTree newLineTree, Void unused) {
        if (tryDepth != 0) return null;
        code.append("caller.setLine(").append(newLineTree.line()).append(");\n");
        return null;
    }

    @Override
    public Void visitLoadMemberFastTree(BytecodeParser.LoadMemberFastTree loadTree, Void unused) {
        valueStack.push("caller.unpack(getOwner()).get(" + loadTree.address() + ").data");
        return null;
    }

    @Override
    public Void visitStoreMemberFastTree(BytecodeParser.StoreMemberFastTree storeTree, Void unused) {
        storeTree.child.accept(this);
        String toStore = valueStack.pop();
        String storeCode = """
                {
                    TObject owner = caller.unpack(getOwner());
                    Member member = owner.get({1});
                    if (member.data == null){
                        // member was not initialized yet
                        member.kind = caller.unpack({2}) instanceof Callable
                                ? Member.Kind.IMMUTABLE
                                : Member.Kind.MUTABLE;
                    }
                    member.data = {2};
                }
                """.replace("{1}", Integer.toString(storeTree.address))
                .replaceAll("\\{2}", toStore);

        code.append(storeCode);
        return null;
    }

    @Override
    public Void visitLoadMemberTree(BytecodeParser.LoadMemberTree loadTree, Void unused) {
        return null;
    }

    @Override
    public Void visitStoreMemberTree(BytecodeParser.StoreMemberTree storeTree, Void unused) {
        return null;
    }

    @Override
    public Void visitAccessUnknownFastTree(BytecodeParser.AccessUnknownFastTree accessTre, Void unused) {
        accessTre.exp.accept(this);
        String s = "caller.unpack(" + valueStack.pop() + ").get(" + accessTre.address + ").data";
        valueStack.push(s);
        return null;
    }

    @Override
    public Void visitLoadStaticTree(BytecodeParser.LoadStaticTree loadTree, Void unused) {
        return null;
    }

    @Override
    public Void visitStoreStaticTree(BytecodeParser.StoreStaticTree storeTree, Void unused) {
        return null;
    }

    @Override
    public Void visitWriteContainerTree(BytecodeParser.WriteContainerTree writeTree, Void unused) {
        writeTree.container.accept(this);
        String container = valueStack.pop();
        writeTree.key.accept(this);
        String key = valueStack.pop();
        writeTree.exp.accept(this);
        String value = valueStack.pop();

        String checkCode = genCheckForAccessibleCode(container, writeTree.container, "ContainerWriteable");

        String writeCode = """
                {
                    {0}
                    ContainerWriteable container = (ContainerWriteable) {1};
                    boolean success = container.writeToContainer(caller, {2}, {3});
                    if(!success){
                        return null;
                   }
                }
                """
                .replace("{0}", checkCode)
                .replace("{1}", container)
                .replace("{2}", key)
                .replace("{3}", value);

        code.append(writeCode);
        return null;
    }

    @Override
    public Void visitReadContainerTree(BytecodeParser.ReadContainerTree readTree, Void unused) {
        readTree.container.accept(this);
        String container = valueStack.pop();
        readTree.key.accept(this);
        String key = valueStack.pop();

        String storeVar = newVarName();

        String checkCode = genCheckForAccessibleCode(container, readTree.container, "ContainerAccessible");
        String readCode = """
                Data {1} = null;
                {
                    {4}
                    ContainerAccessible container = (ContainerAccessible) {2};
                    {1} = container.readFromContainer(caller, {3});
                    if({1} == null){
                        return null;
                    }
                }
                """.replaceAll("\\{1}", storeVar)
                .replaceAll("\\{2}", container)
                .replace("{3}", key)
                .replace("{4}", checkCode);

        code.append(readCode);
        valueStack.push(storeVar);
        return null;
    }

    private String genCheckForAccessibleCode(String checked, BytecodeParser.Tree given, String requiredClass){

        if (given instanceof BytecodeParser.ArrayTree
                || given instanceof BytecodeParser.DictionaryTree
                || given instanceof BytecodeParser.RangeTree)
            return "";

        String exHandling = genException(checked + " + \" is not accessible\"");

        return """
            if (!({2} instanceof {1})){
                {3}
            }""".replace("{2}", checked)
                .replace("{1}", requiredClass)
                .replace("{3}", exHandling);
    }

    @Override
    public Void visitLoadAbstractImplTree(BytecodeParser.LoadAbstractImplTree loadTree, Void unused) {
        String varName = newVarName();

        String exHandling = genException("\"can not find implementation of '\" + " + loadTree.name() + " + \"'\"");

        String loadCode = """
                Data {0} = null;
                {
                    boolean found = false;
                    TObject owner = caller.unpack(getOwner());
                    TType currType = owner.getType();
                    while (currType != null){
                        Member member = owner.get("{1}");
                        if (member == null) {
                            currType = currType.getSuper();
                            continue;
                        }
                        {0} = member.data;
                        found = true;
                        break;
                    }
                    if(!found){
                        {2}
                    }
                }
                """
                .replaceAll("\\{0}", varName)
                .replaceAll("\\{1}", loadTree.name())
                .replace("{2}", exHandling);

        valueStack.push(varName);
        code.append(loadCode);
        return null;
    }

    @Override
    public Void visitTryCatchTree(BytecodeParser.TryCatchTree tryCatchTree, Void unused) {
        localsUsed = true;
        tryDepth++;
        code.append("try{\n");
        tryCatchTree.tryBody.accept(this);
        tryDepth--;
        code.append("""
                } catch(GeneratedJITException ex){
                    locals[{0}] = ex.thrown;
            """.replace("{0}", Integer.toString(tryCatchTree.exAddress)));
        tryCatchTree.catchBody.accept(this);
        code.append("}\n");
        return null;
    }

    int nextFreeItrVarID = 0;
    @Override
    public Void visitForLoopTree(BytecodeParser.ForLoopTree forLoopTree, Void unused) {

        if (forLoopTree.iterable instanceof BytecodeParser.RangeTree i
                && i.from instanceof BytecodeParser.IntegerTree from
                && i.to instanceof BytecodeParser.IntegerTree to){

            String varName = "idx" + nextFreeItrVarID++;

           code.append("for(int ")
                   .append(varName)
                   .append(" = ")
                   .append(from.value)
                   .append("; ")
                   .append(varName)
                   .append(" < ")
                   .append(to.value)
                   .append("; ")
                   .append(varName)
                   .append("++) {\n");

            if (forLoopTree.address >= 0){
                localsUsed = true;
                code.append("locals[").append(forLoopTree.address).append("] = new TInteger(i);\n".replace("i", varName));
            }

           forLoopTree.body.accept(this);
           code.append("}\n");
           nextFreeItrVarID--;
           return null;
        }
        else {
            forLoopTree.iterable.accept(this);
            String iterable = valueStack.pop();
            code.append("""
                    if (!({0} instanceof IterableObject)){
                        caller.reportRuntimeError({0} + " is not iterable");
                        return null;
                    }
                     """.replaceAll("\\{0}", iterable));
            String itrName = newVarName();
            code.append("""
                    IteratorObject {1} = ((IterableObject){0}).iterator();
                    while ({1}.hasNext()){
                    """.replaceAll("\\{0}", iterable)
                    .replaceAll("\\{1}", itrName));
            if (forLoopTree.address >= 0){
                code.append("locals[").append(forLoopTree.address).append("] = ").append(itrName).append(". next();\n");
            }
            else {
                code.append(itrName).append(".next();\n");
            }
            forLoopTree.body.accept(this);
            code.append("}\n");
        }

        return null;
    }

    @Override
    public Void visitIfElseTree(BytecodeParser.IfElseTree ifElseTree, Void unused) {
        ifElseTree.condition.accept(this);
        String condition = "caller.isTrue(" + valueStack.pop() + ")";
        if (!ifElseTree.ifTrue) condition = "!" + condition;
        code.append("if(").append(condition).append("){\n");
        ifElseTree.ifBody.accept(this);
        code.append("}\n");

        if (ifElseTree.elseBody != null){
            code.append("else{\n");
            ifElseTree.elseBody.accept(this);
            code.append("}\n");
        }

        return null;
    }

    @Override
    public Void visitJavaCodeTree(BytecodeParser.JavaCodeTree javaCodeTree, Void unused) {
        code.append(javaCodeTree.code());
        return null;
    }


    private String genException(String thrown){
        if (tryDepth == 0){
            return """
                    caller.reportRuntimeError({0});
                    return null;
                    """.replace("{0}", thrown);
        }
        else {
            return "throw new GeneratedJITException({0});\n".replace("{0}", thrown);
        }
    }


}
