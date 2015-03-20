/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure;

import java.util.List;
import java.util.Set;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public interface IModelViewEntityDescriptor {
	String getName();
	String getType();
	Set<String> getInnerEntityUniqueNames();
	List<IModelViewJoinDescriptor> getJoinDescriptors();
	List<IModelViewRelationshipDescriptor> getRelationshipDescriptors();
	public List<IModelViewRelationshipDescriptor> getRelationshipToViewsDescriptors();
	
	public interface IModelViewJoinDescriptor {
		public String getSourceEntityUniqueName();
		public String getDestinationEntityUniqueName();
		public List<String> getSourceColumns();
		public List<String> getDestinationColumns();
	}
	
	public interface IModelViewRelationshipDescriptor{
		public String getSourceEntityUniqueName();
		public String getDestinationEntityUniqueName();
		public List<String> getSourceColumns();
		public List<String> getDestinationColumns();
		public boolean isOutbound();
		public boolean isSourceEntityView();
		public boolean isDestinationEntityView();
	}
}
