package it.eng.spagobi.api.v3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ContainerNode;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.serializer.DataSetMetadataJSONSerializer;
import it.eng.spagobi.tools.dataset.bo.DataSetParametersList;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.tag.SbiTag;

class DataSetForWorkspaceDTO extends DataSetMainDTO {

	private static final DataSetMetadataJSONSerializer metaSerializer = new DataSetMetadataJSONSerializer();
	private final List<DataSetParameterDTO> params = new ArrayList<>();
	private ContainerNode<?> meta;

	public DataSetForWorkspaceDTO(SbiDataSet dataset) {
		super(dataset);
		try {
			initParams();
			initMeta(dataset);
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

	private void initMeta(SbiDataSet dataset) throws SourceBeanException, JSONException {
		String metaAsString = dataset.getDsMetadata();
		meta = metaSerializer.serializeToJson(metaAsString).getWrappedObject();
	}

	@Override
	@JsonIgnore
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

	@JsonProperty("pars")
	public List<DataSetParameterDTO> getParams() {
		return params;
	}

	public ContainerNode<?> getMeta() throws SourceBeanException, JSONException {
		return meta;
	}

}