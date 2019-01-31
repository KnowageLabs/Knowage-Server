package it.eng.knowage.meta.generator.jpamapping.wrappers.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.BIMetaModelParameterDAOHibImpl;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParameters;

public class JpaOuterResource {

	public static List<String> getParamName(String sqlExpression) {
		Pattern pattern = Pattern.compile("\\$P\\{(.+?)\\}", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(sqlExpression);

		List<String> listToReturn = new ArrayList<>();

		while (matcher.find()) {
			String sqlFilter = matcher.group(1);
			listToReturn.add(sqlFilter);
		}

		return listToReturn;

	}

	public static SbiParameters getParameterByMetaModelIdAndName(String modelName, String name) {
		BIMetaModelParameterDAOHibImpl dao = new BIMetaModelParameterDAOHibImpl();
		SbiParameters sbiParam = dao.getParameterByModelAndDriverName(modelName, name);

		return sbiParam;
	}

}
