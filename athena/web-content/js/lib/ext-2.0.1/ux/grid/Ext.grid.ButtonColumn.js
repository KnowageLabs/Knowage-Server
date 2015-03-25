// JavaScript Document
Ext.grid.ButtonColumn = function(config){
    Ext.apply(this, config);
    if(!this.id){
        this.id = Ext.id();
    }
    this.renderer = this.renderer.createDelegate(this);
};

Ext.grid.ButtonColumn.prototype = {
    init : function(grid){
        this.grid = grid;
        this.grid.on('render', function(){
            var view = this.grid.getView();
            view.mainBody.on('click', this.onClick, this);
        }, this);
    },

    onClick : function(e, t){
        if(t.className && t.className.indexOf('x-mybutton-'+this.id) != -1){
            e.stopEvent();
            this.clickHandler(e,t);
            /*
            var index = this.grid.getView().findRowIndex(t);
            var record = this.grid.store.getAt(index);
            this.grid.store.remove(record);
            */
        }
    },
    clickHandler:function(e, t){
      var index = this.grid.getView().findRowIndex(t);
      var record = this.grid.store.getAt(index);
      alert('index: ' + index + '; column-value: ' + record.data[this.dataIndex]);
    },

    renderer : function(v, p, record){
        return '<center><img class="x-mybutton-'+this.id+'" width="13px" height="13px" src="' + this.imgSrc + '"/></center>';
    }
};