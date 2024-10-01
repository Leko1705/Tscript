package com.tscript.compiler.impl.generation.gen;

import com.tscript.compiler.impl.generation.compiled.CompiledClass;
import com.tscript.compiler.impl.generation.compiled.instruction.*;
import com.tscript.compiler.impl.generation.gen.adapter.ConstructorFunction;
import com.tscript.compiler.impl.generation.gen.adapter.EmptyConstructorFunction;
import com.tscript.compiler.impl.generation.gen.adapter.Method;
import com.tscript.compiler.impl.generation.gen.adapter.StaticBlock;
import com.tscript.compiler.impl.utils.*;
import com.tscript.compiler.impl.utils.TCTree.*;
import com.tscript.compiler.source.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ClassGenerator extends TCTreeScanner<Void, Void> {

    private final Context context;
    private final TCClassTree handled;
    private final CompClass clazz;

    private FunctionGenerator staticBlockGenerator;

    private FunctionGenerator constructorGenerator;
    List<Runnable> constructorGens = new ArrayList<>();
    List<Runnable> preSuperCallGens = new ArrayList<>();

    public ClassGenerator(Context context, TCClassTree generated) {
        this.context = context;
        this.handled = generated;
        clazz = new CompClass(generated.name, generated.sym.classIndex, generated.sym.isAbstract());
    }


    public int generate(){
        clazz.constructorIndex = -1;
        clazz.staticIndex = -1;
        clazz.superIndex = -1;

        if (handled.superName != null && !handled.superName.isEmpty() && handled.sym.superClass != null) {
            clazz.superIndex = handled.sym.superClass.classIndex;
        }

        for (TCTree member : handled.members)
            member.accept(this);

        return complete();
    }

    public int complete(){
        if (constructorGenerator == null){
            constructorGenerator = new FunctionGenerator(context, new EmptyConstructorFunction(handled.location, handled.sym.name));

            constructorGenerator
                    .genParams();

            for (Runnable r : preSuperCallGens)
                r.run();

            if (handled.superName != null && !handled.superName.isEmpty()){
                constructorGenerator.addInstructions(List.of(new CallSuper(0), new Pop()));
                constructorGenerator.stackGrows();
                constructorGenerator.stackShrinks();
                for (Runnable r : constructorGens)
                    r.run();
            }

            constructorGenerator.genReturn(compFunc -> {
                compFunc.getInstructions().add(new PushThis());
                compFunc.getInstructions().add(new Return());
            });

            constructorGenerator.stackGrows();
            constructorGenerator.stackShrinks();

        }
        else {
            constructorGenerator
                    .genParams();

            for (Runnable r : preSuperCallGens)
                r.run();

            if (handled.superName != null && !handled.superName.isEmpty()) {
                for (Runnable r : constructorGens)
                    r.run();
            }

            constructorGenerator.genBody()
                    .genReturn(compFunc -> {
                        compFunc.getInstructions().add(new PushThis());
                        compFunc.getInstructions().add(new Return());
                    });

        }

        clazz.constructorIndex = constructorGenerator.complete();

        if (staticBlockGenerator != null){
            clazz.staticIndex = staticBlockGenerator.genReturn(null).complete();
        }

        for (Symbol sym : handled.sym.subScope){
            addMember(sym);
        }

        context.getFile().classes.add(clazz);
        return clazz.getIndex();
    }


    @Override
    public Void visitConstructor(TCConstructorTree node, Void unused) {
        TCFunctionTree transformed = new ConstructorFunction(node, handled.name);
        constructorGenerator = new FunctionGenerator(context, transformed);

        constructorGens.add(0, () -> {
            GenUtils.genArgFetch(context, node.superArgs, constructorGenerator);
            constructorGenerator.addInstructions(List.of(new CallSuper(node.superArgs.size()), new Pop()));
            constructorGenerator.stackShrinks(node.superArgs.size());
        });

        return null;
    }

    @Override
    public Void visitFunction(TCFunctionTree node, Void unused) {
        if (node.sym.isAbstract()) return null;

        BiConsumer<Instruction, Integer> instructionAdder = (instruction, growth) -> {
            if (node.sym.isStatic()) {
                staticBlock(node).addInstructions(List.of(instruction));
                staticBlock(node).stackGrows(growth);
            }
            else {
                preSuperCallGens.add(() -> {
                    constructorGenerator.addInstructions(List.of(instruction));
                    constructorGenerator.stackGrows(growth);
                });
            }
        };


        instructionAdder.accept(new PushThis(), 1);

        if (node.sym.isNative()) {
            int poolAddr = PoolPutter.putUtf8(context, node.name);
            Instruction loadInstr = new LoadNative(poolAddr);
            instructionAdder.accept(loadInstr, 1);
        }
        else {
            TCFunctionTree method = new Method(node, handled.name);
            FunctionGenerator gen = new FunctionGenerator(context, method);
            int refIndex = gen.genParams().genBody().genReturn(null).complete();
            Instruction loadInstr = new LoadVirtual(refIndex);
            instructionAdder.accept(loadInstr, 1);
        }

        instructionAdder.accept(new SetOwner(), -1);

        Instruction storeInstr = new StoreInternal(PoolPutter.putUtf8(context, node.name));
        instructionAdder.accept(storeInstr, -1);

        return null;
    }

    @Override
    public Void visitVarDef(TCVarDefTree node, Void unused) {

        if (node.sym.isStatic()){
            FunctionGenerator staticBlock = staticBlock(node);
            genVarInit(node, staticBlock);
        }
        else {
            constructorGens.add(() -> genVarInit(node, constructorGenerator));
        }

        return null;
    }

    private void genVarInit(TCVarDefTree node, FunctionGenerator staticBlock) {
        staticBlock.scan(node.initializer, null);
        if (node.initializer == null) {
            staticBlock.addInstructions(List.of(new PushNull()));
            staticBlock.stackGrows();
        }
        staticBlock.addInstructions(List.of(new StoreInternal(PoolPutter.putUtf8(context, node.name))));
        staticBlock.stackShrinks();
    }

    private void addMember(Symbol symbol){
        CompiledClass.Member member =
                CompiledClass.Member.of(
                        symbol.name,
                        visibility(symbol),
                        !symbol.isConstant());

        if (symbol.isStatic()){
            clazz.staticMembers.add(member);
        }
        else {
            clazz.instanceMembers.add(member);
        }
    }

    private FunctionGenerator staticBlock(Tree tree){
        if (staticBlockGenerator == null){
            staticBlockGenerator = new FunctionGenerator(context, new StaticBlock(tree.getLocation(), handled.name));
        }
        return staticBlockGenerator;
    }

    private static Visibility visibility(Symbol sym){
        if (sym.isPublic()) return Visibility.PUBLIC;
        if (sym.isProtected()) return Visibility.PROTECTED;
        if (sym.isPrivate()) return Visibility.PRIVATE;
        throw new AssertionError();
    }

}
