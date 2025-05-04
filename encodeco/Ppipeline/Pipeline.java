/**
 * 
 */
package Ppipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

import Aconfig.AConfig;
import Breaders.BReader;
import Breaders.OntologyReader;
//import Ctransformers.Rule;
import Ewritters.EWritter;

/**
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 *  * Pipeline of tasks, for now only a few tasks
 */


public class Pipeline {
	AConfig config=null; // each pipeline should have a configration
	
	/**
	 * Constructor
	 * @param configfile
	 */
	public Pipeline(String configfile) {
		   config = new AConfig(configfile);
	}
      
	
	/**
	 * Checks if the configuration exists and prints start and the read configuration
	 * @return
	 */
	boolean isConfigurationOk() {
		if (config==null) {
			throw new RuntimeException("Pipeline without config file");
		}
		System.out.println("\nPipeline start.");
		System.out.println("\nPipeline configuration:");
		System.out.println(config);
		return true;
	}
	
	
	
	/**
	 * For each line of the input file it writes a fixed bit string 
	 */
public void transformDummy() {
	int linesWritten=0;
	if (isConfigurationOk() ) {
		BReader r = new BReader(config.getInputfilepath());
		EWritter w = new EWritter(config.getOutputfilepath());
		OntologyReader or = new OntologyReader(); // reads a file
		
		
		w.writePrefix(); // Writing a prefix to the file
		String line;
		try {
			while ((line = r.bfr.readLine()) !=null ) {   // line by line processing
				//System.out.println(line);
				w.write("010010000000000000000");
				w.write("\n");
				linesWritten++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		r.close();
		w.close();
		System.out.println("Pipeline completed. Wrote " + linesWritten + " lines.");
	}		
 }	
		
	
	/**
	 * This should be completed
	 */
	public void transform() {
		
		if (isConfigurationOk() ) {
		
		BReader r = new BReader(config.getInputfilepath());
		EWritter w = new EWritter(config.getOutputfilepath());
		
		// Writing a prefix to the file
		w.writePrefix();
		
		
		OntologyReader or = new OntologyReader() ;
		System.out.println(or.readTriplesFromPath(config.getInputfilepath()));
		
		
		int linesWritten=0;
		/*
		// line by line processing
		
		String line;
		try {
			while ((line = r.bfr.readLine()) !=null ) {
				
				//System.out.println(line);
				w.write("0000000000000000000");
				w.write("\n");
				linesWritten++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//w.write("bye bye");
		*/
		
		r.close();
		w.close();
		
		System.out.println("Pipeline completed. Wrote " + linesWritten + " lines.");
	
		
		
		// put all the data in a arraylist of strings	
		/*
		ArrayList data = r.readContentsAsArrayListOfArraysToStrings("\t",false); // tab separator
		//write them all (for testing)
		for (row: data) {
			Stream.of((String[])row).forEach(e -> w.write("|"+ e + "|"));
			w.write("\n");
		}
		*/		
		/*
		ArrayList<String> data = r.readContentsAsArraylistOfStrings(); // 
		Stream.of(data).forEach(e -> w.write("|"+ e + "\n"));
		*/
		//w.write("\n");
				
		
		// apply each rule in order
		
		/*
		//for each row test all rules
		for (String row: data) {
			for (Rule rl: config.getRules()) {
				w.write(rl.apply(row));
				w.write("\n");
				//
			}
		
		
		}
		
		*/
		
		
		
		
		
		
		//return 1;
	}
	}
   
}


class PipelineClient {
	public static void main(String[] lala) {
		
		
	}
}

