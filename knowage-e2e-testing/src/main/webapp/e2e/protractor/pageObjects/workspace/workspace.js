var helper = require('pageObjects/helper');
var knowageQbePage = require('pageObjects/knowageQbe/knowageQbePage');
var WorkSpace = function(){
	
	var loadingMask = element(by.css('.loadingMask'));
	var dataListToogle = element(by.css('button[aria-label="Data"]'));
	var modelsList = element.all(by.css('button[aria-label="Open business model in QBE"]'));
	var el = element(by.model('entityModel'));


	
	this.toggleDataList = function(){
		helper.getIframeDoc();
		helper.useAngular(false);
		//browser.sleep(10000);
		helper.waitForElementToBeClickable(dataListToogle);
		dataListToogle.click();
		return this;
	}
	
	this.getDataListItem = function(item){
		var datalistItem = element(by.css('button[aria-label="'+item+'"]'))
		helper.waitForElementToBeClickable(datalistItem);
		datalistItem.click();
		return this;
	}
	
	this.openModeInQbe = function(modelNo){
		helper.waitForElementToBeClickable(modelsList.get(modelNo));
		modelsList.get(modelNo).click();
		return knowageQbePage;
	}
	
	
}

module.exports = new WorkSpace();