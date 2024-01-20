package tscriptc.generation;

import tscriptc.tree.Modifier;
import tscriptc.util.Conversion;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Type implements Writeable {

    private final String name;
    private final String superName;
    private boolean isAbstract = false;
    private final List<Member> members = new ArrayList<>();
    private final List<Member> statics = new ArrayList<>();

    private final int constructorAddress;
    private final int staticBlockAddress;

    public Type(String name, String superName, int constructorAddress, int staticBlockID) {
        this.name = name;
        this.superName = superName;
        this.constructorAddress = constructorAddress;
        this.staticBlockAddress = staticBlockID;
    }

    public String getName() {
        return name;
    }

    public void add(String name, Set<Modifier> modifiers){
        if (modifiers.contains(Modifier.STATIC))
            statics.add(new Member(name, modifiers));
        else
            members.add(new Member(name, modifiers));
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        out.write(name.getBytes(StandardCharsets.UTF_8));
        out.write('\0');
        out.write(superName != null ? 1 : 0);
        if (superName != null) {
            out.write(superName.getBytes(StandardCharsets.UTF_8));
            out.write('\0');
        }
        out.write(isAbstract ? 1 : 0);
        out.write(Conversion.getBytes(constructorAddress));
        out.write(Conversion.getBytes(staticBlockAddress));
        out.write(Conversion.getBytes(members.size() + statics.size()));
        for (Member m : members)
            m.write(out);
        for (Member m : statics)
            m.write(out);

    }


    @Override
    public void writeReadable(OutputStream out) throws IOException {
        String s = "Type " + name;
        if (superName != null) s += "(" + superName + ")";
        s += ":";
        if (isAbstract) s += " (ABSTRACT)";
        s += "\n\t constructor at: " + constructorAddress + '\n';
        s += "\t @static at: " + staticBlockAddress + '\n';
        out.write(s.getBytes(StandardCharsets.UTF_8));
        for (Member member : members)
            member.writeReadable(out);
        for (Member member : statics)
            member.writeReadable(out);
    }


    private record Member(String name, Set<Modifier> modifiers) implements Writeable {

        @Override
        public void write(OutputStream out) throws IOException {
            out.write(name.getBytes(StandardCharsets.UTF_8));
            out.write('\0');
            int specs = 0;
            for (Modifier m : modifiers)
                specs = apply(specs, m);
            out.write(Conversion.getBytes(specs));
        }

        @Override
        public void writeReadable(OutputStream out) throws IOException {
            String s = "\t" + name + " " + modifiers + "\n";
            out.write(s.getBytes());
        }


        private int apply(int specs, Modifier m){
            return specs | switch (m){
                case PUBLIC -> 1;
                case PROTECTED -> 2;
                case PRIVATE -> 4;
                case STATIC -> 8;
                case ABSTRACT -> 16;
                case IMMUTABLE -> 32;
                case OVERRIDDEN -> 64;
            };
        }


    }


}
