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
package it.eng.spagobi.commons.serializer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import it.eng.qbe.dataset.FederatedDataSet;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.DocumentMetadataProperty;
import it.eng.spagobi.analiticalmodel.document.bo.ObjNote;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.bo.Viewpoint;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentDriverRuntime;
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
import it.eng.spagobi.tools.dataset.bo.JDBCHiveDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCImpalaDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCOrientDbDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCVerticaDataSet;
import it.eng.spagobi.tools.dataset.bo.RESTDataSet;
import it.eng.spagobi.tools.dataset.bo.SPARQLDataSet;
import it.eng.spagobi.tools.dataset.bo.ScriptDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.udp.metadata.SbiUdp;
import it.eng.spagobi.tools.udp.metadata.SbiUdpValue;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JSONSerializer implements Serializer {

	Map<Class, Serializer> mappings;
	private static Logger logger = Logger.getLogger(JSONSerializer.class);
	public JSONSerializer() {
		mappings = new HashMap();

		mappings.put(DataSource.class, new DataSourceJSONSerializer());
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
		mappings.put(SbiDataSet.class, new SbiDataSetJSONSerializer());
		mappings.put(VersionedDataSet.class, new DataSetJSONSerializer());
		mappings.put(CustomDataSet.class, new DataSetJSONSerializer());
		mappings.put(JDBCDataSet.class, new DataSetJSONSerializer());
		mappings.put(JDBCHiveDataSet.class, new DataSetJSONSerializer());
		mappings.put(ScriptDataSet.class, new DataSetJSONSerializer());
		mappings.put(FileDataSet.class, new DataSetJSONSerializer());
		mappings.put(CkanDataSet.class, new DataSetJSONSerializer());
		mappings.put(FederatedDataSet.class, new DataSetJSONSerializer());
		mappings.put(RESTDataSet.class, new DataSetJSONSerializer());
		mappings.put(SPARQLDataSet.class, new DataSetJSONSerializer());
		mappings.put(JDBCImpalaDataSet.class, new DataSetJSONSerializer());
		mappings.put(JDBCOrientDbDataSet.class, new DataSetJSONSerializer());
		mappings.put(JDBCVerticaDataSet.class, new DataSetJSONSerializer());

		mappings.put(RoleMetaModelCategory.class, new RoleMetaModelCategoryJSONSerializer());

		mappings.put(GetParametersForExecutionAction.ParameterForExecution.class, new ParameterForExecutionJSONSerializer());
		// mappings.put(ParameterForExecution.class, new ParameterForExecutionJSONSerializer());
		mappings.put(DocumentDriverRuntime.class, new DocumentParameterForExecutionJSONSerializer());
		mappings.put(SbiUdp.class, new UdpJSONSerializer());
		mappings.put(SbiUdpValue.class, new UdpValueJSONSerializer());

		mappings.put(MetaModel.class, new MetaModelJSONSerializer());
		mappings.put(Artifact.class, new ArtifactJSONSerializer());
		mappings.put(Content.class, new ContentJSONSerializer());
		mappings.put(SbiTenant.class, new TenantJSONSerializer());

		mappings.put(Job.class, new JobJSONSerializer());
		mappings.put(Trigger.class, new TriggerJSONSerializer());

		mappings.put(Parameter.class, new ParametersJSONSerialize());
		mappings.put(ParameterUse.class, new ParametersUseJSONSerialize());
		mappings.put(ModalitiesValue.class, new ModalitiesValuesJSONSerializer());
		mappings.put(FederationDefinition.class, new DatasetFederationJSONSerializer());

	}

	@Override
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
			logger.error("serialize",t);
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {

		}
		return result;
	}

}
