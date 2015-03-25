/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.lov.handlers;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LovManager {
    
	/**
	 * Returns all the names of the columns returned by the lov.
	 * 
	 * @param lov the lov to analize
	 * 
	 * @return List of the columns name (the element of the list are Strings)
	 */
	public List getAllColumnsNames(ModalitiesValue lov) {
		List names = new ArrayList();
		try{
			String lovProvider = lov.getLovProvider();
			ILovDetail lovProvDet = LovDetailFactory.getLovFromXML(lovProvider);
			List viscols = lovProvDet.getVisibleColumnNames();
			List inviscols = lovProvDet.getInvisibleColumnNames();
			names.addAll(viscols);
			names.addAll(inviscols);
		} catch (Exception e) {
			names = new ArrayList();
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
					            "getAllColumnsNames", "Error while recovering column names " + e);
		}
		return names;
	}
	
	/**
	 * Gets the labels of documents which use lov.
	 * 
	 * @param lov the lov
	 * 
	 * @return the labels of documents which use lov
	 */
	public static List getLabelsOfDocumentsWhichUseLov(ModalitiesValue lov) {
		List docLabels = new ArrayList();
		try{
			IParameterUseDAO parusedao = DAOFactory.getParameterUseDAO();
			IBIObjectParameterDAO biobjpardao = DAOFactory.getBIObjectParameterDAO();
			List paruses = parusedao.getParameterUsesAssociatedToLov(lov.getId());
			Iterator parusesIt = paruses.iterator();
			while (parusesIt.hasNext()) {
				ParameterUse aParuse = (ParameterUse) parusesIt.next();
				List temp = biobjpardao.getDocumentLabelsListUsingParameter(aParuse.getId());
				docLabels.addAll(temp);
			}
		} catch (Exception e) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, LovManager.class.getName(), 
					            "getLabelsOfDocumentsWhichUseLov", "Error while recovering document labels", e);
			
		}
		return docLabels;
	}
	
	
}
