package com.tscript.compiler.impl.generation.target;

import com.tscript.compiler.impl.generation.compiled.CompiledClass;
import com.tscript.compiler.impl.generation.compiled.CompiledFile;
import com.tscript.compiler.impl.generation.compiled.CompiledFunction;
import com.tscript.compiler.impl.generation.compiled.GlobalVariable;
import com.tscript.compiler.impl.generation.compiled.instruction.*;
import com.tscript.compiler.impl.generation.compiled.pool.*;
import com.tscript.compiler.impl.generation.writers.*;
import com.tscript.runtime.core.Builtins;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public class ReadableTscriptBytecode implements Target, PoolWriter, PoolEntryWriter,
        FunctionWriter, InstructionWriter, ClassWriter {

    private final OutputStream out;

    private int instructionIndex = 0;

    public ReadableTscriptBytecode(OutputStream out) {
        this.out = out;
    }

    private void write(String s){
        try {
            out.write(s.getBytes(StandardCharsets.UTF_8));
            out.write('\n');
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(CompiledFile file) {
        write("magic: 0xDEAD");
        write("module-name: " + file.getModuleName());
        write("minor: " + file.getVersion().getMinor());
        write("major: " + file.getVersion().getMajor());
        write("entrypoint: " + file.getEntryPoint());
        write("\nglobals:");
        for (GlobalVariable variable : file.getGlobalVariables()) {
            write("\t" + ((variable.isMutable) ? "var " : "const ") + variable.name);
        }
        writePool(file.getConstantPool());
        for (CompiledFunction function : file.getFunctions()) {
            writeFunction(function);
            write("");
        }
        for (CompiledClass clazz : file.getClasses()) {
            writeClass(clazz);
        }
    }

    @Override
    public void writeClass(CompiledClass compiledClass) {
        StringBuilder sb = new StringBuilder();

        sb.append(compiledClass.getIndex())
                .append(" ")
                .append(compiledClass.getName())
                .append(": ").append(compiledClass.getSuperIndex()).append(" ");

        if (compiledClass.isAbstract())
            sb.append("(abstract) ");

        sb.append("{\n\tconstructor:ref=").append(compiledClass.getConstructorIndex())
                .append("\n\tstaticBlock:ref=").append(compiledClass.getStaticInitializerIndex())
                .append("\n");

        for (CompiledClass.Member member : compiledClass.getStaticMembers()){
            sb.append("\t");
            sb.append("static ");
            if (!member.isMutable) sb.append("const ");
            sb.append(member.name)
                    .append(" ").append(member.visibility)
                    .append("\n");
        }

        for (CompiledClass.Member member : compiledClass.getInstanceMembers()){
            sb.append("\t");
            if (!member.isMutable) sb.append("const ");
            sb.append(member.name)
                    .append(" ").append(member.visibility)
                    .append("\n");
        }

        write(sb.append("}").toString());
    }

    @Override
    public void writeFunction(CompiledFunction function) {
        StringBuilder sb = new StringBuilder();

        sb.append(function.getIndex())
                .append(" ")
                .append(function.getName())
                .append(" stack=").append(function.getStackSize())
                .append(", locals=").append(function.getRegisterAmount())
                .append(", params=");


        if (function.getParameters().isEmpty()){
            sb.append("[]");
        }
        else {
            StringJoiner joiner = new StringJoiner(", ", "[", "]");
            for (CompiledFunction.Parameter parameter : function.getParameters()) {
                String s = parameter.name;
                if (parameter.defaultValueRef != -1){
                    s += ": ref=" + parameter.defaultValueRef;
                }
                joiner.add(s);
            }
            sb.append(joiner);
        }

        write(sb.toString());

        instructionIndex = 0;
        for (Instruction instruction : function.getInstructions()) {
            instruction.write(this);
        }

    }

    private void writeInst(String s){
        write("\t" + instructionIndex++ + " " + s);
    }

    @Override
    public void writeBinaryOperation(BinaryOperation inst) {
        writeInst(inst.operation.name());
    }

    @Override
    public void writeBranchIfFalse(BranchIfFalse inst) {
        writeInst("BRANCH_IF_FALSE " + inst.address);
    }

    @Override
    public void writeBranchIfTrue(BranchIfTrue inst) {
        writeInst("BRANCH_IF_TRUE " + inst.address);
    }

    @Override
    public void writeBranchItr(BranchItr inst) {
        writeInst("BRANCH_ITR " + inst.address);
    }

    @Override
    public void writeCallInplace(CallInplace inst) {
        writeInst("CALL_INPLACE " + inst.argc);
    }

    @Override
    public void writeCallMapped(CallMapped inst) {
        writeInst("CALL_MAPPED " + inst.argc);
    }

    @Override
    public void writeCallSuper(CallSuper inst) {
        writeInst("CALL_SUPER " + inst.argc);
    }

    @Override
    public void writeContainerRead(ContainerRead inst) {
        writeInst("CONTAINER_READ");
    }

    @Override
    public void writeContainerWrite(ContainerWrite inst) {
        writeInst("CONTAINER_WRITE");
    }

    @Override
    public void writeDup(Dup inst) {
        writeInst("DUP");
    }

    @Override
    public void writeEnterTry(EnterTry inst) {
        writeInst("ENTER_TRY " + inst.address);
    }

    @Override
    public void writeEquals(Equals inst) {
        writeInst("EQUALS");
    }

    @Override
    public void writeGetItr(GetItr inst) {
        writeInst("GET_ITR");
    }

    @Override
    public void writeGetType(GetType inst) {
        writeInst("GET_TYPE");
    }

    @Override
    public void writeGoto(Goto inst) {
        writeInst("GOTO " + inst.address);
    }

    @Override
    public void writeImport(Import inst) {
        writeInst("IMPORT " + inst.address);
    }

    @Override
    public void writeItrNext(ItrNext inst) {
        writeInst("ITR_NEXT");
    }

    @Override
    public void writeLeaveTry(LeaveTry inst) {
        writeInst("LEAVE_TRY");
    }

    @Override
    public void writeLoadAbstract(LoadAbstract inst) {
        writeInst("LOAD_ABSTRACT " + inst.address);
    }

    @Override
    public void writeLoadConst(LoadConst inst) {
        writeInst("LOAD_CONST " + inst.address);
    }

    @Override
    public void writeLoadExternal(LoadExternal inst) {
        writeInst("LOAD_EXTERNAL " + inst.address);
    }

    @Override
    public void writeStoreExternal(StoreExternal inst) {
        writeInst("STORE_EXTERNAL " + inst.address);
    }

    @Override
    public void writeLoadInternal(LoadInternal inst) {
        writeInst("LOAD_INTERNAL " + inst.address);
    }

    @Override
    public void writeStoreInternal(StoreInternal inst) {
        writeInst("STORE_INTERNAL " + inst.address);
    }

    @Override
    public void writeLoadGlobal(LoadGlobal inst) {
        writeInst("LOAD_GLOBAL " + inst.address);
    }

    @Override
    public void writeStoreGlobal(StoreGlobal inst) {
        writeInst("STORE_GLOBAL " + inst.address);
    }

    @Override
    public void writeLoadLocal(LoadLocal inst) {
        writeInst("LOAD_LOCAL " + inst.address);
    }

    @Override
    public void writeStoreLocal(StoreLocal inst) {
        writeInst("STORE_LOCAL " + inst.address);
    }

    @Override
    public void writeLoadBuiltin(LoadBuiltin inst) {
        writeInst("LOAD_BUILTIN " + Builtins.load(inst.address).getDisplayName());
    }

    @Override
    public void writeLoadName(LoadName inst) {
        writeInst("LOAD_NAME " + inst.address);
    }

    @Override
    public void writeLoadNative(LoadNative inst) {
        writeInst("LOAD_NATIVE " + inst.address);
    }

    @Override
    public void writeLoadStatic(LoadStatic inst) {
        writeInst("LOAD_STATIC " + inst.address);
    }

    @Override
    public void writeStoreStatic(StoreStatic inst) {
        writeInst("STORE_STATIC " + inst.address);
    }

    @Override
    public void writeLoadType(LoadType inst) {
        writeInst("LOAD_TYPE " + inst.address);
    }

    @Override
    public void writeLoadVirtual(LoadVirtual inst) {
        writeInst("LOAD_VIRTUAL " + inst.address);
    }

    @Override
    public void writeMakeArray(MakeArray inst) {
        writeInst("MAKE_ARRAY " + inst.count);
    }

    @Override
    public void writeMakeDict(MakeDict inst) {
        writeInst("MAKE_DICT " + inst.count);
    }

    @Override
    public void writeMakeRange(MakeRange inst) {
        writeInst("MAKE_RANGE");
    }

    @Override
    public void writeNeg(Neg inst) {
        writeInst("NEG");
    }

    @Override
    public void writeNewLine(NewLine inst) {
        writeInst("NEW_LINE " + inst.line);
    }

    @Override
    public void writeNot(Not inst) {
        writeInst("NOT");
    }

    @Override
    public void writeNotEquals(NotEquals inst) {
        writeInst("NOT_EQUALS");
    }

    @Override
    public void writePop(Pop inst) {
        writeInst("POP");
    }

    @Override
    public void writePos(Pos inst) {
        writeInst("POS");
    }

    @Override
    public void writePushNull(PushNull inst) {
        writeInst("PUSH_NULL");
    }

    @Override
    public void writePushBool(PushBool inst) {
        writeInst("PUSH_BOOL " + inst.value);
    }

    @Override
    public void writePushInt(PushInt inst) {
        writeInst("PUSH_INT " + inst.value);
    }

    @Override
    public void writePushThis(PushThis inst) {
        writeInst("PUSH_THIS");
    }

    @Override
    public void writeReturn(Return inst) {
        writeInst("RETURN");
    }

    @Override
    public void writeSetOwner(SetOwner inst) {
        writeInst("SET_OWNER");
    }

    @Override
    public void writeThrow(Throw inst) {
        writeInst("THROW");
    }

    @Override
    public void writeToInplaceArg(ToInplaceArg inst) {
        writeInst("TO_INP_ARG");
    }

    @Override
    public void writeToMapArg(ToMapArg inst) {
        writeInst("TO_MAP_ARG " + inst.address);
    }

    @Override
    public void writeUse(Use inst) {
        writeInst("USE");
    }


    @Override
    public void writeInteger(IntegerEntry entry) {
        write("\t" + entry.getIndex() + " " + entry.getTag() + " " + entry.get());
    }

    @Override
    public void writeFloat(FloatEntry entry) {
        write("\t" + entry.getIndex() + " " + entry.getTag() + " " + entry.get());
    }

    @Override
    public void writeString(StringEntry entry) {
        write("\t" + entry.getIndex() + " " + entry.getTag() + " \"" + entry.get() + "\"");
    }

    @Override
    public void writeNull(NullEntry entry) {
        write("\t" + entry.getIndex() + " " + entry.getTag());
    }

    @Override
    public void writeBoolean(BooleanEntry entry) {
        write("\t" + entry.getIndex() + " " + entry.getTag() + " " + entry.get());
    }

    @Override
    public void writeRange(RangeEntry entry) {
        write("\t" + entry.getIndex() + " " + entry.getTag() + " ref:" + entry.get().get(0) + " ref:" + entry.get().get(1));
    }

    @Override
    public void writeArray(ArrayEntry entry) {
        StringJoiner joiner = new StringJoiner("[ref=", ", ref=", "]");
        for (int ref : entry.get()){
            joiner.add(String.valueOf(ref));
        }
        write("\t" + entry.getIndex() + " " + entry.getTag() + " " + joiner);
    }

    @Override
    public void writeDictionary(DictionaryEntry entry) {
        StringJoiner joiner = new StringJoiner("[ref=", ", ref=", "]");
        for (int ref : entry.get()){
            joiner.add(String.valueOf(ref));
        }
        write("\t" + entry.getIndex() + " " + entry.getTag() + " " + joiner);
    }

    @Override
    public void writeUTF8(UTF8Entry entry) {
        write("\t" + entry.getIndex() + " " + entry.getTag() + " " + entry.get());
    }

    @Override
    public void writePool(ConstantPool pool) {
        write("\nconstant-pool:");
        for (PoolEntry<?> entry : pool.getEntries())
            entry.write(this);
        if (pool.getEntries().isEmpty()){
            write("\t--empty--");
        }
        write("");
    }
}
