/**
 * 
 */
package Zclients;

import Ppipeline.Pipeline;

/**
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 *
 */
public class EncodDecoExample {

	
	public static void main(String[] args) {
		System.out.print("EncoDeco v.0.1");
		
		String[]   configFiles = {
				//"Resources/configFiles/CIDOC-CRMencode.json",
				//"Resources/configFiles/CIDOC-CRMdecode.json",
				//"Resources/configFiles/DBpediaEncode.json",
				//"Resources/configFiles/DBpediaDecode.json",
				//"Resources/configFiles/ChebiEncode.json",
				//"Resources/configFiles/ChebiDecode.json"
				"Resources/configFiles/GRSFEncode.json",
				"Resources/configFiles/GRSFDecode.json"
		};
		
		for (String cf: configFiles) {
			  (new Pipeline(cf)).process();
		}
			
		System.out.println("Bye bye");
	}

}
