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
 * 
 * This class is the container for the ad-hoc reporting interface 
 *    
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 *  Davide Zerbetto (davide.zerbetto@eng.it)
 */
 
  
Ext.define('Sbi.adhocreporting.AdhocreportingTabsPanel', {
	//extend: 'Ext.tab.Panel',
	extend: 'Ext.Panel',

    config: {
    	executionPanel: null,
    	myAnalysisServicePath : ''
    }
	
	
	, constructor : function(config) {

		this.initConfig(config);
		
		this.layout = 'fit';

		var myAnalysisBrowserConf = {
				 user: Sbi.user.userId
				, myAnalysisServicePath: config.myAnalysisServicePath
				, id: 'this'
				, useCockpitEngine: (config.adhocreportingContainer.cockpitEngineBaseUrl != 'null')?true:false
				, useWSEngine: ((config.adhocreportingContainer.worksheetEngineBaseUrl != 'null') && Sbi.settings.mydata.isWorksheetEnabled)?true:false
				, useQbeEngine: (config.adhocreportingContainer.qbeFromDataSetBaseUrl != 'null')?true:false
				, useGeoEngine: (config.adhocreportingContainer.georeportEngineBaseUrl != 'null' &&
								 Sbi.settings.myanalysis.showMapFilter)?true:false
		};
		
		this.myAnalysisBrowser = Ext.create('Sbi.adhocreporting.MyAnalysisBrowser',myAnalysisBrowserConf);
		
		
		this.items = [ this.myAnalysisBrowser ];

		this.callParent(arguments);
		this.addEvents(
		        /**
		         * @event event1
		         * Execute the qbe clicking in the model/dataset
				 * @param {Object} docType engine to execute 'QBE'/'WORKSHEET'/'COCKPIT'
				 * @param {Object} inputType 'DOCUMENT'
				 * @param {Object} record the record that contains all the information of the document
		         */
		        'executeDocument'
				);
		this.myAnalysisBrowser.on('executeDocument',function(docType, inputType,  record){
			this.fireEvent('executeDocument',docType, inputType,  record);
		},this);
		
		this.addEvents('openMyDataForReport');
		this.myAnalysisBrowser.on('openMyDataForReport',function(){
			this.fireEvent('openMyDataForReport');
		},this);
		
		this.addEvents('openMyDataForGeo');		
		this.myAnalysisBrowser.on('openMyDataForGeo',function(){
			this.fireEvent('openMyDataForGeo');
		},this);
		
		this.addEvents('openCockpitDesigner');		
		this.myAnalysisBrowser.on('openCockpitDesigner',function(){
			this.fireEvent('openCockpitDesigner');
		},this);
	}

    
	
});