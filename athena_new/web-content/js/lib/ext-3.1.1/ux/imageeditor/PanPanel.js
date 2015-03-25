/*!
 * Ext JS Library 3.3.1
 * Project with extension: http://code.google.com/p/extjsfilemanager/
 * license: http://www.opensource.org/licenses/bsd-license.php
 */

/**
 * @class Ext.ux.PanPanel
 * @extends Ext.Panel
 * 
 * @xtype panel
 */

Ext.namespace('Ext.ux');

Ext.ux.PanPanel = Ext.extend(Ext.Panel, {
    constructor: function(config) {
        //config.autoScroll = false;
        Ext.ux.PanPanel.superclass.constructor.apply(this, arguments);
    },

    onRender: function() {
        Ext.ux.PanPanel.superclass.onRender.apply(this, arguments);
        this.body.appendChild(this.client);
        this.client = Ext.get(this.client);
        this.client.on('mousedown', this.onMouseDown, this);
        this.client.setStyle('cursor', 'move');
    },

    onMouseDown: function(e) {
        e.stopEvent();
        this.mouseX = e.getPageX();
        this.mouseY = e.getPageY();
        Ext.getBody().on('mousemove', this.onMouseMove, this);
        Ext.getDoc().on('mouseup', this.onMouseUp, this);
    },

    onMouseMove: function(e) {
        e.stopEvent();
        var x = e.getPageX();
        var y = e.getPageY();
        if (e.within(this.body)) {
            var xDelta = x - this.mouseX;
            var yDelta = y - this.mouseY;
            this.body.dom.scrollLeft -= xDelta;
            this.body.dom.scrollTop -= yDelta;
        }
        this.mouseX = x;
        this.mouseY = y;
    },

    onMouseUp: function(e) {
        Ext.getBody().un('mousemove', this.onMouseMove, this);
        Ext.getDoc().un('mouseup', this.onMouseUp, this);
    }
});

Ext.reg('panpanel', Ext.ux.PanPanel);