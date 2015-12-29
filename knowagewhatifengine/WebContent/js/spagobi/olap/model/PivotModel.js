/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */

Ext.define('Sbi.olap.PivotModel', {
	extend: 'Ext.data.Model',
	fields: [ {name: 'rows',  type: 'array'},
	          {name: 'columns',   type: 'array'},
	          {name: 'filters',   type: 'array'},
	          {name: 'table', type: 'string'},
	          {name: 'rowsAxisOrdinal', type: 'int'},
	          {name: 'columnsAxisOrdinal', type: 'int'},
	          {name: 'mdxFormatted', type: 'String'},
	          {name: 'hasPendingTransformations', type: 'boolean'}]
});