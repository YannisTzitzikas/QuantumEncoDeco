package com.ics.readers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;


/**
 * Class for various utilities for READING FILES  (plain and csv)
 * 
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 *
 */
public class BReader {
	private FileReader fr ;
	public BufferedReader bfr ;
	
	/**
	 * Constructor taking as input the path to the json config file
	 * @param filepath
	 */
    public BReader(String filepath) {
    	 try {
    	 fr = new FileReader(filepath);
    	 bfr = new BufferedReader(fr);
    	 } catch (Exception e) {
    		 System.out.println(e);
    	 }
    	 
     }
		
     public void close() {
    	 try {
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
     }
	
     
          
     /**
 	 * Reads  returns a Arraylist of arrays to Strings
 	 * @param csvFile
 	 * @return
 	 */
 	 public ArrayList readContentsAsArrayListOfArraysToStrings(String separators, boolean printAtConsole) {
 		 ArrayList resultAL = new ArrayList();
 		 String line = "";
         String cvsSplitBy = separators;
         try {
             while ((line = bfr.readLine()) != null) {
                 String[] values = line.split(cvsSplitBy);
                 //System.out.println("READ:" + line + " " + values);
                 resultAL.add(values);
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         
         if (printAtConsole==true) {
        	 for (Object row: resultAL) {
        		 String[] rowStrings = (String[]) row;
        		 Stream.of(rowStrings).forEach(e -> System.out.print("\t"+ e));
        		 System.out.println();
        	 }
         }
         
         return resultAL;
 	}
 	 
 	 
 	 
 	 
 	 public ArrayList<String> readContentsAsArraylistOfStrings() {
 		 ArrayList resultAL = new ArrayList();
 		 String line = "";
         try {
             while ((line = bfr.readLine()) != null) {
                 resultAL.add(line);
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
        
         return resultAL;
 	}
 	
 	/**
 	 * It takes as input an arraylist (of tables to strings) and a column number
 	 * and returns an arrayList with those strings that occur  in column i
 	 * @param a  ArrayList of arrays to strings
 	 * @param i  column number starting from 1
 	 * @return An arrayList with those strings that occur  in column i
 	 */
 	public  ArrayList getValueColumn(ArrayList a, int i) {
 		ArrayList ra = new ArrayList();
 		for (Object o: a) {
 			String[] sa = ((String[]) o);
 			ra.add(sa[i-1]);
 		}
 		return ra;
 	}

 	
	
     /**
      * Just for testing
      * @return
      */
	public String  read(){
		String line;
		
		try {
			while ((line = bfr.readLine()) != null) {   
				String[] tmp2 = line.split(","); // reading the tokens of a line
				for (String cell: tmp2)
						System.out.print(cell + " ");
				System.out.println();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		
		return null;
	}
}


class BReaderClient {
	public static void main(String[] lala) {
		BReader btest = new BReader("Resources/datafiles/test.csv");
		btest.read();
	}
}