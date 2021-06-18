package it.eng.spagobi.functions.dao;

import java.io.Serializable;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;

/**
 * Defines a <code>BIObjFunction</code> object.
 *
 * This class maps the SBI_OBJ_FUNCTION table
 */

public class BIObjFunction implements Serializable {

	private static final long serialVersionUID = 4203627476207372467L;

	private Integer biObjFunctionId;
	private BIObject biObject;
	private String functionUuid;

	public Integer getBiObjFunctionId() {
		return biObjFunctionId;
	}

	public void setBiObjFunctionId(Integer biObjfunctionId) {
		this.biObjFunctionId = biObjfunctionId;
	}

	public BIObject getBiObject() {
		return biObject;
	}

	public void setBiObject(BIObject biObject) {
		this.biObject = biObject;
	}

	public String getFunctionUuid() {
		return functionUuid;
	}

	public void setFunctionUuid(String functionUuid) {
		this.functionUuid = functionUuid;
	}
}
