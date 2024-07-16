package com.tscript.runtime.jit;

import com.tscript.runtime.core.Data;
import com.tscript.runtime.jit.BytecodeParser.IntegerTree;
import com.tscript.runtime.jit.BytecodeParser.RootTree;
import com.tscript.runtime.jit.BytecodeParser.Tree;
import com.tscript.runtime.type.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.tscript.runtime.jit.BytecodeParser.*;

public class TypeReducer extends ParentDelegationPhase<TypeReducer.TypeInfo> {

    private final TypeInfo INTEGER = new TypeInfo("Integer");
    private final TypeInfo REAL = new TypeInfo("Real");
    private final TypeInfo STRING = new TypeInfo("String");
    private final TypeInfo NULL = new TypeInfo("NullType");
    private final TypeInfo RANGE = new TypeInfo("Range");
    private final TypeInfo ARRAY = new TypeInfo("Array");
    private final TypeInfo DICTIONARY = new TypeInfo("Dictionary");
    private final TypeInfo TYPE = new TypeInfo("Type");
    private final TypeInfo BOOLEAN = new TypeInfo("Boolean");


    private final Map<Integer, TypeInfo> localTypeInfo = new HashMap<>();


    @Override
    public TypeInfo visitRootTree(RootTree rootTree, Tree parent) {
        for (int i = 0; i < args.length; i++)
            localTypeInfo.put(i, new TypeInfo(args[i]));
        return super.visitRootTree(rootTree, parent);
    }

    @Override
    public TypeInfo visitNullTree(NullTree nullTree, Tree parent) {
        return NULL;
    }

    @Override
    public TypeInfo visitIntegerTree(IntegerTree integerTree, Tree parent) {
        return INTEGER;
    }

    @Override
    public TypeInfo visitRealTree(RealTree realTree, Tree tree) {
        return REAL;
    }

    @Override
    public TypeInfo visitBooleanTree(BooleanTree booleanTree, Tree parent) {
        return BOOLEAN;
    }

    @Override
    public TypeInfo visitStringTree(StringTree stringTree, Tree tree) {
        return STRING;
    }

    @Override
    public TypeInfo visitRangeTree(RangeTree rangeTree, Tree parent) {
        return RANGE;
    }

    @Override
    public TypeInfo visitGetTypeTree(GetTypeTree getTypeTree, Tree parent) {
        super.visitGetTypeTree(getTypeTree, parent);
        return TYPE;
    }

    @Override
    public TypeInfo visitArrayTree(ArrayTree arrayTree, Tree parent) {
        super.visitArrayTree(arrayTree, parent);
        return ARRAY;
    }

    @Override
    public TypeInfo visitDictionaryTree(DictionaryTree dictionaryTree, Tree parent) {
        super.visitDictionaryTree(dictionaryTree, parent);
        return DICTIONARY;
    }

    @Override
    public TypeInfo visitConstantTree(ConstantTree constantTree, Tree parent) {
        return null;
    }

    @Override
    public TypeInfo visitLoadMemberTree(LoadMemberTree loadTree, Tree parent) {
        TypeInfo typeInfo = scan(loadTree.exp, loadTree);

        if (typeInfo != null && typeInfo.data != null){
            TObject object = jit.unpack(typeInfo.data);
            int address = object.getIndex(loadTree.address);
            if (address >= 0){
                Tree folded = foldConstantField(object.get(address));
                parent.replace(loadTree, Objects.requireNonNullElseGet(folded, () -> new AccessUnknownFastTree(address, loadTree.exp)));
                optimizationPerformed();
            }
        }

        return null;
    }

    private Tree foldConstantField(Member member){
        if (member.kind == Member.Kind.MUTABLE) return null;
        TObject object = jit.unpack(member.data);

        if (object instanceof TInteger i){
            return new IntegerTree(i.get());
        }
        if (object instanceof TReal r){
            return new RealTree(r.get());
        }
        if (object instanceof TNull){
            return new NullTree();
        }
        if (object instanceof TBoolean b){
            return new BooleanTree(b.get());
        }
        if (object instanceof TString s){
            return new StringTree(s.get());
        }
        if (object instanceof TRange r){
            return new RangeTree(new IntegerTree(r.getFrom()), new IntegerTree(r.getTo()));
        }

        return null;
    }

    @Override
    public TypeInfo visitEqualsTree(EqualsTree equalsTree, Tree parent) {
        TypeInfo left = scan(equalsTree.left, equalsTree);
        TypeInfo right = scan(equalsTree.right, equalsTree);

        if (left != null && right != null && !typeInfosAreEqual(left, right)){
            parent.replace(equalsTree, new BooleanTree(false));
            optimizationPerformed();
        }

        return BOOLEAN;
    }

    @Override
    public TypeInfo visitLoadLocalTree(LoadLocalTree loadLocalTree, Tree parent) {
        return localTypeInfo.get(loadLocalTree.address());
    }

    @Override
    public TypeInfo visitStoreLocalTree(StoreLocalTree storeLocalTree, Tree parent) {
        TypeInfo typeInfo = scan(storeLocalTree.child, storeLocalTree);
        localTypeInfo.put(storeLocalTree.address, typeInfo);
        return null;
    }

    public class TypeInfo{
        public final String name;
        public final Data data;
        public TypeInfo(Data data){
            this.name = jit.unpack(data).getType().getName();
            this.data = data;
        }
        public TypeInfo(String name){
            this.name = name;
            this.data = null;
        }

        @Override
        public String toString() {
            return "TypeInfo{" +
                    "name='" + name + '\'' +
                    ", data=" + data +
                    '}';
        }
    }

    private boolean typeInfosAreEqual(TypeInfo t1, TypeInfo t2){
        if (t1.name == null) return false;
        if (t2.name == null) return false;
        return t1.name.equals(t2.name);
    }


}
