package com.csd.common.type;

import java.util.List;

/**
 * Utility methods for working with TypeRef instances.
 */
public final class TypeRefUtils {

    private TypeRefUtils() {}

    /**
     * Checks if `source` TypeRef can be assigned to `target` TypeRef.
     * This mimics Java's assignability rules, with support for bound arguments.
     */
    public static boolean isAssignable(TypeRef source, TypeRef target) {
        if (target.isBound()) return true;
        if (!target.rawType().isAssignableFrom(source.rawType())) return false;
        if (!target.isParameterized()) return true;
        if (target.hasBoundArgs()) return true;
        if (!source.isParameterized()) return false;
        if (source.arity() != target.arity()) return false;

        List<TypeRef.Arg> sourceArgs = source.args();
        List<TypeRef.Arg> targetArgs = target.args();

        for (int i = 0; i < sourceArgs.size(); i++) {
            TypeRef.Arg srcArg = sourceArgs.get(i);
            TypeRef.Arg tgtArg = targetArgs.get(i);

            if (tgtArg.isBound()) continue; // Accept anything
            if (srcArg.isBound()) return false; // Can't assign a bound to a concrete

            // Recursively check assignability of concrete args
            if (!isAssignable(srcArg.concrete().get(), tgtArg.concrete().get())) return false;
        }

        return true;
    }

    /**
     * Deep equality check between two TypeRefs.
     * Considers raw type and all type arguments.
     */
    public static boolean deepEquals(TypeRef a, TypeRef b) {
        if (a == b) return true;
        if (a == null || b == null) return false;

        if (a.isBound() != b.isBound()) return false;
        if (a.rawType() != b.rawType()) return false;
        if (a.arity() != b.arity()) return false;

        List<TypeRef.Arg> argsA = a.args();
        List<TypeRef.Arg> argsB = b.args();

        for (int i = 0; i < argsA.size(); i++) {
            TypeRef.Arg argA = argsA.get(i);
            TypeRef.Arg argB = argsB.get(i);

            if (argA.isBound() != argB.isBound()) return false;
            if (!argA.isBound()) {
                if (!deepEquals(argA.concrete().get(), argB.concrete().get())) return false;
            }
        }

        return true;
    }

    public static boolean isConcrete(TypeRef type)
    {
        if(type == null || type.isBound() || type.hasBoundArgs()) return false;
        return true;    
    }
}
