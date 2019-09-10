/**
 *
 */
package it.eng.knowage.document.cockpit;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.knowage.document.cockpit.template.CockpitTemplateReader;
import it.eng.knowage.document.cockpit.template.DBCockpitTemplateRetriver;
import it.eng.knowage.document.cockpit.template.ICockpitTemplateReader;
import it.eng.knowage.document.cockpit.template.ICockpitTemplateRetriver;

/**
 * @author Dragan Pirkovic
 *
 */
public class CockpitDocument {
	private Map<String, String> parameters;
	private Integer documentId;
	private String documentLabel;
	private ICockpitTemplateReader cockpitTemlate;

	/**
	 * @param parameters
	 * @param documentId
	 * @param documentLabel
	 */
	public CockpitDocument(Map<String, String> parameters, Integer documentId, String documentLabel) {
		super();
		this.parameters = parameters;
		this.documentId = documentId;
		this.documentLabel = documentLabel;
		init();
	}

	/**
	 *
	 */
	private void init() {
		ICockpitTemplateRetriver cockpitTemplateRetriver = new DBCockpitTemplateRetriver(documentId);
		JSONObject jsonTemplate = cockpitTemplateRetriver.getTemplate();
		this.cockpitTemlate = new CockpitTemplateReader(jsonTemplate);

	}

	/**
	 * @return the parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the documentId
	 */
	public Integer getDocumentId() {
		return documentId;
	}

	/**
	 * @param documentId
	 *            the documentId to set
	 */
	public void setDocumentId(Integer documentId) {
		this.documentId = documentId;
	}

	/**
	 * @return the documentLabel
	 */
	public String getDocumentLabel() {
		return documentLabel;
	}

	/**
	 * @param documentLabel
	 *            the documentLabel to set
	 */
	public void setDocumentLabel(String documentLabel) {
		this.documentLabel = documentLabel;
	}

	/**
	 * @return
	 */
	public String getLabel() {

		return documentLabel;
	}

	/**
	 * @return
	 */
	public JSONArray getWidgets() {

		return this.cockpitTemlate.getWidgets();
	}

	public JSONObject getParamsByDataSetId(Integer dsId) {
		return this.cockpitTemlate.getParamsByDataSetId(dsId);

	}

	public String getDataSetLabelById(Integer dsId) {
		return this.cockpitTemlate.getDataSetLabelById(dsId);
	}

	public JSONObject getFilters() {
		return this.cockpitTemlate.getFilters();

	}

}
