/**
 *
 */
package it.eng.spagobi.api.v2.export.cockpit;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Dragan Pirkovic
 *
 */
public class DocumentExportConf implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Integer documentId;
	private String documentLabel;
	private Map<String, String> parameters;
	private ExportType exportType;

	/**
	 * @return the exportType
	 */
	public ExportType getExportType() {
		return exportType;
	}

	/**
	 * @param exportType
	 *            the exportType to set
	 */
	public void setExportType(ExportType exportType) {
		this.exportType = exportType;
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

}
