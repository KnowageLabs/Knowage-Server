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


/**
  * Object name
  *
  * [description]
  *
  *
  * Public Properties
  *
  * [list]
  *
  *
  * Public Methods
  *
  *  [list]
  *
  *
  * Public Events
  *
  *  [list]
  *
  * Authors
  *
  * - Andrea Gioia (andrea.gioia@eng.it)
  */

Ext.ns("Sbi.widgets");

Sbi.widgets.PagingToolbar = function(config) {

	// constructor
	Sbi.widgets.PagingToolbar.superclass.constructor.call(this, config);
};

Ext.extend(Sbi.widgets.PagingToolbar, Ext.PagingToolbar, {

	beforeLoad : function(store, o){
		Sbi.widgets.PagingToolbar.superclass.beforeLoad.call(this);

		 var pn = this.paramNames;
		 if(o.params[pn.start]  == undefined && o.params[pn.limit]  == undefined) {
			 // load has been forced not by paging toolbar
			 o.params[pn.start] = 0;
			 o.params[pn.limit] = this.pageSize;
		 }



		 return true;
	}
});