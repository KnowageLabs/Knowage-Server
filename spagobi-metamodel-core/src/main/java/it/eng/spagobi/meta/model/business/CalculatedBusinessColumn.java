/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.business;

import it.eng.spagobi.commons.exception.SpagoBIPluginException;

import java.util.List;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Calculated Business Column</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see it.eng.spagobi.meta.model.business.BusinessModelPackage#getCalculatedBusinessColumn()
 * @model
 * @generated
 */
public interface CalculatedBusinessColumn extends BusinessColumn {
	
	
	List<SimpleBusinessColumn> getReferencedColumns() throws SpagoBIPluginException;
} // CalculatedBusinessColumn
