/**
 * 
 */
package Ctransformers;

/**
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 *
 */

 /**
 * Represents an RDF triple with URI validation for subject, predicate, and object
 */
public class URITriple {
    private final String subject;
    private final String predicate;
    private final String object;

    public URITriple(String subject, String predicate, String object) {
        this.subject    = subject;
        this.predicate  = predicate;
        this.object     = object;
    }

    // Getters
    public String getSubject()   { return subject; }
    public String getPredicate() { return predicate; }
    public String getObject()    { return object; }

    /**
     * Returns the triple in N-Triples format
     */
    @Override
    public String toString() {
        return subject + "," + predicate + "," + object;
    }
}