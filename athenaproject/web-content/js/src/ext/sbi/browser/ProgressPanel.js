/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  

/**
 * 
 * @author Giulio Gavardi (giulio.gavardi@eng.it)
 */

Ext.ns("Sbi.browser");


Sbi.browser.ProgressPanel = function(config) { 
	
	var defaultSettings = {
			bodyStyle:'padding:6px 6px 6px 6px; background-color:#FFFFFF'
			, autoScroll: true			
	}
	
	var c = Ext.apply(defaultSettings, config || {} );

	this.services = this.services || new Array();
    this.services['GetMassiveExportProgressStatus'] = this.services['GetMassiveExportProgressStatus'] || Sbi.config.serviceRegistry.getServiceUrl({
    			serviceName: 'GET_MASSIVE_EXPORT_PROGRESS_STATUS'
    			, baseParams: new Object()
    			});
    this.services['DownloadMassiveExportZip'] = this.services['DownloadMassiveExportZip'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'DOWNLOAD_MASSIVE_EXPORT_ZIP'
		, baseParams: new Object()
		});
    this.services['DeleteMassiveExportZip'] = this.services['DeleteMassiveExportZip'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'DELETE_MASSIVE_EXPORT_ZIP'
		, baseParams: new Object()
		});
    
    // keep track of current works going on (as from database)
	this.progressGroup = new Object();
	this.currentWorks = new Object();
	this.downloadButtonPanels = new Object();
	this.deleteButtonPanels = new Object();
	this.toBeDeleted = new Array();

	this.initPanels();

	//this.addEvents();
	c = Ext.apply(c, {
		items: [this.startedPanel, this.downloadedPanel] //, this.scheduledPanel]	
	});
	Sbi.browser.ProgressPanel.superclass.constructor.call(this, c);  
	
	this.on('expand', this.onXExpand , this);	
	this.on('collapse', this.onXCollapse, this);

	this.progressCounter = 0;
	this.buttonCounter = 0;
	
	// Start cycle
	this.serverPooling();

};

