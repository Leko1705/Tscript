package com.tscript.compiler.impl.generation.gen;

import com.tscript.compiler.impl.generation.compiled.CompiledClass;
import com.tscript.compiler.impl.generation.compiled.instruction.*;
import com.tscript.compiler.impl.generation.gen.adapter.ConstructorFunction;
import com.tscript.compiler.impl.generation.gen.adapter.EmptyConstructorFunction;
import com.tscript.compiler.impl.utils.Symbol;
import com.tscript.compiler.impl.utils.TCTree;
import com.tscript.compiler.impl.utils.TCTree.*;
import com.tscript.compiler.impl.utils.TCTreeScanner;
import com.tscript.compiler.impl.utils.Visibility;

import java.util.List;
import java.util.function.Consumer;

public class ClassGenerator extends TCTreeScanner<Void, Void> {

    private final Context context;
    private final TCClassTree handled;
    private final CompClass clazz;

    private FunctionGenerator staticBlockGenerator;
    private FunctionGenerator constructorGenerator;

    public ClassGenerator(Context context, TCClassTree generated) {
        this.context = context;
        this.handled = generated;
        clazz = new CompClass(generated.name, context.getNextClassIndex(), generated.sym.isAbstract());
    }



    public int generate(){
        clazz.constructorIndex = -1;
        clazz.staticIndex = -1;
        clazz.superIndex = 0;

        if (handled.superName != null && !handled.superName.isEmpty()) {
            // TODO compile super class first
        }

        for (TCTree member : handled.members)
            member.accept(this);

        if (clazz.constructorIndex == -1){
            constructorGenerator = new FunctionGenerator(context, new EmptyConstructorFunction(handled.location, handled.sym.name));
            clazz.constructorIndex = constructorGenerator.generate(List.of());
        }

        context.getFile().classes.add(clazz);
        return clazz.getIndex();
    }


    @Override
    public Void visitConstructor(TCConstructorTree node, Void unused) {

        TCFunctionTree transformed = new ConstructorFunction(node, handled.name);
        constructorGenerator = new FunctionGenerator(context, transformed);

        Consumer<CompFunc> postParamLoadGen = compFunc -> {
            if (handled.superName == null || handled.superName.isEmpty()) return;
            GenUtils.genCall(context, node.superArgs, () -> {
                constructorGenerator.stackGrows();
                compFunc.getInstructions().add(new LoadVirtual(compFunc.getIndex()));
                constructorGenerator.newLine(node);
            }, constructorGenerator);
        };

        clazz.constructorIndex = constructorGenerator.generate(List.of(), postParamLoadGen, compFunc -> {
            compFunc.getInstructions().add(new PushThis());
            compFunc.getInstructions().add(new Return());
            constructorGenerator.stackGrows();
            constructorGenerator.stackShrinks();
        });

        return null;
    }

    @Override
    public Void visitFunction(TCFunctionTree node, Void unused) {
        FunctionGenerator gen = new FunctionGenerator(context, node);
        int refIndex = gen.generate(List.of());

        addMember(node.sym);
        return null;
    }

    @Override
    public Void visitVarDef(TCVarDefTree node, Void unused) {

        addMember(node.sym);
        return null;
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

    private static Visibility visibility(Symbol sym){
        if (sym.isPublic()) return Visibility.PUBLIC;
        if (sym.isProtected()) return Visibility.PROTECTED;
        if (sym.isPrivate()) return Visibility.PRIVATE;
        throw new AssertionError();
    }
}
