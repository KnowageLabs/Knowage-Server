package it.eng.spagobi.kpi.bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardinalityBuilder {

	private final List<Cardinality> cardinalities = new ArrayList<>();
	private final Map<Rule, Map<String, Boolean>> attributeMap = new HashMap<>();

	public void addMeasure(Integer ruleId, Integer ruleVersion, String ruleName, String name) {
		Cardinality cardinality = new Cardinality();
		cardinality.setRuleId(ruleId);
		cardinality.setRuleVersion(ruleVersion);
		cardinality.setRuleName(ruleName);
		cardinality.setMeasureName(name);
		cardinalities.add(cardinality);
	}

	public void addAttribute(Integer ruleId, Integer ruleVersion, String name) {
		Rule rule = new Rule(ruleId, ruleVersion);
		if (!attributeMap.containsKey(rule)) {
			attributeMap.put(rule, new HashMap<String, Boolean>());
		}
		attributeMap.get(rule).put(name, false);
	}

	public List<Cardinality> getCardinality() {
		for (Cardinality cardinality : cardinalities) {
			Map<String, Boolean> selectedAttributeMap = attributeMap.get(new Rule(cardinality.getRuleId(), cardinality.getRuleVersion()));
			if (selectedAttributeMap != null) {
				cardinality.getAttributes().putAll(selectedAttributeMap);
			}
		}
		return cardinalities;
	}
}
