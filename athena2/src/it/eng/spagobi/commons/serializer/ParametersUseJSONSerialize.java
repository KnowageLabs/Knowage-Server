package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;

import java.util.Locale;

import org.json.JSONObject;

public class ParametersUseJSONSerialize implements Serializer {

	public static final String ID = "ID";
	public static final String USEID = "USEID";
	public static final String LOVID = "LOVID";
	public static final String DEFAULTLOVID = "DEFAULTLOVID";
	public static final String LABEL = "LABEL";
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final String NAME = "NAME";
	public static final String MANUALINPUT = "MANUALINPUT";
	public static final String SELECTIONTYPE = "SELECTIONTYPE";

	public Object serialize(Object o, Locale locale) throws SerializationException {

		JSONObject result = null;

		if (!(o instanceof ParameterUse)) {

			throw new SerializationException("ParametersUseJSONSerializer is unable to serialize object of type: " + o.getClass().getName());

		}

		try {

			ParameterUse parameterUse = null;
			result = new JSONObject();
			parameterUse = (ParameterUse) o;

			result.put(ID, parameterUse.getId());
			result.put(USEID, parameterUse.getUseID());
			result.put(LOVID, parameterUse.getIdLov());
			result.put(DEFAULTLOVID, parameterUse.getIdLovForDefault());
			result.put(LABEL, parameterUse.getLabel());
			result.put(NAME, parameterUse.getName());
			result.put(DESCRIPTION, parameterUse.getDescription());
			result.put(MANUALINPUT, parameterUse.getManualInput());
			result.put(SELECTIONTYPE, parameterUse.getSelectionType());

		} catch (Throwable t) {

			throw new SerializationException("An error occurred while serializing object: " + o, t);

		} finally {

		}

		return result;
	}
}
