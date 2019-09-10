/**
 * 
 */
package it.eng.knowage.document.export.cockpit.converter;

import java.util.List;

import org.json.JSONArray;

import it.eng.knowage.document.export.cockpit.IConverter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;

/**
 * @author Dragan Pirkovic
 *
 */
public class GroupConverter implements IConverter<List<Projection>, JSONArray> {

	/* (non-Javadoc)
	 * @see it.eng.knowage.document.export.cockpit.IConverter#convert(java.lang.Object)
	 */
	@Override
	public List<Projection> convert(JSONArray input) {
		// TODO Auto-generated method stub
		return null;
	}

}
