/**
 *
 */
package it.eng.spagobi.engines.whatif.template.initializer.impl;

import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.whatif.template.CalculatedField;
import it.eng.spagobi.engines.whatif.template.Formula;
import it.eng.spagobi.engines.whatif.template.WhatIfTemplate;
import it.eng.spagobi.engines.whatif.template.initializer.AbstractInitializer;

/**
 * @author Dragan Pirkovic
 *
 */
public class CalculatedFieldsInitializerImpl extends AbstractInitializer {

	public static transient Logger logger = Logger.getLogger(CalculatedFieldsInitializerImpl.class);

	public static final String CALCULATED_FIELDS = "calculated_fields";
	public static final String CALCULATED_FIELD = "calculated_field";
	public static final String CALCULATED_FIELD_NAME = "name";
	public static final String PARENT_MEMBER_UNIQUE_NAME = "parentMemberUniqueName";

	@Override
	public void init(SourceBean template, WhatIfTemplate toReturn) {
		SourceBean calculatedFields = (SourceBean) template.getAttribute(CALCULATED_FIELDS);
		if (calculatedFields != null) {
			initCalculatedFields(calculatedFields, toReturn.getCalculatedFields());
		}
	}

	private void initCalculatedFields(SourceBean calculatedFieldsSB, List<CalculatedField> calculatedFields) {
		List<SourceBean> list = calculatedFieldsSB.getAttributeAsList(CALCULATED_FIELD);
		for (SourceBean calculatedFieldSB : list) {
			String calculatedFieldName = (String) calculatedFieldSB.getAttribute(CALCULATED_FIELD_NAME);
			String parentMemberUniqueName = (String) calculatedFieldSB.getAttribute(PARENT_MEMBER_UNIQUE_NAME);
			Formula formula = null;
			if (calculatedFieldSB.getAttribute("formula") != null) {
				formula = new Formula((SourceBean) calculatedFieldSB.getAttribute("formula"));
			}

			calculatedFields.add(new CalculatedField(calculatedFieldName, formula, parentMemberUniqueName));
		}
	}

}
