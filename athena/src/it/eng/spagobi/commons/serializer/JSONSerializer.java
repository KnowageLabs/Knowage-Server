/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.DocumentMetadataProperty;
import it.eng.spagobi.analiticalmodel.document.bo.ObjNote;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.bo.Viewpoint;
import it.eng.spagobi.analiticalmodel.execution.service.GetParametersForExecutionAction;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.hotlink.rememberme.bo.RememberMe;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarm;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarmContact;
import it.eng.spagobi.kpi.config.bo.Kpi;
import it.eng.spagobi.kpi.config.bo.Periodicity;
import it.eng.spagobi.kpi.config.metadata.SbiKpiComments;
import it.eng.spagobi.kpi.goal.metadata.bo.Goal;
import it.eng.spagobi.kpi.goal.metadata.bo.GoalNode;
import it.eng.spagobi.kpi.model.bo.Model;
import it.eng.spagobi.kpi.model.bo.ModelExtended;
import it.eng.spagobi.kpi.model.bo.ModelInstance;
import it.eng.spagobi.kpi.model.bo.ModelResourcesExtended;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnit;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrant;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitHierarchy;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitNodeWithGrant;
import it.eng.spagobi.kpi.threshold.bo.Threshold;
import it.eng.spagobi.kpi.threshold.bo.ThresholdValue;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bo.UserBO;
import it.eng.spagobi.tools.catalogue.bo.Artifact;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.dataset.bo.CkanDataSet;
import it.eng.spagobi.tools.dataset.bo.CustomDataSet;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCHBaseDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCHiveDataSet;
import it.eng.spagobi.tools.dataset.bo.ScriptDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.bo.WebServiceDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.bo.DataSourceModel;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.udp.metadata.SbiUdp;
import it.eng.spagobi.tools.udp.metadata.SbiUdpValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JSONSerializer implements Serializer {

	Map<Class, Serializer> mappings;

	public JSONSerializer() {
		mappings = new HashMap();

		mappings.put(DataSource.class, new DataSourceJSONSerializer());
		mappings.put(DataSourceModel.class, new DataSourceJSONSerializer());
		mappings.put(Domain.class, new DomainJSONSerializer());
		mappings.put(Config.class, new ConfigJSONSerializer());
		mappings.put(Engine.class, new EngineJSONSerializer());
		mappings.put(Role.class, new RoleJSONSerializer());
		mappings.put(BIObject.class, new DocumentsJSONSerializer());
		mappings.put(LowFunctionality.class, new FoldersJSONSerializer());
		mappings.put(SubObject.class, new SubObjectsJSONSerializer());
		mappings.put(Viewpoint.class, new ViewpointJSONSerializer());
		mappings.put(Snapshot.class, new SnapshotJSONSerializer());
		mappings.put(RememberMe.class, new FavouritesJSONSerializer());
		mappings.put(DataStore.class, new DataStoreJSONSerializer());
		mappings.put(ObjNote.class, new ObjectNotesJSONSerializer());
		mappings.put(DocumentMetadataProperty.class, new MetadataJSONSerializer());
		mappings.put(ObjMetadata.class, new ShortMetadataJSONSerializer());
		mappings.put(SbiUser.class, new SbiUserJSONSerializer());
		mappings.put(UserBO.class, new UserJSONSerializer());
		mappings.put(SbiAttribute.class, new AttributeJSONSerializer());
		mappings.put(SbiAlarm.class, new AlarmJSONSerializer());
		mappings.put(SbiAlarmContact.class, new AlarmContactJSONSerializer());
		mappings.put(ThresholdValue.class, new ThresholdValueJSONSerializer());
		mappings.put(SbiDataSet.class, new SbiDataSetJSONSerializer());
		mappings.put(VersionedDataSet.class, new DataSetJSONSerializer());
		mappings.put(CustomDataSet.class, new DataSetJSONSerializer());
		mappings.put(JDBCDataSet.class, new DataSetJSONSerializer());
		mappings.put(JDBCHBaseDataSet.class, new DataSetJSONSerializer());
		mappings.put(JDBCHiveDataSet.class, new DataSetJSONSerializer());
		mappings.put(ScriptDataSet.class, new DataSetJSONSerializer());
		mappings.put(WebServiceDataSet.class, new DataSetJSONSerializer());
		mappings.put(FileDataSet.class, new DataSetJSONSerializer());
		mappings.put(CkanDataSet.class, new DataSetJSONSerializer());

		mappings.put(RoleMetaModelCategory.class, new RoleMetaModelCategoryJSONSerializer());

		mappings.put(Resource.class, new ResourceJSONSerializer());
		mappings.put(Threshold.class, new ThresholdJSONSerializer());
		mappings.put(Kpi.class, new KpiJSONSerializer());
		mappings.put(Periodicity.class, new PeriodicityJSONSerializer());

		mappings.put(Model.class, new ModelNodeJSONSerializer());
		mappings.put(ModelInstance.class, new ModelInstanceNodeJSONSerializer());
		mappings.put(ModelResourcesExtended.class, new ModelResourcesExtendedJSONSerializer());
		mappings.put(ModelExtended.class, new ModelExtendedJSONSerializer());

		mappings.put(GetParametersForExecutionAction.ParameterForExecution.class, new ParameterForExecutionJSONSerializer());
		mappings.put(SbiUdp.class, new UdpJSONSerializer());
		mappings.put(SbiUdpValue.class, new UdpValueJSONSerializer());

		mappings.put(OrganizationalUnitGrant.class, new OrganizationalUnitGrantJSONSerializer());
		mappings.put(OrganizationalUnit.class, new OrganizationalUnitJSONSerializer());
		mappings.put(OrganizationalUnitHierarchy.class, new OrganizationalUnitHierarchyJSONSerializer());
		mappings.put(OrganizationalUnitNodeWithGrant.class, new OrganizationalUnitNodeWithGrantJSONSerializer());

		mappings.put(GoalNode.class, new GoalNodeJSONSerializer());
		mappings.put(Goal.class, new GoalJSONSerializer());
		mappings.put(SbiKpiComments.class, new SbiKpiCommentSerializer());
		mappings.put(MetaModel.class, new MetaModelJSONSerializer());
		mappings.put(Artifact.class, new ArtifactJSONSerializer());
		mappings.put(Content.class, new ContentJSONSerializer());
		mappings.put(SbiTenant.class, new TenantJSONSerializer());

		mappings.put(Job.class, new JobJSONSerializer());
		mappings.put(Trigger.class, new TriggerJSONSerializer());

		mappings.put(Parameter.class, new ParametersJSONSerialize());
		mappings.put(ParameterUse.class, new ParametersUseJSONSerialize());
		mappings.put(ModalitiesValue.class, new ModalitiesValuesJSONSerializer());
		
	}

	public Object serialize(Object o, Locale locale) throws SerializationException {
		Object result = null;

		try {

			if (o instanceof Collection) {
				JSONArray r = new JSONArray();

				Collection c = (Collection) o;

				Iterator it = c.iterator();

				while (it.hasNext()) {
					// For LOV objects (ModalitiesValue) this will put JSONObjects into JSONArray
					r.put(serialize(it.next(), locale));
				}
				result = r;
			} else {
				if (!mappings.containsKey(o.getClass())) {
					throw new SerializationException("JSONSerializer is unable to serialize object of type: " + o.getClass().getName());
				}

				Serializer serializer = mappings.get(o.getClass());
				result = serializer.serialize(o, locale);
			}

		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {

		}
		return result;
	}

}
