package it.eng.spagobi.tools.glossary.util;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.glossary.metadata.SbiGlBnessCls;
import it.eng.spagobi.tools.glossary.metadata.SbiGlContents;
import it.eng.spagobi.tools.glossary.metadata.SbiGlTable;

import org.json.JSONException;
import org.json.JSONObject;

public class Util {

	public static boolean isNumber(String str) {
		return str != null && str.matches("\\d+");
	}

	public static Integer getNumberOrNull(Object str) {
		if (str instanceof Integer)
			return (Integer) str;
		return isNumber("" + str) ? Integer.valueOf("" + str) : null;
	}

	public static JSONObject fromContentsLight(SbiGlContents sbiGlContents, boolean wordChild, boolean ContentsChild) throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put("CONTENT_ID", sbiGlContents.getContentId());
		ret.put("CONTENT_NM", sbiGlContents.getContentNm());
		ret.put("HAVE_WORD_CHILD", wordChild);
		ret.put("HAVE_CONTENTS_CHILD", ContentsChild);

		return ret;
	}

	public static JSONObject fromDocumentLight(BIObject sbiob) throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put("DOCUMENT_ID", sbiob.getId());
		ret.put("DOCUMENT_NM", sbiob.getLabel());
		ret.put("DOCUMENT_NAME", sbiob.getName());
		ret.put("DOCUMENT_DESCR", sbiob.getDescription());
		return ret;
	}
	
	public static JSONObject fromDataSetLight(IDataSet datas)throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put("DATASET_ID", datas.getId());
		ret.put("DATASET_NM", datas.getLabel());
		ret.put("DATASET_ORG", datas.getOrganization());
		return ret;
	}
	
	public static JSONObject fromDataSetLight(SbiDataSet sbidataset)
			throws JSONException {
		JSONObject jobj = new JSONObject();
		jobj.put("DATASET_ID", sbidataset.getId().getDsId());
		jobj.put("DATASET_NM", sbidataset.getLabel());
		jobj.put("DATASET_ORG", sbidataset.getId().getOrganization());
		return jobj;
	}
	
	public static JSONObject fromBnessClsLight(SbiGlBnessCls sbibnesscls)
			throws JSONException {
		JSONObject jobj = new JSONObject();
		jobj.put("BC_ID", sbibnesscls.getBcId());
		jobj.put("BC_NM", sbibnesscls.getLabel());
		return jobj;
	}
	
	public static JSONObject fromTableLight(SbiGlTable sbitable)
			throws JSONException {
		JSONObject jobj = new JSONObject();
		jobj.put("TABLE_ID", sbitable.getTableId());
		jobj.put("TABLE_NM", sbitable.getLabel());
		return jobj;
	}
	

	public static JSONObject fromDocumentLight(SbiObjects sbiob) throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put("DOCUMENT_ID", sbiob.getBiobjId());
		ret.put("DOCUMENT_NM", sbiob.getLabel());
		return ret;
	}
}
