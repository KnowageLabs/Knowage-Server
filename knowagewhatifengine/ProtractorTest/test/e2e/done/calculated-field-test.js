describe('calculated field test', function() {
 
  afterEach(function () {
	browser.sleep(2345); 
  });

  it('should open olap page', function(){
  	browser.get('/knowagewhatifengine/restful-services/start-standalone?SBI_EXECUTION_ID=4'); 
  });

  it('should open sidenav', function(){
  	element(by.css('[ng-click="toggleRight()"]')).click();
  });

  it('should open calculated field dialog', function(){
  	//ng-click="showCCWizard()"
  	element(by.id('calc-field-btn')).click();
  });

  it('should select some formula', function(){
  	//formula in formulasData
  	//var name = element(by)
  	element.all(by.repeater('formula in formulasData')).first().click();
  	//expect(element.all(by.repeater('formula in formulasData')).first())
  });

});  