package it.eng.spagobi.kpi;

import it.eng.spagobi.kpi.bo.Alias;
import it.eng.spagobi.kpi.bo.Rule;
import it.eng.spagobi.kpi.bo.RuleOutput;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * @authors Salvatore Lupo (Salvatore.Lupo@eng.it)
 * 
 */
@Path("/1.0/kpi")
@ManageAuthorization
public class KpiService {

	List<RuleOutput> measures = new ArrayList<>();
	List<Alias> aliases = new ArrayList<>();
	List<Rule> rules = new ArrayList<>();
	{
		Rule rule = new Rule();
		rule.setId(1);
		rule.setName("Regola1");
		rule.setDefinition("select ...");
		rules.add(rule);

		RuleOutput m = new RuleOutput();
		m.setAlias("Measure 1");
		m.setAliasId(1);
		m.setAuthor("Salvo L.");
		m.setCategory("categoria 1");
		m.setType("Measure");
		m.setRule("Regola1");
		m.setRuleId(1);
		measures.add(m);

		m = new RuleOutput();
		m.setAlias("Measure 2");
		m.setAliasId(2);
		m.setAuthor("Salvo L.");
		m.setCategory("categoria 1");
		m.setType("Measure");
		m.setRule("Regola1");
		m.setRuleId(1);
		measures.add(m);

		Alias alias = new Alias();
		alias.setId(1);
		alias.setName("Measure 1");
		aliases.add(alias);
		alias = new Alias();
		alias.setId(2);
		alias.setName("Measure 2");
		aliases.add(alias);
		alias = new Alias();
		alias.setId(3);
		alias.setName("Measure 3");
		aliases.add(alias);
		alias = new Alias();
		alias.setId(4);
		alias.setName("Measure 4");
		aliases.add(alias);
		alias = new Alias();
		alias.setId(5);
		alias.setName("Aereoporto");
		aliases.add(alias);
		alias = new Alias();
		alias.setId(6);
		alias.setName("Volo");
		aliases.add(alias);
	}

	@GET
	@Path("/listMeasure")
	public Response listMeasure() {

		return Response.ok(JsonConverter.objectToJson(measures, measures.getClass())).build();
	}

	@GET
	@Path("/{id}/loadRule")
	public Response loadRule(@PathParam("id") Integer id) {
		for (RuleOutput measure : measures) {
			if (measure.getId().equals(id)) {
				return Response.ok(JsonConverter.objectToJson(measure, measure.getClass())).build();
			}
		}
		return Response.ok().build();
	}

	@GET
	@Path("/listAlias")
	public Response listAlias() {

		return Response.ok(JsonConverter.objectToJson(aliases, aliases.getClass())).build();
	}

	@POST
	@Path("/saveRule")
	public Response saveRule(@Context HttpServletRequest req) {
		try {
			String requestVal = RestUtilities.readBody(req);
			Rule rule = (Rule) JsonConverter.jsonToObject(requestVal, Rule.class);
			if (rule.isNewRecord()) {
				rule.setId(rules.size());
				rules.add(rule);
			} else {
				Rule oldRule = rules.get(rules.indexOf(rule));
				oldRule.setName(rule.getName());
				oldRule.setDefinition(rule.getDefinition());
				oldRule.setRuleOutputs(rule.getRuleOutputs());
				for (RuleOutput ruleOutput : rule.getRuleOutputs()) {
					if (!measures.contains(ruleOutput)) {
						measures.add(ruleOutput);
					}
				}
			}
		} catch (IOException e) {
			throw new SpagoBIServiceException(req.getPathInfo() + " Error while reading input object ", e);
		}
		return Response.ok().build();
	}
}
