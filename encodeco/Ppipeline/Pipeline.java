/**
 * 
 */
package Ppipeline;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;

import Aconfig.AConfig;
import Breaders.BReader;
import Breaders.OntologyReader;
import Ctransformers.URITriple;
import Ctransformers.encode.R1Encoder;
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
		System.out.println("Pipeline completed. Wrote " + linesWritten + " lines at the output file.");
	}		
 }	


/**
 * Incomplete
 */
public void encodeTODO() {
int linesWritten=0;
if (isConfigurationOk() ) {
	OntologyReader or = new OntologyReader(); // reads a file
	
	List<URITriple> triples = or.readTriplesFromPath(config.getInputfilepath());
    R1Encoder encoder = new R1Encoder();
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
		
        triples.forEach((triple) -> {
            encoder.encode(triple.getSubject());
            encoder.encode(triple.getObject());
            encoder.encode(triple.getPredicate());
        });

        encoder.saveMappings(config.getOutputfilepath() + ".map");
        linesWritten = R1Writter(triples, encoder);

	} catch (Exception e) {
		e.printStackTrace();
	}
	System.out.println("Pipeline completed. Wrote " + linesWritten + " lines at the output file.");
}		
}	

private int R1Writter(List<URITriple> triples, R1Encoder encoder)
{
    int count = encoder.getMappings().size();
    int bitsNeeded = bitCount(count);
	EWritter w = new EWritter(config.getOutputfilepath());

    triples.forEach((triple) ->{

        int s = encoder.getEncoded(triple.getSubject());
        int o = encoder.getEncoded(triple.getObject());
        int p = encoder.getEncoded(triple.getPredicate());

        String sBinary = getBinaryRepresentation(s, bitsNeeded);
        String oBinary = getBinaryRepresentation(o, bitsNeeded);
        String pBinary = getBinaryRepresentation(p, bitsNeeded);

        w.write(sBinary + oBinary + pBinary + "\n");
    });
    w.close();

    return triples.size(); 
}

private int bitCount(int count){
    if (count == 0) {
        return 1; // 0 requires 1 bit
    }
    return 32 - Integer.numberOfLeadingZeros(count);
}


private String getBinaryRepresentation(int number, int bitsNeeded) {
    String binaryString = Integer.toBinaryString(number);
    return String.format("%" + bitsNeeded + "s", binaryString).replace(' ', '0');
}
		
}

class PipelineClient {
	public static void main(String[] lala) {
		
		Pipeline p = new Pipeline("Resources/configFiles/configAristotle.json");
		p.encodeTODO();
		
	}
}

