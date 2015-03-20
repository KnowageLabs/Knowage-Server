/*!
 * Ext JS Library 3.3.1
 * Project with extension: http://code.google.com/p/extjsfilemanager/
 * license: http://www.opensource.org/licenses/bsd-license.php
 */
	
Ext.namespace('Ext.ux');

Ext.ux.ImageEditor = function(cfg) {
    Ext.apply(this, cfg);
}

Ext.ux.ImageEditor.prototype = {
    init: function(panel) {
        this.transformations = {
            rotate: 0,
            zoom: 100
        };

        this.panel = panel;
        this.src = panel.client.src;
        this.children = {};

        var tbar = panel.getTopToolbar();
        
        var zoomInButton = new Ext.Button({
    		text:'Zoom In' ,
    		//tooltip:'Increases dimensions',
    		tooltip:LN('Sbi.office.zoomIn'),
    		handler: function(){
            	this.zoomIn();
            }
            , scope: this
    	});
    	
    	var zoomOutButton = new Ext.Button({
    		text: 'Zoom Out',
    		//tooltip:'Decrements dimensions',
    		tooltip:LN('Sbi.office.zoomOut'),
    		handler: function() {
    			this.zoomOut();            	
            }
            , scope: this
    	});
        
        tbar.push(zoomInButton);
        tbar.push(zoomOutButton);
        var tbFill = new Ext.Toolbar.Fill();
        tbar.push(tbFill);
    },

    zoomIn: function() {
        this.zoom(10);
    },

    zoomOut: function() {
        this.zoom(-10);
    },

    rotate: function() {
        var transformations = this.transformations;
        transformations.rotate += 90;

        var size = this.getSize();
        this.size = { width: size.height, height: size.width }
        while (transformations.rotate > 360) {
            transformations.rotate -= 360;
        }
        this.loadImage();
    },

    loadImage: function() {       
        var el = this.getEl();
        var dom = el.dom;
        dom.src = "s.gif";
        this.zoom(0);

        dom.src = this.src + "?" + Ext.urlEncode(this.transformations) + "&" + new Date().getTime();
    },

    getEl: function() {
        return this.panel.client;
    },

    canZoomIn: function() {
        return this.transformations.zoom < 100;
    },

    canZoomOut: function() {
        return this.transformations.zoom > 10;
    },

    getZoomSize: function() {
        var size = this.getSize(), zoom = this.transformations.zoom;
        return { width: size.width * zoom / 100, height: size.height * zoom / 100 };
    },

    getSize: function() {
        if (!this.size) {
            this.size = this.getEl().getSize();
        }
        return this.size;
    },

    zoom: function(increment) {
        var el = this.getEl();
        this.transformations.zoom += increment;
        var newSize = this.getZoomSize();

        el.setSize(newSize.width, newSize.height);

        //this.children.zoomOut.setDisabled(!this.canZoomOut());
        //this.children.zoomIn.setDisabled(!this.canZoomIn());
    }
    
};