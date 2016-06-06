describe('my app', function() {
 
  afterEach(function () {
	browser.sleep(2345); 
  });

  it('should open olap page', function(){
  	browser.get('/knowagewhatifengine/restful-services/start-standalone?SBI_EXECUTION_ID=4'); 
  });

  it('should open multi hierarchy dialog',function(){
  	element.all(by.css('[ng-click="showMultiHierDialog(e,filter)"]')).first().click();
  });

  it('should open dropdown list', function(){
  	element(by.model('selecetedMultiHierUN')).click();
  	
  });

  it('sholud select last available hierarchie', function(){
  	element.all(by.repeater('hier in member.hierarchies')).then(function(result){
  		element.all(by.repeater('hier in member.hierarchies')).get(result.length -1).click();	
  	});
  });

  it('should save new seleced hierarchy', function(){
  	element(by.css('[ng-click="updateHierarchie(ev)"]')).click();
  });

});