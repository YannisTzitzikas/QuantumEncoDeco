/**
 * 
 */
package com.ics.Ppipeline;

import java.io.IOException;

import com.ics.config.AConfig;
import com.ics.io.readers.BReader;
import com.ics.io.writers.EWritter;

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
	 * Checks if the configuration exists and prints start and the data of the  configuration file
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
	 * For each line of the inputfile it writes a fixed bit string to the output file
	 */
public void transformDummy() {
	int linesWritten=0;
	if (isConfigurationOk() ) {
		BReader r = new BReader(config.getInputfilepath());
		EWritter w = new EWritter(config.getOutputfilepath());
		w.writePrefix(); // Writing a prefix to the file
		try {
			while ((r.bfr.readLine()) !=null ) {   // line by line processing
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
		System.out.println("Pipeline completed. Wrote " + linesWritten + " lines at the output file.");
	}		
 }	


/**
 * Incomplete
 */
public void encodeTODO() {
int linesWritten=0;
if (isConfigurationOk() ) {
	BReader r = new BReader(config.getInputfilepath());
	EWritter w = new EWritter(config.getOutputfilepath());
	w.writePrefix(); // Writing a prefix to the file
		
	try {
		/*
		 * R1: 
		 *  1/ Edw prepei na pairnoume ola ta URIs tou montelou
		 *     (pou emfanizontai subjects, predicate, object) - mesw klhsewn thw Jena (opws sto paradeigma OntologyReader)
		 *  2/ Na tous dinoume ena auksonta arithmo 
		 *  3/ Na ftiaxnoume mia eggrafh sto Dictionary (URI - bitstring)
		 *      Ta bitstring analoga me to posa xreiazontai
		 *      To config file prepei na exei kai dictionaryName
		 *      (isws kai mode: encode, decode)
		 *  4/ Meta na ksanadiavazoume ta statements kai ena ena na to kwdikopoiome
		 *     kai na to grafoume sto output file
		 *  5/ gia test tha mporsouame kapou na kanoume kai decoding
		 */
		
	} catch (Exception e) {
		e.printStackTrace();
	}
	r.close();
	w.close();
	System.out.println("Pipeline completed. Wrote " + linesWritten + " lines at the output file.");
}		
}	
		
}

class PipelineClient {
	public static void main(String[] lala) {
		
		Pipeline p = new Pipeline("Resources/configFiles/configAristotle.json");
		p.transformDummy();
		
	}
}

