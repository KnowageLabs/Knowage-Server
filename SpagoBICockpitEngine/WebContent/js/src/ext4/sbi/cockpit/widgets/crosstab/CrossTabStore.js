/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
  * Object name
  *
  * [description]
  *
  *
  * Public Properties
  *
  * MANDATORY PARAMETERS: serviceUrl: the url for the ajax request
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
  * Alberto Ghedin alberto.ghedin@eng.it
  *
  * - name (mail)
  */

Ext.define('Sbi.cockpit.widgets.crosstab.CrossTabStore', {
    extend: 'Ext.data.Store'

    	, crossTabStore: true
    	, myStoreMetaData: null



    	, loadData: function(data, par, store){
    		this.callParent([data, par]);
   			this.fireEvent("refreshData", data, this.myStoreMetaData);
    	}



});