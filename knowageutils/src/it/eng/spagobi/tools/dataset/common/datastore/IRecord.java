/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.datastore;

import java.util.List;

/**
 * @author Angelo Bernabei
 *         angelo.bernabei@eng.it
 */
public interface IRecord {

    IField getFieldAt(int fieldIndex);
    
    public void appendField(IField field) ;
    public void insertField(int fieldIndex, IField field) ;
    
    public List<IField> getFields();
	public void setFields(List<IField> fields);
	
	public IDataStore getDataStore();
    
}
