package com.csd.core.model.uri;

public final class URITriple {
    private final TripleComponent subject;
    private final TripleComponent predicate;
    private final TripleComponent object;

    public URITriple(TripleComponent subject, TripleComponent predicate, TripleComponent object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public TripleComponent getSubject() { return subject; }
    public TripleComponent getPredicate() { return predicate; }
    public TripleComponent getObject() { return object; }

    @Override
    public String toString() {
        return "(" + subject + ", " + predicate + ", " + object + ")";
    }
}
