package Ctransformers;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages shared URI instances to minimize memory usage
 */
public class URIFactory {
    private static final ConcurrentHashMap<String, String> URI_POOL = new ConcurrentHashMap<>();
    private URIFactory() {}

    /**
     * Returns a shared instance of the URI string
     */
    public static String getURI(String uri) {
    	
    	if (uri != null && !uri.isEmpty()) {
    			return URI_POOL.computeIfAbsent(uri, k -> k);
    	} else {
    		//System.out.println("Problem with a URI!");
    		//new IllegalArgumentException("null parameter");
    	}
    	return "EMPTYURI";
    	
    	//if (uri==null) {
    	//	new IllegalArgumentException("null parameter");
    	//}
        //return URI_POOL.computeIfAbsent(uri, k -> k);
    }

    /**
     * Returns the number of unique URIs stored
     */
    public static int getPoolSize() {
        return URI_POOL.size();
    }
}