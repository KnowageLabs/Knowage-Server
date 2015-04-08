/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.widgets");

Sbi.widgets.DatasetsBrowserView = function(config) {

	Sbi.trace("[DatasetsBrowserView.constructor]: IN");

	var defaultSettings = {
		itemSelector : 'dd'
		, trackOver : true
		, overItemCls : 'over'
		, frame : false
		, border: false
		, emptyText : LN('No Documents')
		, inline : {wrap : false}
		, autoScroll: true
	};
	var settings = Sbi.getObjectSettings('Sbi.widgets.DatasetsBrowserView', defaultSettings);
	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	this.initTemplate();

	Sbi.widgets.DatasetsBrowserView.superclass.constructor.call(this, c);

	Sbi.trace("[DatasetsBrowserView.constructor]: OUT");
};

Ext.extend(Sbi.widgets.DatasetsBrowserView, Ext.DataView, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	tpl: null

	, filterOption: null

	/**
     * @property {Array} usedDatasets
     * The labels of selected datasets. The selected datasets will be properly highlighted
     * in the browser view
     */
	, usedDatasets: null
	, selectedDataset: null

	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method

     * @returns {Array} the list of labels of the used datasets
	 *
	 */
	, getUsedDatasets: function() {
		return this.usedDatasets;
	}

	/**
	 * @method
	 * Set the list used datasets. The used datasets can be filtered out in browser view
	 *
	 * @params {Array} datasets the list of labels of the used datasets
	 *
	 */
	, setUsedDatasets: function(datasets) {
		this.usedDatasets = datasets;
	}

	/**
	 * @method
	 *
	 * The meaning of used depend on the conetxt in which the browser is used. For example in the cockpit engine
	 * a dataset is used when it is referentiated by at least one widget contained in the cockpit itself
	 *
	 * @params {String} datasetLabel the label of the dataset to test against
	 *
	 * @returns {boolean} true if the passed in dataset is contained in the list of used datasets. false otherwise
	 */
	, isUsed: function(datasetLabel) {
		var isUsed = false;
		if(Sbi.isValorized(this.usedDatasets)) {
			for(var i = 0; i < this.usedDatasets.length; i++) {
				if( this.usedDatasets[i] === datasetLabel) {
					isUsed = true;
					break;
				}

			}
		}
		return isUsed;
	}

	, getUsedDatasetsCount: function() {
		var count = 0;
		if(Sbi.isValorized(this.usedDatasets)) {
			count = this.usedDatasets.length;
		}
		return count;
	}

	/**
	 * @methods
	 *
	 * @params {String} datasetLabel the label of the dataset to test against
	 *
	 * @returns {boolean} true if the passed in dataset is contained in the list of selected datasets. false otherwise
	 */
	, isSelected: function(datasetLabel) {
		return this.selectedDataset === datasetLabel;
	}

	// TODO reduce the number of required refresh
	, refresh : function(){
		Sbi.widgets.DatasetsBrowserView.superclass.refresh.call(this);
		if(this.selectedDataset !== null) {
			this.selectDatasetComponent(this.selectedDataset);
		}
	}

    //override of the DataView.collectData method to manage visibility correctly
	, collectData: function(records, startIndex){
		Sbi.trace("[DatasetsBrowserView.collectData]: IN");
		var collectedData = new Array();

		if (this.filterOption == 'UsedDataSet' && this.getUsedDatasetsCount() === 0){
			Sbi.trace("[DatasetsBrowserView.collectData]: There are no datasets in use");
			return collectedData;
		}

		Sbi.trace("[DatasetsBrowserView.collectData]: There are [" + records.length + "] dataset in use");
		for(var i=0; i < records.length; i++){
			var preparedItem = null;
			var addItem = false;


			preparedItem = this.prepareData(records[i].data, startIndex + i, records[i]);

			if(this.isUsed(preparedItem.label)) {
				preparedItem.isUsed = 'true';
				addItem = true;
			} else if (this.filterOption != 'UsedDataSet'){
				preparedItem.isUsed = 'false';
				addItem = true;
			}

			if(this.isSelected(preparedItem.label)) {
				preparedItem.isSelected = 'true';
			} else {
				preparedItem.isSelected = 'false';
			}


			if (addItem) collectedData.push(preparedItem);
		}

		Sbi.trace("[DatasetsBrowserView.collectData]: OUT");

		return collectedData;
	}


	//Build the TPL
	, initTemplate : function() {

		Sbi.trace("[DatasetsBrowserView.initTemplate]: IN");

		var tpl = null;
		var datasetsTpl = this.getDatasetsTemplate();

		tpl = new Ext.XTemplate(
				 '<div id="list-container" class="main-datasets-list">',
				 	'<dl>',
					'<tpl for=".">',
					    '{[isUsed=""]}',
					    '{[label=""]}',
					    '{[isSelected=""]}',
					    '<tpl if="this.checkIsSelected(isSelected, label) == true">'+
					 		'<dd id="{label}" class="box selectboxBlu">',
					 			datasetsTpl,
						    '</dd>',
					    '</tpl>'+
					    '<tpl if="this.checkIsSelected(isSelected, label) == false && this.checkIsUsed(isUsed, label) == true">'+
					 		'<dd id="{label}" class="box selectboxOrange">',
					 			datasetsTpl,
						    '</dd>',
					    '</tpl>'+
				        '<tpl if="this.checkIsSelected(isSelected, label) == false && this.checkIsUsed(isUsed, label) == false">'+
					        '<dd id="{label}" class="box">',
					 			datasetsTpl,
						    '</dd>',
					    '</tpl>'+
				    '</tpl>',
				    '<div style="clear:left"></div>',
				    '</dl>',
			      '</div>', {
			        checkIsUsed: function(v, l) {
			    	  return v == 'true';
		        	},
	        	    checkIsSelected: function(v, l) {
			    	  return v == 'true';
		        	},
		        	shorten: function(text){
		                return Ext.util.Format.ellipsis(text,35,false);
		            },
		            shortenTitle: function(text){
		                return Ext.util.Format.ellipsis(text,18,false);
		            }
				 }
				);

		this.tpl = tpl;

		Sbi.trace("[DatasetsBrowserView.initTemplate]: OUT");
	}


	, getDatasetsTemplate : function(){
		var img = Ext.BLANK_IMAGE_URL ;

		var classImg = ' class="measure-detail-dataset" ';

		var author = LN('sbi.generic.author');
		var created = LN('sbi.generic.creationdate');
//		var changed = LN('sbi.ds.changedon');

		var datasetTpl = ''+
		'<div class="box-container">'+
	        '<div id="box-figure-{label}" class="box-figure">'+
//				'<img  align="center" src="' + img + '" '+ classImg+'" + ext:qtip="<b>{views}</b><br/>{summary}"></img>' +
				'<img  align="center" src="' + img + '" '+ classImg +'"></img>' +
			'</div>'+ //box-figure
			'<tpl if="this.checkIsUsed(isUsed, label) == true">'+
				'<div id="box-text-{label}" title="{name}" class="box-text box-text-select">'+
					'<h3>{[this.shortenTitle(values.name)]}</h3>'+
					'<p>{[this.shorten(values.description)]}</p>'+
//					'<p>{description}</p>'+
					'<p><b>'+author+':</b> {owner}</p>'+
					'<p><b>'+created+':</b> {dateIn}</p>'+
				'</div>'+
			'</tpl>'+
	        '<tpl if="this.checkIsUsed(isUsed, label) == false">'+
		        '<div id="box-text-{label}" title="{name}" class="box-text">'+
					'<h3>{[this.shortenTitle(values.name)]}</h3>'+
						'<p>{[this.shorten(values.description)]}</p>'+
//					'<p>{description}</p>'+
					'<p><b>'+author+':</b> {owner}</p>'+
					'<p class="modified"><b>'+created+':</b> {dateIn}</p>'+
				'</div>'+
			'</tpl>'+
		'</div>';

		return datasetTpl;
	}


	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

	, selectDatasetComponent: function(datasetLabel) {
		Sbi.trace("[DatasetsBrowserView.selectDatasetComponent]: IN");
		if(this.rendered === true) {
			var el = Ext.get(datasetLabel);
	 		if (el) {
	 			Sbi.trace("[DatasetsBrowserView.selectDatasetComponent]: class before [" + el.dom.className + "]");
	 			// the  class selectboxOrange is used to mark used datatset. a selected dataset is of course also a used dataset but we
	 			// want to mark it only as selected so we remove the undesired class
	 			el.dom.className = el.dom.className.replace( /(?:^|\s)selectboxOrange(?!\S)/g , '' );
	 			el.dom.className += ' selectboxBlu ';
	 			Sbi.trace("[DatasetsBrowserView.selectDatasetComponent]: class after [" + el.dom.className + "]");
	 		} else {
	 			Sbi.trace("[DatasetsBrowserView.selectDatasetComponent]: Impossible to find dataset [" + datasetLabel +"]");
	 		}

	 		var elText = Ext.get('box-text-' + datasetLabel);
		 	if (elText) {
		 		Sbi.trace("[DatasetsBrowserView.selectDatasetComponent]: class before [" + elText.dom.className + "]");
		 		elText.dom.className += ' box-text-select ';
		 		Sbi.trace("[DatasetsBrowserView.selectDatasetComponent]: class after [" + elText.dom.className + "]");
		 	}

		 	Sbi.trace("[DatasetsBrowserView.selectDatasetComponent]: dataset [" + datasetLabel +"] succesfully selected");
		}
		Sbi.trace("[DatasetsBrowserView.selectDatasetComponent]: OUT");
	}

	, unselectDatasetComponent: function(datasetLabel) {
		Sbi.trace("[DatasetsBrowserView.selectDatasetComponent]: IN");
		if(this.rendered === true) {
			var el = Ext.get(datasetLabel);
	 		if (el) {
	 			Sbi.trace("[DatasetsBrowserView.unselectDatasetComponent]: class before [" + el.dom.className + "]");
	 			el.dom.className = el.dom.className.replace( /(?:^|\s)selectboxBlu(?!\S)/g , '' ); //remove active class
	 			if(this.isUsed(datasetLabel)) {
	 				el.dom.className += ' selectboxOrange ';
	 			}
	 			Sbi.trace("[DatasetsBrowserView.unselectDatasetComponent]: class after [" + el.dom.className + "]");
	 		} else {
	 			Sbi.trace("[DatasetsBrowserView.selectDatasetComponent]: Impossible to find dataset [" + datasetLabel +"]");
	 		}

	 		var elText = Ext.get('box-text-' + datasetLabel);
		 	if (elText) {
		 		Sbi.trace("[DatasetsBrowserView.unselectDatasetComponent]: class before [" + elText.dom.className + "]");
		 		elText.dom.className = elText.dom.className.replace( /(?:^|\s)box-text-select(?!\S)/g , '' ); //remove active class
		 		Sbi.trace("[DatasetsBrowserView.unselectDatasetComponent]: class after [" + elText.dom.className + "]");
		 	}

		 	Sbi.trace("[DatasetsBrowserView.selectDatasetComponent]: dataset [" + datasetLabel +"] succesfully unselected");
		}
		Sbi.trace("[DatasetsBrowserView.selectDatasetComponent]: OUT");
	}
});
