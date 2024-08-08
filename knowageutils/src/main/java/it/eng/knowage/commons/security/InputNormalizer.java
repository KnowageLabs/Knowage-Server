package it.eng.knowage.commons.security;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;

public class InputNormalizer {
	public static String normalizeURL(String url) throws MalformedURLException, URISyntaxException {
		url = Normalizer.normalize(url, Form.NFKC);
		url = new URI(url).normalize().toURL().toString();
        return url;
	}
	
	public static String normalizePath(String path) {
		path = Normalizer.normalize(path, Form.NFKC);
		Path iPath = Paths.get(path).normalize();
		return iPath.toString();
	}
	
	
	

	

}
