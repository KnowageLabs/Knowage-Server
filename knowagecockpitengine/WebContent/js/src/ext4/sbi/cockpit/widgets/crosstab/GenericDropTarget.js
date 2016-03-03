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
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */

Ext.ns("Sbi.cockpit.widgets.crosstab");

Sbi.cockpit.widgets.crosstab.GenericDropTarget = function(targetPanel, config) {

	var c = Ext.apply({
		//ddGroup must be provided by input config object!!
		copy       : false
	}, config || {});

	Ext.apply(this, c);

	this.targetPanel = targetPanel;

	// constructor
	Sbi.cockpit.widgets.crosstab.GenericDropTarget.superclass.constructor.call(this, this.targetPanel.getEl(), c);
};

Ext.extend(Sbi.cockpit.widgets.crosstab.GenericDropTarget, Ext.dd.DropTarget, {

	targetPanel: null

	/*
    , notifyOver : function(ddSource, e, data) {
		return this.dropAllowed;
	}
	*/

	, notifyDrop : function(ddSource, e, data) {
		if (this.onFieldDrop) {
			this.onFieldDrop.call(this.targetPanel, ddSource);
		}
	}

});