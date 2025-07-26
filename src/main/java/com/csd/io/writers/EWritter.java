/**
 * 
 */
package com.csd.io.writers;

import java.io.FileWriter;

/**
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 * Class with various utility methods for writting files
 *
 */
public class EWritter {
	  private FileWriter fr; 	
	  
	  private String prefixHeaders = "";
	  //private String prefixHeaders = "Row bits follow";
			  
	  /*		  
	  private String prefixHeaders = 
			"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n" + 
	  		"@prefix yannis: <www.ics.forth.gr/~tzitzik/rdf> .\r\n" +
	  		"@prefix example: <www.ics.forth.gr/~tzitzik/rdf/example> .\r\n" +		
	  		"@prefix xml:   <http://www.w3.org/XML/1998/namespace> .\r\n" + 
	  		"@prefix skos:  <http://www.w3.org/2004/02/skos/core#> .\r\n" + 
	  		"@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\r\n" + 
	  		"@prefix crm:   <http://www.cidoc-crm.org/cidoc-crm/> .\r\n" + 
	  		"";
	  */
	  
	  
	  public void write(String str) {
		  try {
			  fr.write(str);
			  //fr.write("\n");
			  //fr.write("1st column, second column" + "\n");
			  //fr.write("2nd row" + str+ "\n");
		  } catch (Exception e) {
			  System.out.println(e);
		  }
	  }
	  
	  
	  public void writePrefix() {
		  write(prefixHeaders);
	  }
	  

	  public void close() {
		  try {
			  fr.close();
		  } catch (Exception e) {
			  System.out.println(e);
		  }
	  }
	  public EWritter(String fileName) {
		  try {
		  fr = new FileWriter(fileName,false); // overwrite file if it exists

		  } catch (Exception e) {
			  System.out.println(e);
		  }
	  }
}


class EWritterClient {
	public static void main(String[] lala) {
		EWritter mw = new EWritter("Resources/datafiles/todelete.txt");
		mw.writePrefix();
		mw.write("row1cell1, row1cell2");
		mw.write("row2cell1, row2cell2");
		mw.close();
	}
}