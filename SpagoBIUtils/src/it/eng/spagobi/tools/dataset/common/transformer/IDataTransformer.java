/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.transformer;

import java.util.List;

/**
 * @author Angelo Bernabei
 *         angelo.bernabei@eng.it
 */
public interface IDataTransformer {
	
    List transformData(List records);
    
    /**
     * IDataTransformer is a general interface for transformer. this method is specific of one kind of
     * transformer so it must be removed from here
     * 
     * @deprectade
     */
    List transformData(List records, String pivotColumn,  String pivotRow, String pivotValue);
}
