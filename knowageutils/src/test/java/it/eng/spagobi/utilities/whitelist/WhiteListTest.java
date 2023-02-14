package it.eng.spagobi.utilities.whitelist;

import java.util.Arrays;
import java.util.List;

public class WhiteListTest implements IWhiteList {

	@Override
	public List<String> getRelativePaths() {
		return Arrays.asList("/knowage/themes/", "/knowage/icons/", "/knowage/dashboards/");
	}

	@Override
	public List<String> getExternalServices() {
		return Arrays.asList("https://cdn.jsdelivr.net", "https://www.youtube.com", "https://vimeo.com", "https://www.flickr.com", "https://fonts.googleapis.com", "https://code.highcharts.com");
	}

}
