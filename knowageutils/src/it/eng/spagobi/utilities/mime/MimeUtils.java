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
package it.eng.spagobi.utilities.mime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.file.FileUtils;



/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class MimeUtils {
	
	private static Map mimeTypes;
	private static Map fileExtensions;
	
	
	public static final String UNKNOWN_MIME_TYPE="application/x-unknown-mime-type";
	
	public static boolean isValidMimeType(String mimeType) {
		return fileExtensions.containsKey( mimeType );
	}
	
	public static boolean isValidFileExtension(String fileExtension) {
		return mimeTypes.containsKey( fileExtension );
	}
	
	public static String getFileExtension(String mimeType) {
		if(mimeType!= null){
			return (String)fileExtensions.get( mimeType );
		}else{
			return "";
		}
	}
	
	public static String getMimeType(File file) {
		return getMimeType( file.getName() );
	}
	
	/**
	 * Determin the mime type of a file from its name using
	 */ 
	public static String getMimeType(String fileName) {
		String mimeType = null;
		if( StringUtilities.isEmpty( fileName ) ){
			fileName = "";
		}
		
		// Get the file extension
		String ext = FileUtils.getFileExtension( fileName );
		if( StringUtilities.isEmpty( ext ) ) {
			// we cannot tell from the file extension the correct mime type
			mimeType = UNKNOWN_MIME_TYPE;
		} else {
			// First try case sensitive
			mimeType = (String)mimeTypes.get(ext);
			if( StringUtilities.isEmpty( mimeType ) ) {
				//try again case insensitive (lower case)
				mimeType = (String)mimeTypes.get(ext.toLowerCase());
				if( StringUtilities.isEmpty( mimeType ) ) {
					// we cannot tell from the file extension the correct mime type
					mimeType = UNKNOWN_MIME_TYPE;;
				}
			}
		}
		
		return mimeType;
	}
	
	
	// Initialise mimeTypes map in preperation for mime type detection
	static {
		mimeTypes = new Properties();
		// Load the file extension mappings from the internal property file and then 
		// from the custom property file if it can be found on the classpath
		try {
			// Load the default supplied mime types
			((Properties)mimeTypes).load(MimeUtils.class.getClassLoader().getResourceAsStream("it/eng/spagobi/utilities/mime/mime-types.properties"));
			// Load any classpath provided mime types that either extend or override the default mime types
			InputStream is =  MimeUtils.class.getClassLoader(). getResourceAsStream("mime-types.properties");
			if(is != null) {
				try {
					Properties props = new Properties();
					props.load(is);
					if(props.size() > 0) {
						mimeTypes.putAll(props);
					}
				}finally {
					if(is != null) {
						is.close();
					}
				} 
			} 
		} catch (IOException ignore) {}
	}	
	
	// Initialise fileExtensions map in preperation for file extension detection
	static {
		fileExtensions = new Properties();
		Iterator it = mimeTypes.keySet().iterator();
		while( it.hasNext() ) {
			String fileExtension = (String)it.next();
			String mimeType = (String)mimeTypes.get( fileExtension );
			fileExtensions.put(mimeType, fileExtension);
		}
	}	
}
