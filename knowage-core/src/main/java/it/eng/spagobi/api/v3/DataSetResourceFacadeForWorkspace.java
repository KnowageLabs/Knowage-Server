package it.eng.spagobi.api.v3;

import java.util.List;
import java.util.Set;

import org.json.JSONException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ContainerNode;

import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.serializer.DataSetMetadataJSONSerializer;
import it.eng.spagobi.tools.dataset.bo.DataSetParameterItem;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.tag.SbiTag;

class DataSetResourceFacadeForWorkspace extends DataSetResourceMainFacade {

	private static final DataSetMetadataJSONSerializer metaSerializer = new DataSetMetadataJSONSerializer();

	public DataSetResourceFacadeForWorkspace(SbiDataSet dataset) {
		super(dataset);
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
	public List<DataSetParameterItem> getParams() {
		return dataset.getParametersList();
	}

	public ContainerNode getMeta() throws SourceBeanException, JSONException {
		String meta = dataset.getDsMetadata();
		ContainerNode serializedMetadata = metaSerializer.serializeToJson(meta).getWrappedObject();
		return serializedMetadata;
	}

}