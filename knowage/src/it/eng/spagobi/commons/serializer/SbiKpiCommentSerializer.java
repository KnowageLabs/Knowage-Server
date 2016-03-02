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
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.analiticalmodel.document.bo.ObjNote;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.kpi.config.metadata.SbiKpiComments;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONObject;


public class SbiKpiCommentSerializer implements Serializer {
	private static Logger logger = Logger.getLogger(SbiKpiCommentSerializer.class);
	
	public static final String ID = "id";
	public static final String BIN_ID = "binId";
	public static final String OWNER = "owner";
	public static final String CREATION_DATE = "creationDate";
	public static final String LAST_MODIFICATION_DATE = "lastModificationDate";
	public static final String COMMENT = "comment";
	
	public Object serialize(Object o, Locale locale)
			throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof SbiKpiComments) ) {
			throw new SerializationException("SbiKpiCommentSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			// dates are sent to the client using a fixed format, the one returned by GeneralUtilities.getServerDateFormat()
			SimpleDateFormat dateFormat =  new SimpleDateFormat();
			dateFormat.applyPattern(GeneralUtilities.getServerTimeStampFormat());
			
			SbiKpiComments comment = (SbiKpiComments) o;
			result = new JSONObject();
			result.put(ID, comment.getKpiCommentId() );
			result.put(BIN_ID, comment.getSbiBinContents().getId() );
			result.put(OWNER, comment.getOwner() );
			Date creationDate = comment.getCreationDate();
			String creationDateStr = dateFormat.format(creationDate);
			result.put(CREATION_DATE, creationDateStr );
			Date lastChangeDate = comment.getLastChangeDate();
			String lastChangeDateStr = dateFormat.format(lastChangeDate);
			result.put(LAST_MODIFICATION_DATE, lastChangeDateStr );
			if(comment.getSbiBinContents() != null && comment.getSbiBinContents().getContent() != null){
				String contentStr = new String (comment.getSbiBinContents().getContent(), "UTF-8");
				result.putOpt(COMMENT, contentStr);
			}
			
			
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}

}
