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
 * Container of the federated dataset catalogue.
 * 
 * 
 *  @author
 * Giulio Gavardi(giulio.gavardi@eng.it)
 *  
 */


Ext.define('Sbi.tools.dataset.FederatedDatasetView', {
	extend : 'Ext.DataView'
		
		,
		config : {
			/**
			 * The Ext.data.Store to bind this DataView to.
			 */
			store : null,

			/**
			 * A simple CSS selector that will be used to determine what nodes this
			 * DataView will be working with.
			 */
			itemSelector : null,

			/**
			 * The definition of the columns of the grid.
			 * {@link Sbi.widgets.store.InMemoryFilteredStore#InMemoryFilteredStore}
			 */
			columns : [],
			/**
			 * The list of the properties that should be filtered
			 */
			filteredProperties : new Array(),

			autoScroll : true


		}
	
	/**
	 * In this constructor you must pass configuration
	 */
	,
	constructor : function(config) {
	
		this.initConfig(config);
		this.initTemplate();
	
		Ext.apply(this, config || {});
	
		this.itemSelector = 'dd';
		//this.overClass = 'over';
		this.trackOver = true;
		//this.overItemCls = 'x-item-over';
		this.overItemCls = 'over';
		this.frame = true;
		this.emptyText = LN('sbi.ds.noDataset');
		this.inline = {
				wrap : false
		};
		this.idsOfFederationDefinitionsUsediNFederatedDatasets = [];
		this.allFederatedDatasets = [];
		this.scrollable = 'horizontal';
	
		this.callParent(arguments);
	
		this.addListener('itemclick', this.onClick, this);
	
		this.addEvents(
				/**
				 * @event event1
				 * Execute the qbe clicking in the model
				 * @param {Object} docType 'QBE'
				 * @param {Object} inputType 'MODEL'
				 * @param {Object} record the record that contains all the information of the metamodel
				 */
				'executeDocument'
		);
		this.getDatasets();
	}
	
	
	
	,getDatasets: function(){
		var globalScope = this;
		var obj = {};
		Ext.Ajax.request({
			url: Sbi.config.contextName+"/restful-services/1.0/datasets",
			method: 'GET', 
			headers: { 'Content-Type': 'application/json' },
		    success: function(response) {
		    	var allDatasets = [];
		    	obj = JSON.parse(response.responseText);
		    	allDatasets = obj.root;
		    	
				for (var i = 0; i < allDatasets.length; i++) {
					if(allDatasets[i].hasOwnProperty('federationId')) {
						if(globalScope.idsOfFederationDefinitionsUsediNFederatedDatasets.indexOf(allDatasets[i].federationId)==-1){
							globalScope.idsOfFederationDefinitionsUsediNFederatedDatasets.push(allDatasets[i].federationId);
						}
						globalScope.allFederatedDatasets.push(allDatasets[i]);
					}
				}
		    },
		    failure: function(response, opts) {
		        console.log('server-side failure with status code ' + response.status);
		    }
		});
		
	}
	
	,initTemplate : function() {
	
		createFederationDefinition = function () {
			var urlToCall =  Sbi.config.contextName+"/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp";
			window.location.href = urlToCall;
		}
	
		// BUILD THE TPL
		Sbi.debug('DataViewPanel bulding the tpl...');
		var noItem = LN('sbi.browser.folderdetailpanel.emptytext');
		var title = LN('sbi.ds.listTitle');
	
		var buttonShowQbe ='<li class="qbe"><a id="showQbe" href="#" title="Show Qbe"></a></li>';
		var buttonEditFederated = '<li class="editFederated"><a id="editFederated" href="#" title="Edit federated"></a></li>';
		var buttonDeleteFederated = '<li class="deleteFederated"><a id="deleteFederated" href="#" title="Delete federated"></a></li>';
		var createFederation = '<button class="btn btn-default" onclick="createFederationDefinition()"><a id="createFederated" href="#" title="Create federated"></a>CREATE FEDERATION</button>';
		var buttonHelpOnLine= Sbi.user.functionalities.indexOf("Glossary")!=-1 ? '<li class="MyDataHelpOnLine"><a id="MHOL" href="#" title="Show Help OnLine"></a></li>' : "";
	
		this.tpl = new Ext.XTemplate(
				'<script src="'+Sbi.config.contextName+'/js/src/ext4/sbi/tools/dataset/FederatedDatasetView.js">',
				'</script>',
				'<div class="list-actions-container createfederationbtn">',
				createFederation,
				'</div>',
				'<div id="list-container" class="main-datasets-list">', 	            
				'<tpl if="root.length == 0">',
				'<div id="empty-group-message">',
				noItem,
				'</div>',
				'</tpl>', 
				'<tpl for=".">',
				'<dd class="box">',
				'<div class="box-container">',
				'<div class="box-figure">',
				'<img  align="center" src="'+Sbi.config.contextName+'/themes/sbi_default/img/metamodel/federation.png" alt=" " />',
				'<span class="shadow"></span>',
				'<div class="hover">',
				'<div class="box-actions-container">',
				'    <ul class="box-actions">',	    
				buttonShowQbe,
				buttonEditFederated,
				buttonDeleteFederated,
				'    </ul>',
				'</div>',
				'</div>',										
				'</div>',
				'<div title="{name}" class="box-text">',
				'<h2>{name}</h2>',
				'<p>{[Ext.String.ellipsis(values.description, 100, false)]}</p>',
				'</div>',
				'</div>',
				'</dd>',
				'</tpl>',	 
				'<div style="clear:left"></div>',
	
				'</div>'
		);			
		Sbi.debug('DataViewPanel tpl built.');
	
		return this.tpl;
	}
	
	,onClick : function(obj, record, item, index, e, eOpts) {
	
		if(e.target.id == 'createFederated')	{
			var urlToCall = Sbi.config.contextName+"/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp";
			window.location.href = urlToCall;
		}
	
		else if(e.target.id == 'editFederated')	{
			var id = record.data.id;
			var label = record.data.label;
			var urlToCall = Sbi.config.contextName+"/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp&id="+id+"&label="+label;
			window.location.href = urlToCall;
		}
	
		else if(e.target.id == 'deleteFederated')	{
			var globalScope = this;
			var id = record.data.id;
			var usedInDatasets = [];
			var fds = globalScope.allFederatedDatasets;
			if (globalScope.idsOfFederationDefinitionsUsediNFederatedDatasets.indexOf(id)>-1) {
				for (var i = 0; i < globalScope.allFederatedDatasets.length; i++) {
					if(globalScope.allFederatedDatasets[i].federationId==id){
						usedInDatasets.push(globalScope.allFederatedDatasets[i].label)
					}
				}
				Ext.Msg.alert(LN('sbi.generic.error'), LN('sbi.federationdefinition.models.delete') +"["+usedInDatasets+"]");
			} else {
				Ext.Msg.confirm(LN('sbi.federationdefinition.confirm.dialog'), LN('sbi.federationdefinition.confirm.delete'), function(btn){
					  if (btn == 'yes'){
						  Ext.Ajax.request({
								url: Sbi.config.contextName+"/restful-services/2.0/federateddataset/"+id,
								method: 'DELETE', 
								headers: { 'Content-Type': 'application/json' },
							    success: function() {
							    	var urlToCall =   Sbi.config.contextName+"/servlet/AdapterHTTP?ACTION_NAME=SELF_SERVICE_DATASET_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE&MYDATA=TRUE&CALLBACK_FUNCTION=openFederation"; 
									window.location.href = urlToCall;
							    },
							    failure: function(response, opts) {
							        console.log('server-side failure with status code ' + response.status);
							    }
							});
					  }
				});
				
			}	
		}
		else {
			this.fireEvent('executeDocument','QBE','FEDERATED_DATASET',record);
		}
	}
});