/**
 * 
 */
package Zclients;

import Aconfig.AConfig;
import Ppipeline.Pipeline;

/**
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 *
 */
public class EncodDecoExample {

	
	public static void main(String[] args) {
		System.out.print("EncoDeco v.0.1");
		/*
		Pipeline ep = new Pipeline("Resources/configFiles/currentConfig.json");
		ep.transformDummy();
		//ep.transform();
		*/
		
		Pipeline ep2 = new Pipeline("Resources/configFiles/configAristotle.json");
		ep2.transformDummy();
		
		
		
		System.out.print("EncoDeco: bye bye");

	}

}
