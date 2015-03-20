/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.dataproxy;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.behaviouralmodel.lov.bo.JavaClassUtils;
import it.eng.spagobi.tools.dataset.bo.IJavaClassDataSet;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JavaClassDataProxy  extends AbstractDataProxy {

	String className;

	private static transient Logger logger = Logger.getLogger(JavaClassDataProxy.class);


	public JavaClassDataProxy() {
		super();
	}

	public JavaClassDataProxy(String className) {
		setClassName( className );
	}

	public IDataStore load(String statement, IDataReader dataReader) throws EMFUserError {
		throw new UnsupportedOperationException("metothd load not yet implemented");
	}

	public IDataStore load(IDataReader dataReader) {
		String result = null;				
		IDataStore dataStore = null;
		IJavaClassDataSet javaClass;
		try {
			javaClass = (IJavaClassDataSet) Class.forName( className ).newInstance();

			if(javaClass==null){
				logger.debug("java class not found");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 9217);
			}

			boolean checkProfileAttribute=checkProfileAttribute(javaClass, profile);
			
			if(!checkProfileAttribute){
				logger.debug("error in solving profile Attributes required");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 9213);
			}
			
			result = javaClass.getValues( getProfile(), getParameters());
			result = result.trim();
//			boolean toconvert = JavaClassUtils.checkSintax(result);
//			// check if the result must be converted into the right xml sintax
//			if(toconvert) { 
//				result = JavaClassUtils.convertResult(result);
//			}
			dataStore = dataReader.read(result);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to load dataset", t);
		}

		return dataStore;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}



	public boolean checkProfileAttribute(IJavaClassDataSet classData, Map profile){
		List profileAttributeNeeded=classData.getNamesOfProfileAttributeRequired();
		if(profileAttributeNeeded==null) return true;
		for (Iterator iterator = profileAttributeNeeded.iterator(); iterator.hasNext();) {
			String attribute = (String) iterator.next();
			String value=null;
			value=(String)profile.get(attribute);
			if(value==null)return false;
		}		
		return true;
	}







}
