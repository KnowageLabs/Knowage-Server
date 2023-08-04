var helper = require('pageObjects/helper');
var EntityManager = function(){
	var root = element(by.model('entityModel'));
	var saveButton = null;
	var entityList = null;
	var countDispay = null;
	
	this.getRoot = function(){
		helper.waitForElement(root);
		return root;
	}
	
}

module.exports = new EntityManager();