package it.eng.spagobi.mapcatalogue.service;

import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;

public class LayerServices {
	private SingletonConfig configSingleton;
	private String path;
	private String resourcePath;


	LayerServices(){
		setPath();

	}

	public void setPath(){
		configSingleton = SingletonConfig.getInstance();
		path  = configSingleton.getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
		resourcePath= SpagoBIUtilities.readJndiResource(path);
	}

	public String getResourcePath(byte[] data){
		System.out.println(resourcePath);
		return resourcePath;
	}

	public  byte[] getFile(byte[] data){
		//15 perchè 15 sono i bye prima dell'inizi del doc... nel caso di txt
		byte[] result = Arrays.copyOfRange(data, 15, data.length -1);
		System.out.println(result);
		System.out.println("a confronto"+result.length+" "+data.length);
		return result;

	}


	public byte[] decode64(byte[] data){
		byte[] result = new byte[0];
		try{
			result = Base64.decodeBase64(data);
		}
		catch (Exception e) {

		}
		return result;
	}
}
