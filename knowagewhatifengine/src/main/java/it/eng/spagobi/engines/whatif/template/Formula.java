/**
 *
 */
package it.eng.spagobi.engines.whatif.template;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.utilities.StringUtils;

/**
 * @author Dragan Pirkovic
 *
 */
public class Formula {
	private String name;
	private final List<Argument> arguments;

	/**
	 *
	 */
	public Formula(SourceBean sb) {
		arguments = new ArrayList<>();
		this.name = (String) sb.getAttribute("name");
		List<SourceBean> argumentsSb = sb.getAttributeAsList("argument");
		if (argumentsSb != null) {
			for (SourceBean argument : argumentsSb) {
				arguments.add(new Argument((String) argument.getAttribute("default_value")));
			}
		}

	}

	/**
	 * @param formulaJson
	 * @throws JSONException
	 */
	public Formula(JSONObject formulaJson) throws JSONException {
		arguments = new ArrayList<>();
		name = formulaJson.getString("name");
		JSONArray argumentsJson = formulaJson.getJSONArray("argument");
		for (int i = 0; i < argumentsJson.length(); i++) {
			arguments.add(new Argument(argumentsJson.getJSONObject(i).getString("default_value")));
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	private String getArgumentExpression() {
		// StringUtils
		List<String> list = new ArrayList<>();
		for (Argument argument : arguments) {
			list.add(argument.getDefaultValue());
		}
		return StringUtils.join(list, ",");
	}

	public String getExpression() {
		return name + "(" + getArgumentExpression() + ")";
	}

	/**
	 * @return
	 */
	public List<Argument> getArguments() {
		// TODO Auto-generated method stub
		return arguments;
	}

}
