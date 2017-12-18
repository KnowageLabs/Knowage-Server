/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.engines.whatif.export;

/**
 * Container for export properties
 */
import javax.print.attribute.standard.OrientationRequested;

import org.apache.log4j.Logger;

public class ExportConfig {
	private String fontFamily;
	private Integer fontSize;
	private OrientationRequested orientation;
	
	public static transient Logger logger = Logger.getLogger(ExportConfig.class);

	public ExportConfig(String fontFamily, String fontSize,
			String orientation) {
		super();
		this.fontFamily = fontFamily;
		if(fontSize!=null){
			try {
				this.fontSize = new Integer(fontSize);
			} catch (Exception e) {
				this.fontSize = null;
				logger.error("Invalid export fontSize. It has to be a number, but fount ["+fontSize+"]");
			}
		}
		
		if(orientation.equals("PORTRAIT")){
			this.orientation =OrientationRequested.PORTRAIT;
		}
		
		if(orientation.equals("LANDSCAPE")){
			this.orientation =OrientationRequested.LANDSCAPE;
		}
		
		if(orientation.equals("REVERSE_LANDSCAPE")){
			this.orientation =OrientationRequested.REVERSE_LANDSCAPE;	
		}
		
		if(orientation.equals("REVERSE_PORTRAIT")){
			this.orientation =OrientationRequested.REVERSE_PORTRAIT; 
		}
	}
	
	public String getFontFamily() {
		return fontFamily;
	}

	public Integer getFontSize() {
		return fontSize;
	}

	public OrientationRequested getOrientation() {
		return orientation;
	}


}






