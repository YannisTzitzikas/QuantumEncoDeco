/**
 * 
 */
package Ppipeline;

import java.io.IOException;
import java.util.List;

import Aconfig.AConfig;
import Breaders.OntologyReader;
import Ctransformers.EncoderFactory;
import Ctransformers.EncodingData;
import Ctransformers.StatisticsCollector;
import Ctransformers.TripleComponent;
import Ctransformers.URITriple;
import Ctransformers.decoder.DecoderFactory;
import Ctransformers.decoder.IDecoder;
import Ctransformers.encode.IEncoder;
import Ewritters.encoding.EncoderWriterFactory;
import Ewritters.encoding.IEncodeWriter;

/**
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 *         * Pipeline of tasks, for now only a few tasks
 */

public class Pipeline {
    AConfig config = null; // each pipeline should have a configration

    /**
     * Constructor
     * 
     * @param configfile
     */
    public Pipeline(String configfile) {
        config = new AConfig(configfile);
    }

    /**
     * Checks if the configuration exists and prints start and the data of the
     * configuration file
     * 
     * @return
     */
    boolean isConfigurationOk() {
        if (config == null) {
            throw new RuntimeException("Pipeline without config file");
        }
        System.out.println("\nPipeline start.");
        System.out.println("\nPipeline configuration:");
        System.out.println(config);
        return true;
    }

public void decode() {
    try {
        IDecoder decoder = DecoderFactory.createDecoder(config.getEncoding()); 
        decoder.loadMappings(config.getMappingFile());
        decoder.decodeFile(config.getInputfilepath(), config.getOutputfilepath());
    } catch (IOException e) {
        e.printStackTrace();
    }
}


    public void encode() {
        int linesWritten = 0;
        if (isConfigurationOk()) {
            
            StatisticsCollector collector = new StatisticsCollector();
            String encodingType = config.getEncoding();
            OntologyReader or   = new OntologyReader(collector); 

            List<URITriple> triples = or.readTriplesFromPath(config.getInputfilepath());

            EncoderFactory encoFactory = new EncoderFactory();
            IEncoder<?> encoder        = encoFactory.createEncoder(encodingType);
            IEncodeWriter writer       = EncoderWriterFactory.getWriter(encodingType);

            try {
                triples.forEach((triple) -> {
                    encoder.encode(new EncodingData(triple.getObject(), TripleComponent.OBJECT));
                    encoder.encode(new EncodingData(triple.getSubject(), TripleComponent.SUBJECT));
                    encoder.encode(new EncodingData(triple.getPredicate(), TripleComponent.PREDICATE));
                });

                encoder.saveMappings(config.getMappingFile());
                linesWritten = writer.write(triples, encoder, collector, config.getOutputfilepath());   

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Pipeline completed. Wrote " + linesWritten + " lines at the output file.");
            System.out.println(collector);
        }
    }

    public void process()
    {
        if(config.getMode().equals("encode"))
        {
            encode();
        }
        else
        {
            decode();
        }
    }
}
