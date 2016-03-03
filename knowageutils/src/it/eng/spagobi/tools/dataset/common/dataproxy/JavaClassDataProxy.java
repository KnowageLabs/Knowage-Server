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
