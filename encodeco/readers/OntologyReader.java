/**
 * 
 */
package readers;

import java.awt.Desktop;
import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;

import java.net.URI;

/**
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 * Reading an ontology  from the folder Resources.
 * Can call external visualiation service
 */
public class OntologyReader {
	
	/**
	 * 
	 * @param filenameToRead
	 * @return
	 */
	public String readTriplesFromPath(String filenameToRead) {
		String ret="";
		Model model = ModelFactory.createDefaultModel();
		FileManager.get().readModel(model, filenameToRead);
		// Iterate over the triples in the model
        StmtIterator iter = model.listStatements();

        // Print out each triple
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement(); // get next statement
            Resource subject = stmt.getSubject();   // get the subject
            Property predicate = stmt.getPredicate(); // get the predicate
            RDFNode object = stmt.getObject();     // get the object

            // Print the triple
            ret = "Subject: " + subject.toString();
            ret+=" | Predicate: " + predicate.toString();
            ret+=" | Object: " + object.toString();
           
        }
		
		
		//InputStream inputStream = getClass().getResourceAsStream(filenameToRead);
		System.out.print(ret);
        return ret; 
        
        /*
        System.out.println("\n--[Print what was read from the file:" + filenameToRead);
        model.write(System.out);
        System.out.println("\n--[Print in Turtle what was read from the file:" + filenameToRead);
        model.write(System.out, "TURTLE");
        System.out.println(model);
        */
        
  }

	/**
	 * Reads an RDF/XML file, load it as a Jena Mode,  and prints it, also in TURTLE format (tested ok)
	 * @param filenameToRead
	 */

	 void readFromPath(String filenameToRead, String format) {
		InputStream inputStream = getClass().getResourceAsStream(filenameToRead);
		Model model = ModelFactory.createDefaultModel();
        try {
             model.read(inputStream, "http://ex.org/", format);            
        } catch (Exception e) {
            System.out.println(e);
        }          
        //System.out.println("\n--[Print what was read from the file:" + filenameToRead);
        //model.write(System.out);
        System.out.println("\n--[Print in Turtle what was read from the file:" + filenameToRead);
        model.write(System.out, "TURTLE");
        System.out.println("Turtle end");
       
        // new begin
        String ret=""; 
        StmtIterator iter = model.listStatements();  // Iterate over the triples in the model
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement(); // get next statement
            Resource subject = stmt.getSubject();   // get the subject
            Property predicate = stmt.getPredicate(); // get the predicate
            RDFNode object = stmt.getObject();     // get the object

            // Print the triple
            ret+= "\n\t Subject: " + subject.toString();
            ret+=" \n\t\t Predicate: " + predicate.toString();
            ret+=" \n\t\t | Object: " + object.toString();
        }
        System.out.println(ret);
            // new end
       // System.out.println(model);
        
	}
	 
	/**
	 * Calls webvowl to visualize an ontology
	 * @param url  the url of an ontology (should be web accessible)
	 * Status: ok
	 */
	void visualize(String url) {
		//String v7 = "https://service.tib.eu/webvowl/#iri=https://cidoc-crm.org/rdfs/7.1.1/CIDOC_CRM_v7.1.1.rdfs";
		//String v6 ="https://service.tib.eu/webvowl/#iri=http://www.cidoc-crm.org/sites/default/files/cidoc_crm_v6.2-2018April.rdfs";
		
		String urlwithParam = "https://service.tib.eu/webvowl/#iri="+url;
		
		try {
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			    Desktop.getDesktop().browse(new URI(urlwithParam));
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		
			
	}
	
	/**
	 * Demo cases
	 * @param lala
	 */
	
	public static void main(String[] lala) {
		// files in RDF/XML
		String[]  KFfilesToTest = {
				//"/ontologies/cidoc_crm_v6.2-2018April.rdfs.xml",   // cidoc crm 2018
				//"/ontologies/CIDOC_CRM_v7.1.1.rdfs.xml",  // cirod crm  2021
				"/datafiles/Aristotle.xml",
				//"Resources/datafiles/toyInput.rdf";
		};
		OntologyReader a = new OntologyReader();
		for (String file: 	KFfilesToTest) {
			a.readFromPath(file,"RDF/XML"); //
		}
		
	
		//a.readTriplesFromPath("Resources/ontologies/CIDOC_CRM_v7.1.1.rdfs.xml");
			
		/* VISUALIZATION DEMO*/
		//a.visualize("https://cidoc-crm.org/rdfs/7.1.1/CIDOC_CRM_v7.1.1.rdfs");
		
	}
   

}
