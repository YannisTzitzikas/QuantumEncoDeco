package com.csd.common.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This is vital to our DAG implementation due to the fact that some stages
 * such as R1 return parameterized types e.g. Map<K,Integer>. Thus, we can only
 * connect this stages to stages that accept type Agnostic Maps e.g. <?,?> ,<?,Integer>
 * or maps that can accept parameterized keys and integers.
 * 
 * This class is a workaround to avoid using gson's TypeToken class (Reduce external dependencies)
 * 
 * @author George Theodorakis (csd4881@csd.uoc.gr)
 */

public final class TypeRef {
    private final Class<?>  rawType;
    private final List<Arg> args;       // empty for non-parameterized
    private final boolean   isBoundRef;
    private final boolean   hasBoundArgument;

    private TypeRef(boolean isBoundRef, boolean hasBoundArgument, Class<?> rawType, List<Arg> args) {
        this.isBoundRef = isBoundRef;
        this.hasBoundArgument = hasBoundArgument;
        this.rawType = Objects.requireNonNull(rawType, "rawType");
        this.args = Collections.unmodifiableList(new ArrayList<>(args));
        validateArity();
    }

    public static TypeRef bound() {
        return new TypeRef(true,false,null, Collections.<Arg>emptyList());
    }

    public static TypeRef simple(Class<?> rawType) {
        return new TypeRef(false, false, rawType, Collections.<Arg>emptyList());
    }

    public static TypeRef parameterized(Class<?> rawType, Arg... args) {
        List<Arg> tempArray = Arrays.asList(args);
        return new TypeRef(true, tempArray.stream().anyMatch(Arg::isBound), rawType, tempArray);
    }

    public int       arity()   { return args.size(); }
    public Class<?>  rawType() { return rawType; }
    public List<Arg> args()    { return args; }
    
    // Boolean
    public boolean   isBound()         { return isBoundRef; }
    public boolean   hasBoundArgs()    { return hasBoundArgument; }
    public boolean   isParameterized() { return !args.isEmpty(); }

    private void validateArity() {
        int expected = 0;
        if (rawType.getTypeParameters().length > 0) expected = rawType.getTypeParameters().length;

        if (expected != args.size()) {
            throw new IllegalArgumentException("Type " + rawType.getName() +
                " expects " + expected + " type argument(s), got " + args.size());
        }
    }

    @Override public String toString() {
        if (args.isEmpty()) return rawType.getTypeName();
        StringBuilder sb = new StringBuilder(rawType.getTypeName()).append('<');
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(args.get(i));
        }
        return sb.append('>').toString();
    }

    // --- Type argument ---
    public static final class Arg {
        private final boolean bound; // true means "bind to previous stage output (or part of it)"
        private final TypeRef concrete; // present only when not bound

        private Arg(boolean bound, TypeRef concrete) {
            if (bound && concrete != null)  throw new IllegalArgumentException("Bound arg cannot have concrete type");
            if (!bound && concrete == null) throw new IllegalArgumentException("Concrete arg must have a type");
            this.bound = bound;
            this.concrete = concrete;
        }

        public static Arg bound() { return new Arg(true, null); }
        public static Arg of(TypeRef concrete) { return new Arg(false, concrete); }

        public boolean isBound() { return bound; }
        public Optional<TypeRef> concrete() { return Optional.ofNullable(concrete); }

        @Override public String toString() {
            return bound ? "_BOUND_" : concrete.toString();
        }
    }
}
