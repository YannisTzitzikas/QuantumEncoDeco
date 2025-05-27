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
		
		Pipeline ep2 = new Pipeline("Resources/configFiles/configAristotle.json");
		ep2.process();
        		
		// Pipeline ep3 = new Pipeline("Resources/configFiles/configAristotle_decode.json");
		// ep3.process();

		System.out.print("EncoDeco: bye bye");

	}

}
