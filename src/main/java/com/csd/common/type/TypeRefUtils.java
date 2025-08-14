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
            if (!isAssignable(srcArg.getTypeRef(), tgtArg.getTypeRef())) return false;
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
                if (!deepEquals(argA.getTypeRef(), argB.getTypeRef())) return false;
            }
        }

        return true;
    }

    /**
     * Finds the Greatest Common Type among a list of TypeRefs.
    * Returns null if the list is null/empty or if no common type exists.
    */
    public static TypeRef GCTypeInList(List<TypeRef> typeRefs) {
        if (typeRefs == null || typeRefs.isEmpty()) {
            return null;
        }
    
        TypeRef pivot = typeRefs.get(0);
        for (int i = 1; i < typeRefs.size(); i++) {
            pivot = GCType(pivot, typeRefs.get(i));
            if (pivot == null) {
                return null; // No common type exists
            }
        }
        return pivot;
    }


    /**
     * Calculates the Greatest Common Type (most specific type that is assignable from both).
     * If either type has bounds, try to keep the more concrete one.
     * If both are concrete, return the more specific one if assignable.
     * Returns null if no common type exists.
     */
    public static TypeRef GCType(TypeRef a, TypeRef b) {
        if (a == null || b == null) return null;

        // Exact equality → just return one of them
        if (deepEquals(a, b)) return a;

        // Case: both concrete
        if (isConcrete(a) && isConcrete(b)) {
            boolean aToB = isAssignable(a, b);
            boolean bToA = isAssignable(b, a);

            if (aToB && bToA) {
                // They are mutually assignable (same raw type, likely)
                return a; // or b, equivalent here
            }
            if (aToB) return b; // b is more general
            if (bToA) return a; // a is more general

            return null; // incompatible concrete types
        }

        // Case: one bound, one concrete
        if (a.isBound() && isConcrete(b)) return b;
        if (b.isBound() && isConcrete(a)) return a;

        // Case: parameterized with bounds
        if (a.hasBoundArgs() || b.hasBoundArgs()) {
            // Prefer the one with fewer bounds / more concrete args
            int aBoundCount = countBounds(a);
            int bBoundCount = countBounds(b);

            if (aBoundCount < bBoundCount && isAssignable(b, a)) return a;
            if (bBoundCount < aBoundCount && isAssignable(a, b)) return b;
            // Fall back to assignability checks
            if (isAssignable(a, b)) return b;
            if (isAssignable(b, a)) return a;
            return null;
        }

        // Default: pick the more general one if assignable
        if (isAssignable(a, b)) return b;
        if (isAssignable(b, a)) return a;

        return null;
    }

    private static int countBounds(TypeRef t) {
        if (t == null || !t.isParameterized()) return 0;
        int count = 0;
        for (TypeRef.Arg arg : t.args()) {
            if (arg.isBound()) count++;
            else count += countBounds(arg.getTypeRef());
        }
        return count;
    }


    public static boolean isConcrete(TypeRef type)
    {
        if(type == null || type.isBound() || type.hasBoundArgs()) return false;
        return true;    
    }
}
