/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.v3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ContainerNode;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.DataSetMetadataJSONSerializer;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.dataset.bo.DataSetParametersList;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.tag.SbiTag;

class DataSetForWorkspaceDTO extends AbstractDataSetDTO {

	private static final DataSetMetadataJSONSerializer metaSerializer = new DataSetMetadataJSONSerializer();
	private final List<DataSetParameterDTO> params = new ArrayList<>();
	private final List<DataSetParameterDTO> drivers = new ArrayList<>();
	private ContainerNode<?> meta;

	public DataSetForWorkspaceDTO(SbiDataSet dataset) {
		super(dataset);
		try {
			initParams();
			initMeta();
			initDrivers();
		} catch (Exception e) {
			throw new RuntimeException("Cannot create DTO for dataset " + dataset.getLabel(), e);
		}
	}

	private void initParams() throws SourceBeanException {
		String pars = this.dataset.getParameters();
		if (pars != null && !pars.equals("")) {
			SourceBean source = SourceBean.fromXMLString(pars);
			if (source != null && source.getName().equals("PARAMETERSLIST")) {
				List<SourceBean> rows = source.getAttributeAsList("ROWS.ROW");
				for (int i = 0; i < rows.size(); i++) {
					SourceBean row = rows.get(i);
					String name = (String) row.getAttribute("NAME");
					String type = (String) row.getAttribute("TYPE");
					String defaultValue = (String) row.getAttribute(DataSetParametersList.DEFAULT_VALUE_XML);
					boolean multiValue = "true".equalsIgnoreCase((String) row.getAttribute("MULTIVALUE"));

					params.add(new DataSetParameterDTO(name, type, defaultValue, multiValue));
				}
			}
		}
	}

	private void initMeta() throws SourceBeanException, JSONException {
		String metaAsString = dataset.getDsMetadata();
		if (metaAsString != null)
			meta = metaSerializer.serializeToJson(metaAsString).getWrappedObject();
	}

	private void initDrivers() throws JSONException {
		if ("SbiQbeDataSet".equals(dataset.getType())) {

			String configuration = dataset.getConfiguration();
			JSONObject jsonObject = new JSONObject(configuration);

			String datamart = jsonObject.optString("qbeDatamarts", null);

			MetaModel metaModel = DAOFactory.getMetaModelsDAO().loadMetaModelByName(datamart);

			metaModel.getDrivers().forEach(e -> {

				String name = e.getParameter().getName();
				String type = e.getParameter().getType();
				boolean multivalue = e.getMultivalue().intValue() == 1;

				DataSetParameterDTO item = new DataSetParameterDTO(name, type, null, multivalue);

				drivers.add(item);
			});

		}
	}

	@Override
	public List<DataSetResourceAction> getActions() {
		return super.getActions();
	}

	public String getDescription() {
		return dataset.getDescription();
	}

	public String getAuthor() {
		return dataset.getOwner();
	}

	public Set<SbiTag> getTags() {
		return dataset.getTags();
	}

	public String getDateIn() {
		return dataset.getCommonInfo().getTimeIn().toInstant().toString();
	}

	public List<DataSetParameterDTO> getDrivers() {
		return drivers;
	}

	@JsonProperty("pars")
	public List<DataSetParameterDTO> getParams() {
		return params;
	}

	public ContainerNode<?> getMeta() throws SourceBeanException, JSONException {
		return meta;
	}

}