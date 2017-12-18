/* =============================================================================
* Added by Chiara Chiarelli (August 2010)
* In Ext 3.1.1 the method getCount didn't control if this.data existed
============================================================================= */
Ext.override(Ext.data.Store, {
	getCount : function(){
		if(this.data!==null && this.data!== undefined){
			return this.data.length || 0;
		}else{
			return 0;
		}
	}
});

Ext.override(Ext.grid.PropertyStore, {
	onUpdate : function(ds, record, type){
    if(type == Ext.data.Record.EDIT){
        var v = record.data.value;
        var oldValue = record.modified.value;
        if(this.grid.fireEvent('beforepropertychange', this.source, record.id, v, oldValue) !== false){
            if(this.source){
        	   this.source[record.id] = v;
            }
            record.commit();
            this.grid.fireEvent('propertychange', this.source, record.id, v, oldValue);
        }else{
            record.reject();
        }
    }
}
});


/* =============================================================================
* Added by Davide Zerbetto (July 2013)
* IE9 does not support createContextualFragment function
* See https://spagobi.eng.it/jira/browse/SPAGOBI-1266#comment-33990
* See http://www.marcolecce.com/blog/2011/05/19/sencha-extjs-createcontextualfragment-non-supportato-in-ie-9/
============================================================================= */
if (Ext.isIE9 && (typeof Range !== 'undefined') && !Range.prototype.createContextualFragment) {
	  Range.prototype.createContextualFragment = function(html) {
	    var frag = document.createDocumentFragment(),
	    div = document.createElement('div');
	    frag.appendChild(div);
	    div.outerHTML = html;
	    return frag;
	  };
}