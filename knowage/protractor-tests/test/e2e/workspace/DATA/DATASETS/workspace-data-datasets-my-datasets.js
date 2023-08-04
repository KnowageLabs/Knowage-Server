describe('workspace - datasets - my datasets', function() {

	afterEach(function() {
		browser.sleep(50);
	});

	it('should try to login', function() {
	browser.ignoreSynchronization=true;
	 browser.get('/knowage');
	 browser.driver.findElement(by.id('userID')).sendKeys('biadmin');
	 browser.driver.findElement(by.id('password')).sendKeys('biadmin');
	 browser.driver.findElement(by.css('.btn-signin')).click();
	});

	it('should click on hamburger menu', function() {
	 element(by.css('.menuKnowage')).click();
	 browser.sleep(1000);
	});

	it('should open Workspace',function(){
		element(by.css('[title="Workspace"]')).click();
		browser.sleep(5000);
	});

	it('should open data - my datasets',function(){
		var driver = browser.driver;
		var loc = by.tagName('iframe');
		var el = driver.findElement(loc);
		browser.switchTo().frame(el);

		element(by.css('[class="md-clickable notSelectedLeftMenuItem"]')).click();
		browser.sleep(2000);
		element.all(by.repeater('suboption in option.submenuOptions')).get(0).click();
		browser.sleep(6000);
	});

	it('should toggle share status of a dataset',function(){
		element(by.css('[aria-label="shareDataset"]')).click();
		browser.sleep(3000);
	});

	it('should  toggle back share status of a dataset',function(){
		element(by.css('[aria-label="shareDataset"]')).click();
		browser.sleep(3000);
	});

	it('should preview a dataset',function(){
		element(by.css('[aria-label="previewDataset"]')).click();
		browser.sleep(4000);
	});

	it('should close preview of a dataset',function(){
		element(by.id('close-preview')).click();
		browser.sleep(3000);
	});

	it('should click on delete button of a dataset',function(){
		element(by.css('[aria-label="deleteDataset"]')).click();
		browser.sleep(3000);
	});

	it('should select no on delete conformation',function(){
		element(by.css('[ng-click="dialog.abort()"]')).click();
		browser.sleep(3000);
	});

});
