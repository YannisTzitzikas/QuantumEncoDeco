/**
 * 
 */
package transformers;

/**
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 *
 */
public class Triple {
	String s;
	String p;
	String o;
	public String toString() {
		return s + "," + p + "," + o +".";
	}
	public Triple(String s, String p, String o) {
		this.s=s; this.p=p; this.o=o;
	}
}
