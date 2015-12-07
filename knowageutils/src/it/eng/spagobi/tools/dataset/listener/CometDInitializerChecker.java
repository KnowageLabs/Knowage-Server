package it.eng.spagobi.tools.dataset.listener;

/**
 * This class is necessary to check if Cometd servlet was initialized without using the CometD jar dependencies
 */
public class CometDInitializerChecker {

	private static boolean cometdInitialized;
	
	private CometDInitializerChecker() {
		
	}

	public synchronized static void setCometdInitialized() {
		cometdInitialized = true;
	}
	
	public synchronized static boolean isCometdInitialized() {
		return cometdInitialized;
	}
	

}
