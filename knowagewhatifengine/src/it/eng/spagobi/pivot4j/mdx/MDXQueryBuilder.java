/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it) 
 */

package it.eng.spagobi.pivot4j.mdx;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Member;

public class MDXQueryBuilder {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(MDXQueryBuilder.class);

	public String getMDXForTuple(Member[] members, Cube cube) {
		logger.debug("IN: tuple = [" + members + "]");
		String toReturn = null;
		try {

			Member measure = null;
			List<Member> dimensions = new ArrayList<Member>();
			for (int i = 0; i < members.length; i++) {
				Member member = members[i];
				// if member is an all member or a formula, we skip it since we
				// don'y need to put this into the MDX query
				if (member.isAll()) {
					continue;
				}
				// TODO how to manage case when a dimension has 2 hierarchies
				// both without the all member?
				// we catch the member's measure
				if (member.getHierarchy().getDimension().getDimensionType().equals(Dimension.Type.MEASURE)) {
					measure = member;
					continue;
				}
				dimensions.add(member);
			}

			StringBuffer buffer = new StringBuffer();
			buffer.append("SELECT ");
			buffer.append(" {" + measure.getUniqueName() + "} ON COLUMNS, ");
			buffer.append(" {(");
			for (int i = 0; i < dimensions.size(); i++) {
				buffer.append(dimensions.get(i));
				if (i < dimensions.size() - 1) {
					buffer.append(", ");
				}
			}
			buffer.append(" )} ON ROWS FROM " + cube.getUniqueName());

			toReturn = buffer.toString();

		} catch (Exception e) {
			logger.error("Error while getting value for tuple [" + members + "]", e);
			throw new SpagoBIEngineRuntimeException("Error while getting value for tuple [" + members + "]", e);
		} finally {
			logger.debug("OUT: returning [" + toReturn + "]");
		}
		return toReturn;
	}

}
