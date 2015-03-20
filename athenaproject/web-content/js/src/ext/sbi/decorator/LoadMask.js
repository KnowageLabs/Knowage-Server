/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  

/**
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */

Ext.ns("Sbi.decorator");

Sbi.decorator.LoadMask = function(el, config){
    this.el = Ext.get(el);
    Ext.apply(this, config);
    if(this.store){
        this.store.on('beforeload', this.onBeforeLoad, this);
        this.store.on('load', this.onLoad, this);
        this.store.on('loadexception', this.onLoad, this);
        this.removeMask = Ext.value(this.removeMask, false);
    }else{
        var um = this.el.getUpdater();
        um.showLoadIndicator = false; // disable the default indicator
        um.on('beforeupdate', this.onBeforeLoad, this);
        um.on('update', this.onLoad, this);
        um.on('failure', this.onLoad, this);
        this.removeMask = Ext.value(this.removeMask, true);
    }
};

Sbi.decorator.LoadMask.prototype = {
		
	active: false, 
    /**
     * @cfg {Boolean} removeMask
     * True to create a single-use mask that is automatically destroyed after loading (useful for page loads),
     * False to persist the mask element reference for multiple uses (e.g., for paged data widgets).  Defaults to false.
     */
    /**
     * @cfg {String} msg
     * The text to display in a centered loading message box (defaults to 'Loading...')
     */
    msg : 'Loading...',
    /**
     * @cfg {String} msgCls
     * The CSS class to apply to the loading message element (defaults to "x-mask-loading")
     */
    msgCls : 'x-mask-loading',

    /**
     * Read-only. True if the mask is currently disabled so that it will not be displayed (defaults to false)
     * @type Boolean
     */
    disabled: false,

    /**
     * Disables the mask to prevent it from being displayed
     */
    disable : function(){
       this.disabled = true;
    },

    /**
     * Enables the mask so that it can be displayed
     */
    enable : function(){
        this.disabled = false;
    },

    // private
    onLoad : function(){
        //alert('onLoad');
        this.unmask(this.el, this.removeMask);
        //this.el.unmask(this.removeMask);
    },

    // private
    onBeforeLoad : function(){
        if(!this.disabled){
            this.mask(this.el, this.msg, this.msgCls);
        }
    },

    show: function(){
    	if(this.active === false) {
    		this.onBeforeLoad();
            this.active = true;
    	}
        
    },

    hide: function(){
    	if(this.active === true) {
    		this.onLoad();  
    		this.active = false;
    	}
    },
    
    mask : function(el, msg, msgCls) {
      if(el.getStyle("position") == "static"){
            el.setStyle("position", "relative");
        }
        if(el._maskMsg){
            el._maskMsg.remove();
        }
        if(el._mask){
            el._mask.remove();
        }

        el._mask = Ext.DomHelper.append(el.dom, {cls:"sbi-el-mask"}, true);

        el.addClass("x-masked");
        el._mask.setDisplayed(true);
        if(typeof msg == 'string'){
            el._maskMsg = Ext.DomHelper.append(el.dom, {cls:"sbi-el-mask-msg", cn:{tag:'div'}}, true);
            var mm = el._maskMsg;
            mm.dom.className = msgCls ? "sbi-el-mask-msg " + msgCls : "sbi-el-mask-msg";
            mm.dom.firstChild.innerHTML = msg;
            mm.setDisplayed(true);
            mm.center(el);
        }
        if(Ext.isIE && !(Ext.isIE7 && Ext.isStrict) && el.getStyle('height') == 'auto'){ // ie will not expand full height automatically
            el._mask.setSize(el.dom.clientWidth, el.getHeight());
        }
        return el._mask;
    },
    
    unmask : function(el){
        if(el._mask){
            if(el._maskMsg){
                el._maskMsg.remove();
                delete el._maskMsg;
            }
            el._mask.remove();
            delete el._mask;
        }
        el.removeClass("x-masked");
    },

    // private
    destroy : function(){
        if(this.store){
            this.store.un('beforeload', this.onBeforeLoad, this);
            this.store.un('load', this.onLoad, this);
            this.store.un('loadexception', this.onLoad, this);
        }else{
            var um = this.el.getUpdater();
            um.un('beforeupdate', this.onBeforeLoad, this);
            um.un('update', this.onLoad, this);
            um.un('failure', this.onLoad, this);
        }
    }
};

