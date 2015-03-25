/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  function viewManagerObj(){
// array of vies
	  this.views = new Array();

// add function
  function addViewFunct(corr) {
    this.views[this.views.length] = corr;
  }
  this.addView = addViewFunct;

//get function
  function getViewFunct(index) {
    return this.views[index];
  }
  this.getView = getViewFunct;

//set function
  function setViewFunct(index, corr) {
    this.views[index] = corr;
  }
  this.setView = setViewFunct;

//delete function
  function deleteViewFunct(index) {
    var prog = 0;
    var tmpCorr = new Array();
    for(i=0; i<this.views.length; i++) {
      if(i!=index) {
        tmpCorr[prog] = this.views[i];
        prog = prog + 1;
      }
    }
    this.views=tmpCorr; 
  }
  this.deleteView = deleteViewFunct;

//set pre condition fucntion
  function setViewPreConditionFunct(index, precondval) {
    this.views[index].preCond = precondval; 
  }
  this.setViewPreCondition = setViewPreConditionFunct;

//set psot condition fucntion
  function setViewPostConditionFunct(index, postcondval) {
    this.views[index].postCond = postcondval; 
  }
  this.setViewPostCondition = setViewPostConditionFunct;

//set logic operatorfucntion 
  function setViewLogicOperatorFunct(index, logop) {
    this.views[index].logicOper = logop; 
  }
  this.setViewLogicOperator = setViewLogicOperatorFunct;

//check already exist funciton
  function viewExistFunct(idPFath, fOp) {
     var found = false;
     for(i=0; i<this.views.length; i++) {
        var corr = this.views[i];
        if( (corr.idParFather==idPFath) && (corr.condition==fOp) ) {
          found = true;
        }
     }
     return found; 
  }
  this.viewExist = viewExistFunct;

}