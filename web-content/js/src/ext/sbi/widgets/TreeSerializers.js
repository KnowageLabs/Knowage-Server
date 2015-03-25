/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
 /*global Ext*/

/**
 * @class Ext.tree.TreeSerializer
 * A base class for implementations which provide serialization of an
 * {@link Ext.tree.TreePanel}.
 * <p>
 * Implementations must provide a toString method which returns the serialized
 * representation of the tree.
 * 
 * @constructor
 * @param {TreePanel} tree
 * @param {Object} config
 */
Ext.ns("Sbi.widgets");

Sbi.widgets.TreeSerializer = function(tree, config){
    if (typeof this.toString !== 'function') {
    	throw 'Sbi.widgets.TreeSerializer implementation does not implement toString()';
    }
	this.tree = tree;

	if (this.attributeFilter) {
		this.attributeFilter = this.attributeFilter.createInterceptor(this.defaultAttributeFilter);
	} else {
		this.attributeFilter = this.defaultAttributeFilter;
	}
	if (this.nodeFilter) {
		this.nodeFilter = this.nodeFilter.createInterceptor(this.defaultNodeFilter);
	} else {
		this.nodeFilter = this.defaultNodeFilter;
	}
	
	Ext.apply(this, config);

};

Sbi.widgets.TreeSerializer.prototype = {

	/*
	 * @cfg nodeFilter {Function} (optional) A function, which when passed the node, returns true or false to include
	 * or exclude the node.
	 */
	 
	/*
	 * @cfg attributeFilter {Function} (optional) A function, which when passed an attribute name, and an attribute value,
	 * returns true or false to include or exclude the attribute.
	 */
	 
	/*
	 * @cfg attributeMap {Array} (Optional) An associative array mapping Node attribute names to XML attribute names.
	 */

	/* @private
	 * Array of node attributes to ignore.
	 */
    standardAttributes: ["expanded", "allowDrag", "allowDrop", "disabled", "icon",
    "cls", "iconCls", "href", "hrefTarget", "qtip", "singleClickExpand", "uiProvider", "allowChildren", "expandable", "loader", "label"],
    

	/** @private
	 * Default attribute filter.
	 * Rejects functions and standard attributes.
	 */
	defaultAttributeFilter: function(attName, attValue) {
		return	(typeof attValue != 'function') &&
				(this.standardAttributes.indexOf(attName) == -1);
	},

	/** @private
	 * Default node filter.
	 * Accepts all nodes.
	 */
	defaultNodeFilter: function(node) {
		return true;
	}
};

/**
 * @class Sbi.widgets.XmlTreeSerializer
 * An implementation of Sbi.widgets.TreeSerializer which serializes an
 * {@link Ext.tree.TreePanel} to an XML string.
 */
Sbi.widgets.XmlTreeSerializer = function(tree, config){
	Sbi.widgets.XmlTreeSerializer.superclass.constructor.apply(this, arguments);
};

Ext.extend(Sbi.widgets.XmlTreeSerializer, Sbi.widgets.TreeSerializer, {
	/**
	 * Returns a string of XML that represents the tree
	 * @return {String}
	 */
	toString: function(){
		return '<?xml version="1.0"?><tree>' +
			this.nodeToString(this.tree.getRootNode()) + '</tree>';
	},

	/**
	 * Returns a string of XML that represents the node
	 * @param {Object} node The node to serialize
	 * @return {String}
	 */
	nodeToString: function(node){
	    if (!this.nodeFilter(node)) {
	        return '';
	    }
	    var result = '<node';
	    
	    /**
	     *  This doesn't appear necessary. Since the iteration below will include id, 
	     *  this block simply includes it twice
	     
	    if (this.attributeFilter("id", node.id)) {
	        result += ' id="' + node.id + '"';
	    }
	    ***/

//		Add all user-added attributes unless rejected by the attributeFilter.
	    for(var key in node.attributes) {
	        if (this.attributeFilter(key, node.attributes[key])) {
		        result += ' ' + (this.attributeMap ? (this.attributeMap[key] || key) : key) + '="' + node.attributes[key] + '"';
		    }
	    }

//		Add child nodes if any
	    var children = node.childNodes;
	    var clen = children.length;
	    if(clen == 0){
	        result += '/>';
	    }else{
	        result += '>';
	        for(var i = 0; i < clen; i++){
	            result += this.nodeToString(children[i]);
	        }
	        result += '</node>';
	    }
	    return result;
	}

});

/**
 * @class Sbi.widgets.JsonTreeSerializer
 * An implementation of Sbi.widgets.TreeSerializer which serializes an
 * {@link Ext.tree.TreePanel} to a Json string.
 */
Sbi.widgets.JsonTreeSerializer = function(tree, config){
	Sbi.widgets.JsonTreeSerializer.superclass.constructor.apply(this, arguments);
};

Ext.extend(Sbi.widgets.JsonTreeSerializer, Sbi.widgets.TreeSerializer, {

	/**
	 * Returns a string of Json that represents the tree
	 * @return {String}
	 */
	toString: function(){
	      return this.nodeToString(this.tree.getRootNode());
	},

	/**
	 * Returns a string of Json that represents the node
	 * @param {Object} node The node to serialize
	 */
	nodeToString: function(node){

//		Exclude nodes based on caller-supplied filtering function
	    if (!this.nodeFilter(node)) {
	        return '';
	    }
	    var c = false, result = "{";


//		Add all user-added attributes unless rejected by the attributeFilter.
	    for(var key in node.attributes) {

	        if (this.attributeFilter(key, node.attributes[key])) {
		        if (c) { result += ','; }
		        result += '"' + (this.attributeMap ? (this.attributeMap[key] || key) : key) + '":"' + node.attributes[key] + '"';
		        c = true;
		    }

	    }
	    //adds node depth
	    //result += '",depth":"' + node.getDepth() + '"';
	    
//		Add child nodes if any
	    var children = node.childNodes;
	    if(children !== undefined && children!= null){
		    var clen = children.length;
		    if(clen != 0){
		        if (c) {result += ',';}
		        result += '"children":[';
		        for(var i = 0; i < clen; i++){
		            if (i > 0) {result += ',';}
		            result += this.nodeToString(children[i]);
		        }
		        result += ']';
		    }
	    }
	    return result + "}";
	}
	 
});