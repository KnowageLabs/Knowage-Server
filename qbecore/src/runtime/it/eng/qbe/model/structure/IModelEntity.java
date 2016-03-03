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
package it.eng.qbe.model.structure;

import java.util.Iterator;
import java.util.List;

public interface IModelEntity extends IModelNode {

		public IModelEntity getRoot();
		public String getType();
		public String getUniqueType();
		public String getRole();

		public List<IModelField> getAllFields();
		public IModelField getField(String fieldUniqueName);
		public IModelField getFieldByName(String fieldName);
		public List<IModelField> getFieldsByType(boolean isKey);
		public List<IModelField> getKeyFields() ;
		public Iterator<IModelField> getKeyFieldIterator();
		public List<IModelField> getNormalFields();
		public Iterator<IModelField> getNormalFieldIterator();
		public List<ModelCalculatedField> getCalculatedFields();

		public IModelEntity getSubEntity(String entityUniqueName);
		public List<IModelEntity> getSubEntities() ;
		public List<IModelEntity> getAllSubEntities() ;
		public List<IModelEntity> getAllSubEntities(String entityName);



		public List<IModelField> getAllFieldOccurencesOnSubEntity(String entityName, String fieldName);
		public String toString();
		public String getPath();
		public int getDepth();

		public IModelField addNormalField(String fieldName);
		public IModelField addKeyField(String fieldName);
		public void addField(IModelField field);

		public void addCalculatedField(ModelCalculatedField calculatedField);
		public void deleteCalculatedField(String fieldName);

		public HierarchicalDimensionField getHierarchicalDimensionByEntity(String entity);
		public void addHierarchicalDimension(HierarchicalDimensionField hierarchicalDimensionField);

		public IModelEntity addSubEntity(String subEntityName, String subEntityRole, String subEntityType) ;
		public void addSubEntity(IModelEntity entity) ;

		public void setPath(String path);

		public void setRole(String role);
		public void setRoot(IModelEntity root);
		public void setType(String type);

		public IModelEntity clone(IModelEntity newParent, String parentEntity);
}
