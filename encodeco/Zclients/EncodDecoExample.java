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
		
		// Pipeline ep2 = new Pipeline("Resources/configFiles/newConfig.json");
		// ep2.process();
        		
		// Pipeline ep3 = new Pipeline("Resources/configFiles/newConfig_decode.json");
		// ep3.process();

		Pipeline ep4 = new Pipeline("Resources/configFiles/newConfig_V2.json");
		ep4.process();
        		
		Pipeline ep5 = new Pipeline("Resources/configFiles/newConfig_decode_V2.json");
		ep5.process();

            		
		Pipeline ep6 = new Pipeline("Resources/configFiles/newConfig_V3.json");
		ep6.process();

        
	}

}
