package it.eng.spagobi.commons.robobraillerconverter.restclient;



public class HtmlTxtRobobrailleController extends AbstractRoboBrailleController  {
	
	
	
	public HtmlTxtRobobrailleController() {
		super.controllerPath = "/api/HTMLToText";
		super.fileExtension = ".txt";
	} 
}
