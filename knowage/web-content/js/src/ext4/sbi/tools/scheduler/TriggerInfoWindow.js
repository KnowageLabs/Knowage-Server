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

Ext.define('Sbi.tools.scheduler.TriggerInfoWindow', {
	extend: 'Ext.Window'
		,layout: 'fit'
		,config: {   
			
	    	width: 600,
			height: 350

	    } 
		
		,constructor: function(config, data) {
			this.initConfig(config);
			this.initTabPanel(data);
			this.callParent(arguments);

		}
		
		, initTabPanel: function(data){
			var localConf = {};	
			localConf.activeTab = 0;
			localConf.height = this.height;
			localConf.border = false;
			
			var tabsItems = [];
			
			var parsedData = JSON.parse(data);
			if ((parsedData.documents != null) && (parsedData.documents != undefined)){
				for (var i = 0; i< parsedData.documents.length; i++){
					var document = parsedData.documents[i];
					var docLabel = document.label;
					var mailTos= '<b>'+LN('sbi.scheduler.schedulation.mailto')+':</b> '+(document.mailtos || "" )+"</br>";
					var zipMailName= '<b>'+LN('sbi.scheduler.schedulation.attachedzip')+':</b> '+(document.zipMailName || "" )+"</br>";
					var mailSubject= '<b>'+LN('sbi.scheduler.schedulation.mailsubject')+':</b> '+(document.mailsubj || "" )+"</br>";
					var containedFileName= '<b>'+LN('sbi.scheduler.schedulation.containedfilename')+':</b> '+(document.containedFileName || "" )+"</br>";
					var mailTxt= '<b>'+LN('sbi.scheduler.schedulation.mailtext')+':</b> '+(document.mailtxt || "" )+"</br>";
					
					var content = mailTos+zipMailName+mailSubject+containedFileName+mailTxt;
					var item = {
							title: docLabel,
							html: content,
			                bodyPadding: 10,
			                autoScroll: true
					}
					tabsItems.push(item);
					
					
				}
			}

			
			
			localConf.items = tabsItems;
			
	    	this.tabPanel = Ext.create('Ext.TabPanel', localConf);
	    	this.items = [this.tabPanel];
		}
		
		
});	