package com.csd.core.model.uri;

public final class TripleComponent {
    public enum Kind { IRI, LITERAL, BLANK_NODE, UNKNOWN }
    public enum Role { SUBJECT, PREDICATE, OBJECT, UNKNOWN }

    private final String value;
    private final Kind kind;
    private final Role role;

    public TripleComponent(String value, Kind kind, Role role) {
        this.value = value;
        this.kind = kind != null ? kind : Kind.UNKNOWN;
        this.role = role != null ? role : Role.UNKNOWN;
    }

    public String getValue() { return value; }
    public Kind getKind() { return kind; }
    public Role getRole() { return role; }

    @Override
    public String toString() {
        return role + " | " + kind + " | " + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TripleComponent other = (TripleComponent) obj;
        return value != null ? value.equals(other.value) : other.value == null;
    }
    
    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

}
