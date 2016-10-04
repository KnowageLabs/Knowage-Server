describe('search filter test', function(){

	afterEach(function(){
		browser.sleep(2345);
	});

	it('open olap page', function(){
		browser.get('/knowagewhatifengine/restful-services/start-standalone?SBI_EXECUTION_ID=4');
	});

	it('open Region filter element',function(){
		browser.driver.findElement(by.id('Region-filter')).click();
	});

	it('search for filter and siblings',function(){
		element(by.model('searchText')).sendKeys('canada');
		browser.sleep(567);
		//posibly can add expect that search icon is enabled
		element(by.css('[ng-click="searchFilter()"]')).click();
		//expect that one is selected?
	});

	it('search for filter without siblings',function(){
		//posibly can add expect that search icon is enabled
		element(by.model('showSiblings')).click();
		browser.sleep(567);
		element(by.css('[ng-click="searchFilter()"]')).click();
		//expect that one is selected?
	});

});