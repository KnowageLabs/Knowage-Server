Ext.define('Ext.ux.RadioColumn', {
    extend: 'Ext.grid.column.Column',
    alias: 'widget.radiocolumn',
    checkededRecordIndex:null, //checked record index 
    
    constructor: function() {
        this.addEvents(
            /**
             * @event checkchange
             * Fires when the checked state of a row changes
             * @param {Ext.ux.RadioColumn} this
             * @param {Number} rowIndex The row index
             * @param {Boolean} checked True if the box is checked
             */
            'checkchange'
        );
        this.callParent(arguments);
    },

    /**
     * @private
     * Process and refire events routed from the GridView's processEvent method.
     */
    processEvent: function(type, view, cell, recordIndex, cellIndex, e) {
        if (type == 'mousedown' || (type == 'keydown' && (e.getKey() == e.ENTER || e.getKey() == e.SPACE))) {
            var record = view.panel.store.getAt(recordIndex),
                dataIndex = this.dataIndex,
                checked = !record.get(dataIndex);
            
            if(checked){//if a record has been checked we should deselect the previous one
            	if(this.checkededRecordIndex!=null){
            		var previousCheckedRecord=view.panel.store.getAt(this.checkededRecordIndex);
            		previousCheckedRecord.set(dataIndex, false);
            	}else{
            		for(var i=0; i<view.panel.store.totalCount; i++){
            			 var recordToClean = view.panel.store.getAt(i);
            			 recordToClean.set(dataIndex, false);
            		}
            	}
            	this.checkededRecordIndex = recordIndex;
            }
            
            
            record.set(dataIndex, checked);
            this.fireEvent('checkchange', this, recordIndex, checked);
            // cancel selection.
            return false;
        } else {
            return this.callParent(arguments);
        }
    },

    // Note: class names are not placed on the prototype bc renderer scope
    // is not in the header.
    renderer : function(value, metaData, record, rowIndex, colIndex, store, view){
        var cssPrefix = Ext.baseCSSPrefix;
        cls = [cssPrefix + 'form-radio'];
        if (value) {
            cls.push('radio-column-checked');
        }else{
			cls.push('radio-column-unchecked');
		}
        var marginleft=2;
        if(this.columns && this.columns.length>colIndex && this.columns[colIndex].width){
        	marginleft = ((this.columns[colIndex].width-13)/2)-6;
        	if(marginleft<0){
        		marginleft = 2;
        	}
        }
        return '<div style="margin-left:'+marginleft+'" class="' + cls.join(' ') + '">&#160;</div>';
    }
});