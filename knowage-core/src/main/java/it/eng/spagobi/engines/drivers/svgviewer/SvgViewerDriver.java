/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.engines.drivers.svgviewer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.engines.drivers.DefaultOutputParameter;
import it.eng.spagobi.engines.drivers.DefaultOutputParameter.TYPE;
import it.eng.spagobi.engines.drivers.generic.GenericDriver;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class SvgViewerDriver extends GenericDriver {

	static private Logger logger = Logger.getLogger(SvgViewerDriver.class);

	@Override
	public List<DefaultOutputParameter> getDefaultOutputParameters() {
		List<DefaultOutputParameter> ret = new ArrayList<>();
		ret.add(new DefaultOutputParameter("HIERARCHY", TYPE.String));
		ret.add(new DefaultOutputParameter("LEVEL", TYPE.String));
		ret.add(new DefaultOutputParameter("MEMBER", TYPE.String));
		ret.add(new DefaultOutputParameter("ELEMENT_ID", TYPE.String));
		return ret;
	}

	@Override
	public ArrayList<String> getDatasetAssociated(byte[] contentTemplate) throws JSONException {
		ArrayList<String> associatedDatasets = new ArrayList<String>();

		SourceBean templateSB = null;
		try {
			templateSB = SourceBean.fromXMLString(new String(contentTemplate));
		} catch (SourceBeanException e) {
			logger.error(
					"An error occured while recovering the template, so the relations between document and dataset cannot be inserted. Please check the template uploaded!");
			return null;
		}

		// 1. search used datasets
		SourceBean dmSB = (SourceBean) templateSB.getAttribute("DATAMART_PROVIDER");
		SourceBean hierarchySB = (SourceBean) dmSB.getAttribute("HIERARCHY");

		List members = hierarchySB.getAttributeAsList("MEMBER");
		// 3. insert the new relations between document and datasets

		for (int i = 0; i < members.size(); i++) {
			SourceBean memberSB = null;
			logger.debug("Parsing member  [" + i + "]");
			memberSB = (SourceBean) members.get(i);

			String dsLabel = (String) memberSB.getAttribute("measure_dataset");
			logger.debug("Insert relation for dataset with label [" + dsLabel + "]");

//			VersionedDataSet ds = ((VersionedDataSet) DAOFactory.getDataSetDAO().loadDataSetByLabel(dsLabel));
			// insert only relations with new ds
			associatedDatasets.add(dsLabel);
		}
		return associatedDatasets;
	}

}
