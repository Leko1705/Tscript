package com.tscript.compiler.impl.generation.target;

import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.stroage.loading.LoadingConstants;
import com.tscript.runtime.utils.Conversion;
import com.tscript.compiler.impl.generation.compiled.CompiledClass;
import com.tscript.compiler.impl.generation.compiled.CompiledFile;
import com.tscript.compiler.impl.generation.compiled.CompiledFunction;
import com.tscript.compiler.impl.generation.compiled.GlobalVariable;
import com.tscript.compiler.impl.generation.compiled.instruction.*;
import com.tscript.compiler.impl.generation.compiled.pool.*;
import com.tscript.compiler.impl.generation.writers.*;
import com.tscript.compiler.source.utils.Version;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TscriptBytecode
        implements Target, PoolWriter, PoolEntryWriter,
        FunctionWriter, InstructionWriter, ClassWriter {



    private final OutputStream out;

    public TscriptBytecode(OutputStream out) {
        this.out = out;
    }

    private void write(int b) {
        try {
            out.write(b);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void write(byte[] b) {
        try {
            out.write(b);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void write(Opcode opcode, byte... bytes) {
        write(opcode.b);
        write(bytes);
    }

    private void write(String s){
        write(s.getBytes(StandardCharsets.UTF_8));
        write('\0');
    }



    @Override
    public void write(CompiledFile file) {
        write(0xD);
        write(0xE);
        write(0xA);
        write(0xD);
        write(file.getModuleName());
        Version version = file.getVersion();
        write(version.getMinor());
        write(version.getMajor());
        write(Conversion.to2Bytes(file.getEntryPoint()));

        List<GlobalVariable> vars = file.getGlobalVariables();
        write(Conversion.to2Bytes(vars.size()));
        for (GlobalVariable var : vars) {
            write(var.name);
            write(var.isMutable ? 1 : 0);
        }

        writePool(file.getConstantPool());

        List<CompiledFunction> functions = file.getFunctions();
        write(Conversion.to2Bytes(functions.size()));
        for (CompiledFunction function : functions) {
            writeFunction(function);
        }

        List<CompiledClass> classes = file.getClasses();
        write(Conversion.to2Bytes(classes.size()));
        for (CompiledClass clazz : classes) {
            writeClass(clazz);
        }
    }



    @Override
    public void writePool(ConstantPool pool) {
        List<PoolEntry<?>> entries = pool.getEntries();
        write(Conversion.getBytes(entries.size()));
        for (PoolEntry<?> entry : entries) {
            entry.write(this);
        }
    }


    private void write(int index, PoolTag tag, byte... bytes) {
        write(Conversion.to2Bytes(index));
        write((byte) switch (tag){
            case INTEGER -> LoadingConstants.POOL_TYPE_INT;
            case FLOAT -> LoadingConstants.POOL_TYPE_REAL;
            case STRING -> LoadingConstants.POOL_TYPE_STRING;
            case BOOL -> LoadingConstants.POOL_TYPE_BOOL;
            case NULL -> LoadingConstants.POOL_TYPE_NULL;
            case ARRAY -> LoadingConstants.POOL_TYPE_ARRAY;
            case RANGE -> LoadingConstants.POOL_TYPE_RANGE;
            case DICTIONARY -> LoadingConstants.POOL_TYPE_DICTIONARY;
            case UTF8 -> LoadingConstants.POOL_TYPE_UTF8;
        });
        write(bytes);
    }

    @Override
    public void writeInteger(IntegerEntry entry) {
        write(entry.getIndex(), entry.getTag(), Conversion.getBytes(entry.get()));
    }

    @Override
    public void writeFloat(FloatEntry entry) {
        write(entry.getIndex(), entry.getTag(), Conversion.getBytes(entry.get()));
    }

    @Override
    public void writeString(StringEntry entry) {
        write(entry.getIndex(), entry.getTag(), entry.get().getBytes(StandardCharsets.UTF_8));
        write('\0');
    }

    @Override
    public void writeNull(NullEntry entry) {
        write(entry.getIndex(), entry.getTag());
    }

    @Override
    public void writeBoolean(BooleanEntry entry) {
        write(entry.getIndex(), entry.getTag(), (byte) (entry.get() ? 1 : 0));
    }

    @Override
    public void writeRange(RangeEntry entry) {
        write(entry.getIndex(), entry.getTag(), entry.get().get(0).byteValue(), entry.get().get(1).byteValue());
    }

    @Override
    public void writeArray(ArrayEntry entry) {
        write(entry.get().size());
        for (Integer ref : entry.get()) {
            write(Conversion.to2Bytes(ref));
        }
    }

    @Override
    public void writeDictionary(DictionaryEntry entry) {
        write(entry.get().size());
        for (Integer ref : entry.get()) {
            write(Conversion.to2Bytes(ref));
        }
    }

    @Override
    public void writeUTF8(UTF8Entry entry) {
        write(entry.getIndex(), entry.getTag(), entry.get().getBytes(StandardCharsets.UTF_8));
        write('\0');
    }




    @Override
    public void writeFunction(CompiledFunction function) {
        write(Conversion.to2Bytes(function.getIndex()));
        write(function.getName());

        List<CompiledFunction.Parameter> parameters = function.getParameters();
        write(Conversion.to2Bytes(parameters.size()));
        for (CompiledFunction.Parameter parameter : parameters) {
            write(parameter.name);
            if (parameter.defaultValueRef < 0){
                write(0);
            }
            else {
                write(1);
                write(Conversion.to2Bytes(parameter.defaultValueRef));
            }
        }

        write(Conversion.to2Bytes(function.getStackSize()));
        write(Conversion.to2Bytes(function.getRegisterAmount()));

        List<Instruction> instructions = function.getInstructions();
        write(Conversion.getBytes(instructions.size()));
        for (Instruction instruction : instructions) {
            instruction.write(this);
        }
    }




    @Override
    public void writeBinaryOperation(BinaryOperation inst) {
        Opcode op = switch (inst.operation){
            case ADD -> Opcode.ADD;
            case SUB -> Opcode.SUB;
            case MUL -> Opcode.MUL;
            case DIV -> Opcode.DIV;
            case MOD -> Opcode.MOD;
            case AND -> Opcode.AND;
            case OR -> Opcode.OR;
            case XOR -> Opcode.XOR;
            case IDIV -> Opcode.IDIV;
            case POW -> Opcode.POW;
            case SHIFT_AL -> Opcode.SLA;
            case EQUALS -> Opcode.EQUALS;
            case NOT_EQUALS -> Opcode.NOT_EQUALS;
            case LESS -> Opcode.LT;
            case GREATER -> Opcode.GT;
            case LESS_EQ -> Opcode.LEQ;
            case GREATER_EQ -> Opcode.GEQ;
            case SHIFT_AR -> Opcode.SRA;
        };
        write(op);
    }

    @Override
    public void writeBranchIfFalse(BranchIfFalse inst) {
        write(Opcode.BRANCH_IF_FALSE, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeBranchIfTrue(BranchIfTrue inst) {
        write(Opcode.BRANCH_IF_TRUE, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeBranchItr(BranchItr inst) {
        write(Opcode.BRANCH_ITR, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeCallInplace(CallInplace inst) {
        write(Opcode.CALL_INPLACE, (byte) inst.argc);
    }

    @Override
    public void writeCallMapped(CallMapped inst) {
        write(Opcode.CALL_MAPPED, (byte) inst.argc);
    }

    @Override
    public void writeCallSuper(CallSuper inst) {
        write(Opcode.CALL_SUPER, (byte) inst.argc);
    }

    @Override
    public void writeContainerRead(ContainerRead inst) {
        write(Opcode.CONTAINER_READ);
    }

    @Override
    public void writeContainerWrite(ContainerWrite inst) {
        write(Opcode.CONTAINER_WRITE);
    }

    @Override
    public void writeDup(Dup inst) {
        write(Opcode.DUP);
    }

    @Override
    public void writeEnterTry(EnterTry inst) {
        write(Opcode.ENTER_TRY, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeEquals(Equals inst) {
        write(Opcode.EQUALS);
    }

    @Override
    public void writeGetItr(GetItr inst) {
        write(Opcode.GET_ITR);
    }

    @Override
    public void writeGetType(GetType inst) {
        write(Opcode.GET_TYPE);
    }

    @Override
    public void writeGoto(Goto inst) {
        write(Opcode.GOTO, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeImport(Import inst) {
        write(Opcode.IMPORT, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeItrNext(ItrNext inst) {
        write(Opcode.ITR_NEXT);
    }

    @Override
    public void writeLeaveTry(LeaveTry inst) {
        write(Opcode.LEAVE_TRY);
    }

    @Override
    public void writeLoadAbstract(LoadAbstract inst) {
        write(Opcode.LOAD_ABSTRACT, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeLoadConst(LoadConst inst) {
        write(Opcode.LOAD_CONST, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeLoadExternal(LoadExternal inst) {
        write(Opcode.LOAD_EXTERNAL, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeStoreExternal(StoreExternal inst) {
        write(Opcode.STORE_EXTERNAL, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeLoadInternal(LoadInternal inst) {
        write(Opcode.LOAD_INTERNAL, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeStoreInternal(StoreInternal inst) {
        write(Opcode.STORE_INTERNAL, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeLoadGlobal(LoadGlobal inst) {
        write(Opcode.LOAD_GLOBAL, (byte) inst.address);
    }

    @Override
    public void writeStoreGlobal(StoreGlobal inst) {
        write(Opcode.STORE_GLOBAL, (byte) inst.address);
    }

    @Override
    public void writeLoadLocal(LoadLocal inst) {
        write(Opcode.LOAD_LOCAL, (byte) inst.address);
    }

    @Override
    public void writeStoreLocal(StoreLocal inst) {
        write(Opcode.STORE_LOCAL, (byte) inst.address);
    }

    @Override
    public void writeLoadSuper(LoadSuper inst) {
        write(Opcode.LOAD_SUPER, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeStoreSuper(StoreSuper inst) {
        write(Opcode.STORE_SUPER, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeLoadBuiltin(LoadBuiltin inst) {
        write(Opcode.LOAD_BUILTIN, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeLoadName(LoadName inst) {
        write(Opcode.LOAD_NAME, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeLoadNative(LoadNative inst) {
        write(Opcode.LOAD_NATIVE, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeLoadStatic(LoadStatic inst) {
        write(Opcode.LOAD_STATIC, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeStoreStatic(StoreStatic inst) {
        write(Opcode.STORE_STATIC, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeLoadType(LoadType inst) {
        write(Opcode.LOAD_TYPE, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeLoadVirtual(LoadVirtual inst) {
        write(Opcode.LOAD_VIRTUAL, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeMakeArray(MakeArray inst) {
        write(Opcode.MAKE_ARRAY, (byte) inst.count);
    }

    @Override
    public void writeMakeDict(MakeDict inst) {
        write(Opcode.MAKE_DICT, (byte) inst.count);
    }

    @Override
    public void writeMakeRange(MakeRange inst) {
        write(Opcode.MAKE_RANGE);
    }

    @Override
    public void writeNeg(Neg inst) {
        write(Opcode.NEG);
    }

    @Override
    public void writeNewLine(NewLine inst) {
        write(Opcode.NEW_LINE, Conversion.getBytes(inst.line));
    }

    @Override
    public void writeNot(Not inst) {
        write(Opcode.NOT);
    }

    @Override
    public void writeNotEquals(NotEquals inst) {
        write(Opcode.NOT_EQUALS);
    }

    @Override
    public void writePop(Pop inst) {
        write(Opcode.POP);
    }

    @Override
    public void writePos(Pos inst) {
        write(Opcode.POS);
    }

    @Override
    public void writePushNull(PushNull inst) {
        write(Opcode.PUSH_NULL);
    }

    @Override
    public void writePushBool(PushBool inst) {
        write(Opcode.PUSH_BOOL, (byte) (inst.value ? 1 : 0));
    }

    @Override
    public void writePushInt(PushInt inst) {
        write(Opcode.PUSH_INT, inst.value.byteValue());
    }

    @Override
    public void writePushThis(PushThis inst) {
        write(Opcode.PUSH_THIS);
    }

    @Override
    public void writeReturn(Return inst) {
        write(Opcode.RETURN);
    }

    @Override
    public void writeSetOwner(SetOwner inst) {
        write(Opcode.SET_OWNER);
    }

    @Override
    public void writeThrow(Throw inst) {
        write(Opcode.THROW);
    }

    @Override
    public void writeToInplaceArg(ToInplaceArg inst) {
        write(Opcode.TO_INP_ARG);
    }

    @Override
    public void writeToMapArg(ToMapArg inst) {
        write(Opcode.TO_MAP_ARG, Conversion.to2Bytes(inst.address));
    }

    @Override
    public void writeUse(Use inst) {
        write(Opcode.USE);
    }



    @Override
    public void writeClass(CompiledClass compiledClass) {
        write(Conversion.to2Bytes(compiledClass.getIndex()));
        write(compiledClass.getName());
        if (compiledClass.getSuperIndex() == -1) {
            write(0);
        }
        else {
            write(1);
            write(Conversion.to2Bytes(compiledClass.getSuperIndex()));
        }
        write(compiledClass.isAbstract() ? 1 : 0);
        if (compiledClass.getConstructorIndex() == -1) {
            write(0);
        }
        else {
            write(1);
            write(Conversion.to2Bytes(compiledClass.getConstructorIndex()));
        }
        if (compiledClass.getStaticInitializerIndex() == -1) {
            write(0);
        }
        else {
            write(1);
            write(Conversion.to2Bytes(compiledClass.getStaticInitializerIndex()));
        }
        write(compiledClass.getStaticMembers());
        write(compiledClass.getInstanceMembers());
    }

    private void write(List<CompiledClass.Member> members){
        write(Conversion.to2Bytes(members.size()));
        for (CompiledClass.Member member : members) {
            write(member.name);
            int specs = switch (member.visibility){
                case PUBLIC -> 1;
                case PROTECTED -> 2;
                case PRIVATE -> 4;
            };
            if (member.isMutable)
                specs |= 8;
            write(specs);
        }
    }

}
