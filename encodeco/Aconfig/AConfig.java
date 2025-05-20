/**
 * 
 */
package Aconfig;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 *  Class for configuration. It can store/read a configuration in JSON format.
 *  In this way, we can have several configutation file 
 *  (instead of having to change the code to test an alternative configuration).
 */

public class AConfig {
	private String 			inputfilepath;
	private String 			outputfilepath;
	private String 			encoding="R1";
	
	
		public String toString() {
			String i = "\tInput file: " + inputfilepath;
			String o = "\tOutput file: " + outputfilepath;
			String e = "\tEncoding: " + encoding;
			return i + "\n" + o + "\n" + e;
		}
		/**
		 * @return the inputfilepath
		 */
		public String getInputfilepath() {
			return inputfilepath;
		}
		/**
		 * @param inputfilepath the inputfilepath to set
		 */
		public void setInputfilepath(String inputfilepath) {
			this.inputfilepath = inputfilepath;
		}
		/**
		 * @return the outputfilepath
		 */
		public String getOutputfilepath() {
			return outputfilepath;
		}
		/**
		 * @param outputfilepath the outputfilepath to set
		 */
		public void setOutputfilepath(String outputfilepath) {
			this.outputfilepath = outputfilepath;
		}
		
		/**
		 * Writes the config to a json file
		 * @param filepath
		 */
		public void writeConfigFile(String filepath) {
			JSONObject files = new JSONObject();
			files.put("inputFile", inputfilepath);
			files.put("outputFile", outputfilepath);
			files.put("encoding", encoding);
			
			//Add to the outter  Congig Array
	    	JSONArray configArray = new JSONArray();
	    	configArray.add(files);
	    	
	    	/*
	    	// Rule array
	    	JSONArray ruleArray = new JSONArray();
	    	for (Rule rl: rules) {
	    		JSONObject rljo = new JSONObject();
	    		rljo.put("rule", rl.toString());
	    		ruleArray.add(rljo);
	    	}
	    	*/
	    	//Add to the outter  Congig Array
	    	//configArray.add(ruleArray);
	    	
	    	//Write JSON file
	    	try (FileWriter file = new FileWriter(filepath)) {
	            file.write(configArray.toJSONString());
	            file.flush();

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		} // write
		
		AConfig(){
			
		}
		
		public AConfig(String filepath){
			readConfigFile(filepath);
		}
		
		private void readConfigFile(String filepath) {
			JSONParser jsonParser = new JSONParser();
			try (FileReader reader = new FileReader(filepath))
			{
	            Object obj = jsonParser.parse(reader);
	            JSONArray configArray = (JSONArray) obj;
	           // System.out.println(configArray);
	            
	            // reading io files
	            JSONObject oj = (JSONObject) configArray.get(0);
	            inputfilepath   = (String) oj.get("inputFile");
		    	outputfilepath  = (String) oj.get("outputFile");
		    	encoding  		= (String) oj.get("encoding");
	            
		    	/*
	            //reading rules
		    	JSONArray rArrayj = (JSONArray) configArray.get(1);
		    	for (Object ro: rArrayj) {
		    		JSONObject roj = (JSONObject)ro;
		    		String  rlstr = (String) roj.get("rule");
		    		//rules.add(new Rule(rlstr));
		    	}
		    	*/
		    	
		
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (ParseException e) {
	            e.printStackTrace();
	        }
		}
}


class AConfigClient{
    @SuppressWarnings("unchecked")
	public static void main( String[] args ) {
    	
    	
    	// A. CREATING A CONFIG FILE PROGRAMMATICALLY
    	AConfig ac1 = new AConfig();
    	ac1.setInputfilepath( "datafiles/LALA.tsv");
    	ac1.setOutputfilepath("datafiles/LALATRANSFORMED.txt");
    	ac1.writeConfigFile("Resources/configFiles/tmpConfig.json");
    	
    		
    	// B. READING A CONFIG FILE PROGRAMMATICALLY
    	AConfig ac2 = new AConfig("./Resources/configFiles/tmpConfig.json");
    	System.out.println(ac2.getInputfilepath() + " "  + ac2.getOutputfilepath());
    	//System.out.println(ac2.getRules());   	
    	
    	System.out.println();
    	System.out.println(ac2);
    }
	
}