Ext.extend(Sbi.browser.ProgressPanel, Ext.Panel, {
    
	startedPanel : null
	, downloadedPanel : null
	, scheduledPanel : null
	, progressGroup : null
	, services : null
	, xexpanded : false
	, currentWorks : null
	, toBeDeleted : null
	, downloadButtonPanels : null
	, deleteButtonPanels : null
	, canAccess: true
	, progressCounter: null
	, buttonCounter : null
	, progressEmptyPanel : null
	, buttonEmptyPanel : null
	
		// Progress Bar creation
	, initPanels : function(){
		this.startedPanel = new Ext.Panel({  
			title: LN('Sbi.browser.ProgressPanel.startedExport'),
			layout: 'anchor',  
			scope: this,
			height: 120,
			autoWidth: true,
			defaults: {border:false},
			autoScroll: true
		});
		
		this.progressEmptyPanel = new Ext.Panel({  
			scope: this,
			height: 20,
			autoWidth: true,
			html : '<i>'+LN('Sbi.browser.ProgressPanel.noProgress')+'</i>',
			defaults: {border:false}
		});
		this.startedPanel.add(this.progressEmptyPanel);
		this.progressEmptyPanel.hide();
			
		
		this.downloadedPanel = new Ext.Panel({  
			title: LN('Sbi.browser.ProgressPanel.completedExport'),
			layout: 'column',
			scope: this,
			height: 200,
			collapsible: true,
			defaults: {border:false},
			autoScroll: true
		});
		
		// create and hide two empty case panel
		this.buttonEmptyPanel = new Ext.Panel({  
			//layout: 'anchor',  
			scope: this,
			height: 20,
			autoWidth: true,
			html : '<i>'+LN('Sbi.browser.ProgressPanel.noDownload')+'</i>',
			defaults: {border:false}
		});
		this.downloadedPanel.add(this.buttonEmptyPanel);
		this.buttonEmptyPanel.hide();
		
		
		this.scheduledPanel = new Ext.Panel({  
			title: LN('Sbi.browser.ProgressPanel.scheduledExport'),
			layout: 'anchor', 
			scope: this,
			height: 200,
			collapsible: true,
			autoWidth: true,
			defaults: {border:false}
		});
	}

	, serverPooling: function(){
		// for better performances wanted to draw bars only when expanded, but execution must go on aniway
		// true means to cycle
		if(this.canAccess==true && this.xexpanded){		
			this.canAccess = false;
			this.updateProgressStatus(true);
			this.doLayout();
			this.canAccess = true;
		} else{
			var that = this;
			setTimeout(function(){that.serverPooling()}, 10000);
		}
	}

	, updateProgressStatus: function(pooling){ 

			// search for pending thrread in database
		Ext.Ajax.request({
      	        url: this.services['GetMassiveExportProgressStatus'],
      	        params: {
      	        	//MESSAGE : 'STARTED'
      	        },
      	        success : function(response, options){
      		  	if(response !== undefined) {   
      	      		if(response.responseText !== undefined) {
      	      			var content = Ext.util.JSON.decode( response.responseText );
      	      			if(content !== undefined) {
      	      				// get array
      	      				var worksFound = new Object();
      	      				for(i = 0; i< content.length;i++){
      	      					var prog = content[i];
      	      					var functCd = prog.functCd;
      	      					var randomKey= prog.randomKey;
      	      					var type = prog.type;
      	      					this.handleProgressThreadResult(prog, functCd, randomKey, worksFound, type);
      	      				}
      	      				// clean work no more present
      	      				this.cleanNoMorePresentWork(worksFound);
        	      		}
          	      	} 
      	      		this.checkEmptyPanels();
      	      		
      	      		// only if called from poolingServer
      	      		if(pooling == true){
      	      			// if(expanded timeout is faster, else take more time before next call
      	      			var that = this;
      	      			if(this.xexpanded== true){
      	      				setTimeout(function(){that.serverPooling()}, 5000);
      	      			} else{ // wait longer if not expanded
      	      				setTimeout(function(){that.serverPooling()}, 20000);
      	      			}
      	      		}
      		  	} else {
      		  		Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
          	    }
      	       },
      	       scope: this,
      	       failure: Sbi.exception.ExceptionHandler.handleFailure      
      	   });
	}
	
	, handleProgressThreadResult : function(prog, functCd, randomKey, worksFound, type){	
		worksFound[functCd+''+randomKey]= true;
		// value progress thread status
	
		var progressBar = this.progressGroup[functCd+''+randomKey];
		// if in download state make download and delete work
		if(prog.message && prog.message=='DOWNLOAD'){
			this.createDownloadForm(progressBar, functCd, randomKey, prog.progressThreadId, type);
		} else if(prog.message && ( prog.message=='STARTED' || prog.message=='PREPARED')){
			// check if progress exist then update otherwise create
			var partial = prog.partial;
			var total = prog.total;
			this.handleStartedProgressThreadResult(progressBar, functCd, randomKey, partial, total);
		}
	}
	
	, createDownloadForm: function(progressBar, functCd, randomKey, progressThreadId, type){
		
		var urlToCall = Sbi.config.serviceRegistry.getBaseUrlStr({});	
		urlToCall += '?ACTION_NAME=DOWNLOAD_MASSIVE_EXPORT_ZIP';
		urlToCall += '&FUNCT_CD='+functCd;
		urlToCall += '&RANDOM_KEY='+randomKey;
		urlToCall += '&PROGRESS_THREAD_ID='+progressThreadId;
		urlToCall += '&PROGRESS_THREAD_TYPE='+type;
		
		if(!progressBar){
			// do nothings
		} else{
			var msg = functCd + ' - ' + randomKey
			progressBar.updateProgress(1, msg);
		}
    	
		// delete the progressBar
		if(progressBar){
			this.deleteWork(functCd + '' + randomKey);
		}
    	
	    if(this.downloadButtonPanels[functCd+randomKey]){
	    	// do nothings
	    } else{
	    	// create panel and put inside button
	    	this.downloadButtonPanels[functCd+randomKey] = new Ext.Panel({  
				scope: this,
				autoWidth: true,
				defaults: {border:false}
			});

	    	var tooltipText = '' + functCd + '-' + randomKey;
	    	// remove milliseconds
	    	tooltipText = tooltipText.substring(0, (tooltipText.length-7));
	    	
	    	var buttonText = Ext.util.Format.ellipsis(tooltipText, 40);
	    	var button = new Ext.Button({
	    		id: functCd+randomKey+'download',
	    		text: buttonText,
	    		tooltip: tooltipText,
	    		disabled: false,
	    		scope: this,
	    		disabled: true,
	    		handler: function(){
	    			window.open(urlToCall,'name','resizable=1,height=750,width=1000');
				}
			});
	    	button.enable();
	    	this.downloadButtonPanels[functCd+randomKey].add(button);
	    	this.buttonCounter++;
	    }
	 
	    this.downloadedPanel.add(this.downloadButtonPanels[functCd+randomKey]);
	   
	    this.createDeleteForm(functCd, randomKey, progressThreadId, type);

	    this.downloadedPanel.doLayout();
	    this.doLayout();
	}
	
	, handleStartedProgressThreadResult: function(progressBar, functCd, randomKey, partial, total){
		// if progress bar has already been created update it otherwise create
		if(progressBar){
			if(progressBar.rendered){
				progressBar.updateProgress(partial/total, LN('Sbi.browser.ProgressPanel.exporting')+' '+functCd+' '+LN('Sbi.browser.ProgressPanel.document')+' ' + partial + ' '+LN('Sbi.browser.ProgressPanel.of')+' '+total+'...');
				this.doLayout();
			}
		}
		else{
			// create: no progress bar store with functCd
			this.createProgressBar(functCd, randomKey);

		this.progressGroup[functCd+''+randomKey].on('render', function() {
		    //alert('212 - '+functCd+''+randomKey); 
				this.progressGroup[functCd+''+randomKey].updateProgress(partial/total, LN('Sbi.browser.ProgressPanel.exporting')+' '+functCd+' '+LN('Sbi.browser.ProgressPanel.document')+' ' + partial + ' '+LN('Sbi.browser.ProgressPanel.of')+' '+total+'...');
			this.doLayout();
		} , this );
			
		}	
	}
	
	, createProgressBar : function(functCd, randomKey) {
		var progressBar = new Ext.ProgressBar({
            text: LN('Sbi.browser.ProgressPanel.initializing')+'...'+functCd+' - '+randomKey
         });
        // add progress bar to array
        this.progressGroup[functCd+''+randomKey] = progressBar;
        this.progressCounter++;
    	this.startedPanel.add(progressBar);
    	this.startedPanel.doLayout();
    	this.currentWorks[functCd+''+randomKey] = true;
		this.progressGroup[functCd+''+randomKey].on('render', function() {
			this.doLayout();
		} , this );
	}
	

	

	, cleanNoMorePresentWork : function(worksFound){
		for (var key in this.currentWorks) {
			var obj = this.currentWorks[key];
			if(obj && obj == true){
				// if it is not among works found delete it
				if(!(worksFound[key] && worksFound[key]==true)){
					this.deleteWork(key); 
				}
			}
		}
	}
	, deleteWork : function(key){
			if(this.progressGroup[key]){
				if(this.progressGroup[key].rendered){
					this.progressGroup[key].updateProgress(1, key+': '+LN('Sbi.browser.ProgressPanel.finished'));
				}
				this.toBeDeleted.push(this.progressGroup[key]);
				var that = this;
				// destroy bar only after a while
				setTimeout(function(){
					for(i=0;i<that.toBeDeleted.length;i++){
						var progBar = 	that.toBeDeleted[i];
              	        progBar.reset(true);
						progBar.destroy();
						that.progressCounter = that.progressCounter-1;
					}
					that.doLayout();
					that.toBeDeleted = new Array();
				}, 5000);
				}
			this.progressGroup[key] = null;	
			this.currentWorks[key]=null;
				this.doLayout();
	}
	
	
	, createDeleteForm: function(functCd, randomKey, progressThreadId, type){
	   
		if(this.deleteButtonPanels[functCd+randomKey]){
	    }
	    else{
	    	
	    	this.deleteButtonPanels[functCd+randomKey] = new Ext.Panel({  
				//title: 'Started Export',
				//layout: 'fit',  
				scope: this,
				//height: 120,
				autoWidth: true,
				//columnWidth : 0.5,
				defaults: {border:false}
			});
	    	
	    	
	    	
	    	var button = new Ext.Button({
	    		id: functCd+randomKey+'delete',
	    		//text: 'delete '+functCd+'-'+randomKey,
				iconCls: 'icon-clear',
	    		disabled: false,
	    		scope: this,
	    		disabled: true,
	    		handler: this.deleteButtonHandler.createDelegate(this, [functCd, randomKey, progressThreadId, type], 0 )
			});
	    	button.enable();
	    	this.deleteButtonPanels[functCd+randomKey].add(button);
	    }

	    this.downloadedPanel.add(this.deleteButtonPanels[functCd+randomKey]);
	
	}
	
	,
	deleteButtonHandler: function(functCd, randomKey, progressThreadId, type) {
		Ext.MessageBox.confirm(
			LN('sbi.generic.pleaseConfirm')
			, LN('sbi.generic.confirmDelete')
            , function(btn, text) {
                if ( btn == 'yes' ) {
                	this.doDeleteItem(functCd, randomKey, progressThreadId, type);
                }
			}
			, this
		);
	}
	
	,
	doDeleteItem : function(functCd, randomKey, progressThreadId, type) {
		
		var pars = {
			FUNCT_CD: functCd
			, RANDOM_KEY: randomKey
			, PROGRESS_THREAD_ID: progressThreadId 
			, PROGRESS_THREAD_TYPE: type
		};
		
		Ext.Ajax.request({
	        url: this.services['DeleteMassiveExportZip'],
	        params: pars,
	        success : function(response, options) {
				if(response !== undefined) {   
	    			this.downloadButtonPanels[functCd+randomKey].hide();
	    			this.downloadButtonPanels[functCd+randomKey].destroy();
	    			this.downloadButtonPanels[functCd+randomKey] = null;
					this.deleteButtonPanels[functCd+randomKey].hide();
	    			this.deleteButtonPanels[functCd+randomKey].destroy();
	    			this.deleteButtonPanels[functCd+randomKey] = null;
	    			this.buttonCounter = this.buttonCounter -1;
				}
			},
	        scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure      
		});	
	}

	, checkEmptyPanels: function(){
		if(this.progressCounter<1){
			if(this.progressEmptyPanel){
				this.progressEmptyPanel.show();
			}
		}
		else{
			this.progressEmptyPanel.hide();
		}	
		
		if(this.buttonCounter<1){
			if(this.buttonEmptyPanel){
				this.buttonEmptyPanel.show();
			}
		}
		else{
			this.buttonEmptyPanel.hide();
		}	
	}
	
	, onXExpand: function () {
		this.xexpanded = true;
		// false because is executing only for one time because of expansion
		if(this.canAccess == true){
			this.canAccess = false;
			this.updateProgressStatus(false); // NO MORE CALL to avoid synchronization problem
	
			this.canAccess=true;
		}
		this.doLayout();
	}
	
	, onXCollapse: function () {
		this.xexpanded = false;
	}
	
    
});

