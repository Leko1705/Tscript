package com.tscript.tscriptc.generation;

import com.tscript.tscriptc.util.Conversion;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Compiled implements Writeable {

    private static final int magicNumber = 0xDEAD;

    private static final int
            minor = 0,
            major = 0;

    private final ConstantPool pool = new ConstantPool();

    private final Map<String, Function> functions = new HashMap<>();

    private final Map<String, Type> types = new HashMap<>();

    private final ArrayDeque<Function> functionStack = new ArrayDeque<>();

    private int entryPoint = 0;
    private int globalRegs = 0;


    public Compiled(){
        this.entryPoint = putFunction("__main__");
        Function mainFunc = new Function("__main__", 0);
        functions.put("__main__", mainFunc);
        functionStack.push(mainFunc);
    }

    public void setGlobalRegs(int globalRegs) {
        this.globalRegs = globalRegs;
    }

    public int putInt(int i){
        return pool.putInt(i);
    }

    public int putReal(double d){
        return pool.putReal(d);
    }

    public int putStr(String s){
        return pool.putStr(s);
    }

    public int putUTF8(String s){
        return pool.putUTF8(s);
    }

    public int putFunction(String s){
        return pool.putFunc(s);
    }

    public int putNative(String s){
        return pool.putNative(s);
    }

    public int putType(String s) {
        return pool.putType(s);
    }

    public int putBool(boolean b){
        return pool.putBool(b);
    }

    public int putNull(){
        return pool.putNull();
    }

    public int putArray(List<Integer> references) {
        return pool.putArray(references);
    }

    public int putDict(List<Integer> references){
        return pool.putDict(references);
    }

    public int putRange(int from, int to) {
        return pool.putRange(from, to);
    }

    public int putImported(String importPath) {
        return pool.putImport(importPath);
    }

    public void addInstruction(Instruction instruction){
        currentFunction().addInstruction(instruction);
    }

    public void stackGrows(){
        stackGrows(1);
    }

    public void stackGrows(int growth){
        currentFunction().stackChanges(growth);
    }

    public int getInstructionStreamSize(){
        return currentFunction().getInstructionStreamSize();
    }

    @Override
    public void write(OutputStream out) throws IOException {
        out.write(Conversion.getBytes(magicNumber));
        out.write(Conversion.getBytes(minor));
        out.write(Conversion.getBytes(major));
        out.write(Conversion.getBytes(entryPoint));
        out.write(Conversion.getBytes(globalRegs));

        pool.write(out);
        out.write(Conversion.getBytes(functions.size()));
        for (Function f : functions.values())
            f.write(out);

        out.write(Conversion.getBytes(types.size()));
        for (Type t : types.values())
            t.write(out);
    }

    @Override
    public void writeReadable(OutputStream out) throws IOException {
        out.write("magic number: 0xDEAD\n".getBytes());
        out.write(("minor: " + minor + "\n").getBytes());
        out.write(("major: " + major + "\n").getBytes());
        out.write(("entry-point: " + entryPoint + "\n").getBytes());
        out.write(("globals: " + globalRegs + "\n").getBytes());
        pool.writeReadable(out);

        for (Function f : functions.values()){
            out.write('\n');
            f.writeReadable(out);
        }

        for (Type t : types.values()){
            out.write('\n');
            t.writeReadable(out);
        }
    }

    public void registerFunction(String name, int argc){
        Function function = new Function(name, argc);
        functions.put(name, function);
    }


    public void enterFunction(String name) {
        Function function = functions.get(name);
        functionStack.push(function);
    }

    public void completeFunction() {
        functionStack.pop();
    }

    private Function currentFunction(){
        return functionStack.element();
    }

    public void setLocals(int locals) {
        functionStack.element().setLocals(locals);
    }

    public void addParameter(String name, int address) {
        Function function = functionStack.element();
        function.addParam(name, address);
    }

    public void addType(Type type) {
        types.put(type.getName(), type);
    }

}
