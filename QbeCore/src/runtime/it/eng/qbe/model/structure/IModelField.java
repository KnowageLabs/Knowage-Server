/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.qbe.model.structure;
import it.eng.spagobi.utilities.objects.Couple;

public interface IModelField extends IModelNode {

		public String getUniqueName();
		public Couple getQueryName();	
		public String getType();
		public void setType(String type);
		public int getLength();
		public void setLength(int length);
		public int getPrecision() ;
		public void setPrecision(int precision);
		public boolean isKey() ;
		public void setKey(boolean key);
		public IModelField clone(IModelEntity newParent);	
}
