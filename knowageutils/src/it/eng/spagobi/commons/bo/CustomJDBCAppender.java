/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.bo;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.apache.log4j.jdbc.JDBCAppender;


/**
 * 
 * 
 * @author Chiara Chiarelli
 */


public class CustomJDBCAppender extends JDBCAppender {  
	
	private static Logger logger = Logger.getLogger("CustomJDBCAppender");  

	     public CustomJDBCAppender(Connection con) {
			super();
			super.connection = con;
		}


		public Connection getConnection(){
	    	 return super.connection;
	     }

	 }  