/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure.filter;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.properties.IModelProperties;
import it.eng.qbe.model.properties.SimpleModelProperties;
import it.eng.qbe.model.structure.IModelEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class QbeTreeOrderEntityFilter.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class QbeTreeOrderEntityByLabelFilter extends ComposableQbeTreeEntityFilter{

	private Locale locale;
	
	/**
	 * Instantiates a new qbe tree order entity filter.
	 */
	public QbeTreeOrderEntityByLabelFilter() {
		super();
	}
	
	public QbeTreeOrderEntityByLabelFilter(IQbeTreeEntityFilter parentFilter, Locale locale) {
		super(parentFilter);
		this.setLocale( locale );
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.qbe.tree.filter.ComposableQbeTreeEntityFilter#filter(it.eng.qbe.model.IDataMartModel, java.util.List)
	 */
	public List filter(IDataSource dataSource, List entities) {
		List list = null;
		
		ComparableEntitiesList comparableEntities = new ComparableEntitiesList(dataSource, locale);
		comparableEntities.addEntities( entities );
		list = comparableEntities.getEntitiesOrderedByLabel();
		
		return list;
	}
	
	/**
	 * The Class ComparableEntitiesList.
	 */
	private class ComparableEntitiesList {

		/** The list. */
		private List list;
		
		/** The datamart model. */
		private IDataSource dataSource;
		private IModelProperties datamartLabels;
		
		/**
		 * Instantiates a new comparable entities list.
		 * 
		 * @param dataSource the datamart model
		 */
		ComparableEntitiesList(IDataSource dataSource, Locale locale) {
			
			list = new ArrayList();
			this.dataSource = dataSource;
			//setDatamartLabels( QbeCacheManager.getInstance().getLabels( dataSource , locale ) );
			setDatamartLabels( dataSource.getModelI18NProperties(locale) );
			if( getDatamartLabels() == null) {
				setDatamartLabels( new SimpleModelProperties() );
			}
		}
		
		/**
		 * Adds the entity.
		 * 
		 * @param entity the entity
		 */
		void addEntity(IModelEntity entity) {
			String label = geEntityLabel( entity );	
			EntityWrapper field = new EntityWrapper(label, entity);
			list.add(field);
		}
		
		private String geEntityLabel(IModelEntity entity) {
			String label;
			label = getDatamartLabels().getProperty(entity, "label");
			return label==null? entity.getName(): label;
		}
		
		
		
		/**
		 * Adds the entities.
		 * 
		 * @param entities the entities
		 */
		void addEntities(Set entities) {
			if (entities != null && entities.size() > 0) {
				Iterator it = entities.iterator();
				while (it.hasNext()) {
					IModelEntity relation = (IModelEntity) it.next();
					addEntity(relation);
				}
			}
		}
		
		/**
		 * Adds the entities.
		 * 
		 * @param relations the relations
		 */
		void addEntities(List relations) {
			if (relations != null && relations.size() > 0) {
				Iterator it = relations.iterator();
				while (it.hasNext()) {
					IModelEntity entity = (IModelEntity) it.next();
					addEntity(entity);
				}
			}
		}
		
		/**
		 * Gets the entities ordered by label.
		 * 
		 * @return the entities ordered by label
		 */
		List getEntitiesOrderedByLabel () {
			Collections.sort(list);
			List toReturn = new ArrayList();
			Iterator it = list.iterator();
			while (it.hasNext()) {
				EntityWrapper field = (EntityWrapper) it.next();
				toReturn.add(field.getEntity());
			}
			return toReturn;
		}

		private IModelProperties getDatamartLabels() {
			return datamartLabels;
		}

		private void setDatamartLabels(IModelProperties datamartLabels) {
			this.datamartLabels = datamartLabels;
		}
		
	}
	
	
	/**
	 * The Class EntityWrapper.
	 */
	private class EntityWrapper implements Comparable {
		
		/** The entity. */
		private IModelEntity entity;
		
		/** The label. */
		private String label;
		
		/**
		 * Instantiates a new entity wrapper.
		 * 
		 * @param label the label
		 * @param entity the entity
		 */
		EntityWrapper (String label, IModelEntity entity) {
			this.entity = entity;
			this.label = label;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object o) {
			if (o == null) throw new NullPointerException();
			if (!(o instanceof EntityWrapper)) throw new ClassCastException();
			EntityWrapper anotherEntity = (EntityWrapper) o;
			return this.getLabel().compareTo(anotherEntity.getLabel());
		}
		
		/**
		 * Gets the entity.
		 * 
		 * @return the entity
		 */
		public IModelEntity getEntity() {
			return entity;
		}
		
		/**
		 * Sets the entity.
		 * 
		 * @param entity the new entity
		 */
		public void setEntity(IModelEntity entity) {
			this.entity = entity;
		}
		
		/**
		 * Gets the label.
		 * 
		 * @return the label
		 */
		public String getLabel() {
			return label;
		}
		
		/**
		 * Sets the label.
		 * 
		 * @param label the new label
		 */
		public void setLabel(String label) {
			this.label = label;
		}
		
	}


	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}
}
