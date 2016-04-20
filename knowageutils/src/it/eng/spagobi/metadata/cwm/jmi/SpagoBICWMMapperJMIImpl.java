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
package it.eng.spagobi.metadata.cwm.jmi;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.CwmCatalog;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.CwmColumn;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.CwmTable;
import it.eng.spagobi.meta.model.physical.PhysicalColumn;
import it.eng.spagobi.meta.model.physical.PhysicalModel;
import it.eng.spagobi.meta.model.physical.PhysicalModelFactory;
import it.eng.spagobi.meta.model.physical.PhysicalTable;
import it.eng.spagobi.metadata.cwm.ICWM;
import it.eng.spagobi.metadata.cwm.ICWMMapper;

import java.util.Collection;
import java.util.List;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SpagoBICWMMapperJMIImpl extends ICWMMapper {

	static public PhysicalModelFactory FACTORY = PhysicalModelFactory.eINSTANCE;

	// -----------------------------------------------------------------------------
	// DECODE
	// -----------------------------------------------------------------------------

	@Override
	public PhysicalModel decodeICWM(ICWM cwm) {
		return decodeModel((SpagoBICWMJMIImpl) cwm);
	}

	public PhysicalModel decodeModel(SpagoBICWMJMIImpl cwm) {
		PhysicalModel model = FACTORY.createPhysicalModel();
		model.setName(cwm.getName());

		model.setCatalog(cwm.getCatalog().getName());

		return model;
	}

	// -----------------------------------------------------------------------------
	// ENCODE
	// -----------------------------------------------------------------------------

	@Override
	public SpagoBICWMJMIImpl encodeICWM(PhysicalModel model) {

		SpagoBICWMJMIImpl cwm = new SpagoBICWMJMIImpl(model.getName());

		CwmCatalog catalog = cwm.createCatalog(model.getCatalog());

		List<PhysicalTable> tables = model.getTables();
		CwmTable table;
		Collection<CwmTable> tc = catalog.getOwnedElement();
		for (int i = 0; i < tables.size(); i++) {
			table = encodeTable(cwm, tables.get(i));
			tc.add(table);
			table.setNamespace(catalog);
			List<PhysicalColumn> columns = tables.get(i).getColumns();
			for (PhysicalColumn column : columns) {
				CwmColumn cwmColumn = encodeColumn(cwm, column);
				cwmColumn.setLength(column.getSize());
				cwmColumn.setReferencedTableType(cwm.createDataType(column.getDataType()));
				// TODO:...

			}
		}

		return cwm;
	}

	public CwmTable encodeTable(SpagoBICWMJMIImpl cwm, PhysicalTable table) {
		CwmTable t;
		t = cwm.createTable(table.getName());
		return t;
	}

	public CwmColumn encodeColumn(SpagoBICWMJMIImpl cwm, PhysicalColumn column) {
		CwmColumn c;
		c = cwm.createColumn(column.getName());
		return c;
	}

}
