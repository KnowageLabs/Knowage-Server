/*------------------------------------------------------------------------
	- HTML Table Filter Generator Sorting Feature
	- TF Adapter v2.1 for WebFX Sortable Table 1.12 (Erik Arvidsson) 
	- By Max Guglielmi (tablefilter.free.fr)
	- Licensed under the MIT License
--------------------------------------------------------------------------
Copyright (c) 2009-2012 Max Guglielmi

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
------------------------------------------------------------------------
	- Changelog:
		1.1 [30-09-09] 
		When table is paged now the whole table is sorted and not
		only the current page
		1.2 [05-12-09]
		Added on_before_sort and on_after_sort callback functions
		1.3 [06-09-10]
		Added IP adresses sort
		1.4 [22-01-11]
		Added DDMMMYYYY date format support
		1.5 [20-02-11]
		Image folder and sort icon can now be set in configuration 
		object
		1.6 [06-08-11]
		Added on_sort_loaded callback function
		1.7 [20-08-11]
		Added sort arrow css classes properties
		Added sort custom key property allowing custom sorts
		Added AddSortType public method
		1.8 [25-09-11]
		Bug fix: sort did not take into account results per page changes 
		1.9 [03-03-12]
		Bug fix: custom value attribute value was read only by Firefox
		2.0 [08-04-12]
		Bug fix: zebra rows background are no longer inverted after sort
		2.1 [25-08-12]
		Bug fix: IE9 did not sort columns correctly
------------------------------------------------------------------------*/

TF.prototype.SetSortTable = function(){
	var o = this; //TF object
	var f = o.fObj; //TF config object
	var isTFPaged = false;

	//edit .sort-arrow.descending / .sort-arrow.ascending in filtergrid.css to reflect any path change	
	o.sortImgPath = f.sort_images_path != undefined ? f.sort_images_path : o.themesPath;
	o.sortImgBlank = f.sort_image_blank != undefined ? f.sort_image_blank : 'blank.png';
	o.sortImgClassName = f.sort_image_class_name != undefined ? f.sort_image_class_name : 'sort-arrow';
	o.sortImgAscClassName = f.sort_image_asc_class_name != undefined ? f.sort_image_asc_class_name : 'ascending';
	o.sortImgDescClassName = f.sort_image_desc_class_name != undefined ? f.sort_image_desc_class_name : 'descending';
	o.sortCustomKey = f.sort_custom_key != undefined ? f.sort_custom_key : '_sortKey'; //cell attribute storing custom key

	/*** TF additional events ***/
	//additional paging events for alternating bg issue
	o.Evt._Paging.nextEvt = function(){ if(o.sorted && o.alternateBgs) o.Filter(); }
	o.Evt._Paging.prevEvt = o.Evt._Paging.nextEvt;
	o.Evt._Paging.firstEvt = o.Evt._Paging.nextEvt;
	o.Evt._Paging.lastEvt = o.Evt._Paging.nextEvt;
	o.Evt._OnSlcPagesChangeEvt = o.Evt._Paging.nextEvt;
	/*** ***/

	/*** Extension events ***/
	//callback invoked after sort is loaded and instanciated
	o.onSortLoaded = tf_IsFn(f.on_sort_loaded) ? f.on_sort_loaded : null;
	//callback invoked before table is sorted
	o.onBeforeSort = tf_IsFn(f.on_before_sort) ? f.on_before_sort : null;
	//callback invoked after table is sorted
	o.onAfterSort = tf_IsFn(f.on_after_sort) ? f.on_after_sort : null;

	/*** SortableTable ***/
	//in case SortableTable class is missing (sortabletable.js)
	if((typeof SortableTable)=='undefined'){ return; }

	//overrides headerOnclick method in order to handle th
	SortableTable.prototype.headerOnclick = function (e) {
		if(!o.sort) return; // TF adaptation
		var el = e.target || e.srcElement; // find Header element

		while (el.tagName != 'TD' && el.tagName != 'TH') // TF adaptation
			el = el.parentNode;

		this.sort(SortableTable.msie ? SortableTable.getCellIndex(el) : el.cellIndex);
	};
	
	//overrides getCellIndex IE returns wrong cellIndex when columns are hidden
	SortableTable.getCellIndex = function (oTd) {
		var cells = oTd.parentNode.cells,
			l = cells.length, i;
		for (i = 0; cells[i] != oTd && i < l; i++)
			;
		return i;
	};

	//overrides initHeader in order to handle filters row position
	SortableTable.prototype.initHeader = function (oSortTypes) {
		if (!this.tHead) return;
		this.headersRow = o.headersRow; // TF adaptation
		var cells = this.tHead.rows[this.headersRow].cells; // TF adaptation
		var doc = this.tHead.ownerDocument || this.tHead.document;
		this.sortTypes = oSortTypes || [];
		var l = cells.length;
		var img, c;
		for (var i = 0; i < l; i++) {
			c = cells[i];
			if (this.sortTypes[i] != null && this.sortTypes[i] != 'None') {
				c.style.cursor = 'pointer';
				img = tf_CreateElm('img',['src', o.sortImgPath + o.sortImgBlank]);
				c.appendChild(img);
				if (this.sortTypes[i] != null)
					c.setAttribute( '_sortType', this.sortTypes[i]);
				tf_AddEvent(c, 'click', this._headerOnclick);
			} else {
				c.setAttribute( '_sortType', oSortTypes[i] );
				c._sortType = 'None';
			}
		}
		this.updateHeaderArrows();
	};

	//overrides updateHeaderArrows in order to handle arrows
	SortableTable.prototype.updateHeaderArrows = function () {
		var cells, l, img;
		if(o.sortConfig.asyncSort && o.sortConfig.triggerIds!=null){//external headers
			var triggers = o.sortConfig.triggerIds;
			cells = [], l = triggers.length;
			for(var j=0; j<triggers.length; j++)
				cells.push(tf_Id(triggers[j]));
		} else {
			if (!this.tHead) return;
			cells = this.tHead.rows[this.headersRow].cells; //TF implementation instead
			l = cells.length;
		}
		for (var i = 0; i < l; i++) {
			if (cells[i].getAttribute('_sortType') != null && cells[i].getAttribute('_sortType') != 'None') {
				img = cells[i].lastChild || cells[i];
				if(img.nodeName.tf_LCase()!='img'){//creates images
					img = tf_CreateElm('img',['src', o.sortImgPath + o.sortImgBlank]);
					cells[i].appendChild(img);
				}
				if (i == this.sortColumn)
					img.className = o.sortImgClassName +' '+ (this.descending ? o.sortImgDescClassName : o.sortImgAscClassName);
				else img.className = o.sortImgClassName;
			}
		}
	};

	//overrides getRowValue for custom key value feature
	SortableTable.prototype.getRowValue = function (oRow, sType, nColumn) {
		// if we have defined a custom getRowValue use that
		if (this._sortTypeInfo[sType] && this._sortTypeInfo[sType].getRowValue)
			return this._sortTypeInfo[sType].getRowValue(oRow, nColumn);
		var c = oRow.cells[nColumn];
		var s = SortableTable.getInnerText(c);
		return this.getValueFromString(s, sType);
	};

	//overrides getInnerText in order to avoid Firefox unexpected sorting behaviour with untrimmed text elements
	SortableTable.getInnerText = function (oNode) {
		if (oNode.getAttribute(o.sortCustomKey) != null) //custom sort key
			return oNode.getAttribute(o.sortCustomKey);
		else return tf_GetNodeText(oNode);
	};

	//sort types
	var sortTypes = [];	
	for(var i=0; i<o.nbCells; i++){
		var colType;
		if(this.sortConfig.sortTypes!=null && this.sortConfig.sortTypes[i]!=null){
			colType = this.sortConfig.sortTypes[i].tf_LCase();
			if(colType=='none') colType = 'None';
		} else {//resolves column types
			if(o.hasColNbFormat && o.colNbFormat[i]!=null)
				colType = o.colNbFormat[i].tf_LCase();
				//colType = 'Number';
			else if(o.hasColDateType && o.colDateType[i]!=null)
				colType = o.colDateType[i].tf_LCase()+'date';
			else colType = 'String';
		}
		sortTypes.push(colType);
	}

	//Public TF method to add sort type
	this.AddSortType = function(){ SortableTable.prototype.addSortType(arguments[0], arguments[1], arguments[2], arguments[3]); };
	
	//Custom sort types
	this.AddSortType('number', Number);
	this.AddSortType('caseinsensitivestring', SortableTable.toUpperCase);
	this.AddSortType('date', SortableTable.toDate);
	this.AddSortType('string');
	this.AddSortType('us', usNumberConverter);
	this.AddSortType('eu', euNumberConverter);
	this.AddSortType('dmydate', dmyDateConverter);
	this.AddSortType('ymddate', ymdDateConverter);
	this.AddSortType('mdydate', mdyDateConverter);
	this.AddSortType('ddmmmyyyydate', ddmmmyyyyDateConverter);
	this.AddSortType('ipaddress', ipAddress, sortIP);

	this.st = new SortableTable(this.tbl,sortTypes);

	/*** external table headers adapter ***/
	if(this.sortConfig.asyncSort && this.sortConfig.triggerIds!=null){
		var triggers = this.sortConfig.triggerIds;
		for(var j=0; j<triggers.length; j++){
			if(triggers[j]==null) continue;
			var trigger = tf_Id(triggers[j]);
			if(trigger){
				trigger.style.cursor = 'pointer';
				trigger.onclick = function(){
					if(o.sort)
						o.st.asyncSort(	triggers.tf_IndexByValue(this.id, true) );
				}
				trigger.setAttribute( '_sortType', sortTypes[j] );
			}
		}
	}
	/*** ***/

	//Column sort at start
	if(this.sortConfig.sortCol) 
		this.st.sort( this.sortConfig.sortCol[0], this.sortConfig.sortCol[1] );

	this.isSortEnabled = true; //sort is set
	if(this.onSortLoaded) this.onSortLoaded.call(null, this, this.st);

	/*** Sort events ***/
	this.st.onbeforesort = function(){
		if(o.onBeforeSort) o.onBeforeSort.call(null, o, o.st.sortColumn);
		o.Sort(); //TF method

		/*** sort behaviour for paging ***/
		if(o.paging){
			isTFPaged = true;
			o.paging = false;
			o.RemovePaging();
		}
	}//onbeforesort

	this.st.onsort = function(){
		o.sorted = true;//table is sorted

		//rows alternating bg issue
		if(o.alternateBgs){
			var rows = o.tbl.rows, c = 0;

			function setClass(row, i, removeOnly){
				if(removeOnly == undefined) removeOnly = false;
				tf_removeClass(row, o.rowBgEvenCssClass);
				tf_removeClass(row, o.rowBgOddCssClass);
				if(!removeOnly)
					tf_addClass(row, i % 2 ? o.rowBgOddCssClass : o.rowBgEvenCssClass);
			}

			for (var i = o.refRow; i < o.nbRows; i++) {
				var isRowValid = rows[i].getAttribute('validRow');
				if(o.paging && rows[i].style.display == ''){
					setClass(rows[i], c);
					c++;
				} else {
					if((isRowValid=='true' || isRowValid==null) && rows[i].style.display == ''){
						setClass(rows[i], c);
						c++;
					} else setClass(rows[i], c, true);
				}
			}
		}
		//sort behaviour for paging
		if(isTFPaged){
			if(o.hasResultsPerPage) 
				o.fObj.paging_length = o.resultsPerPageSlc.options[o.resultsPerPageSlc.selectedIndex].value;
			o.AddPaging(false);
			o.SetPage(o.currentPageNb);
			isTFPaged = false;
		}
		if(o.onAfterSort) o.onAfterSort.call(null,o,o.st.sortColumn);
	}//onsort
}

//Converter fns
function usNumberConverter(s){ return tf_removeNbFormat(s,'us'); }
function euNumberConverter(s){ return tf_removeNbFormat(s,'eu'); }
function dateConverter(s, format){ return tf_formatDate(s, format); }
function dmyDateConverter(s){ return dateConverter(s,'DMY'); }
function mdyDateConverter(s){ return dateConverter(s,'MDY'); }
function ymdDateConverter(s){ return dateConverter(s,'YMD'); }
function ddmmmyyyyDateConverter(s){ return dateConverter(s,'DDMMMYYYY'); }

function ipAddress(val){
	var vals = val.split('.');
	for (x in vals) {
		val = vals[x];
		while (3 > val.length) val = '0'+val;
		vals[x] = val;
	}
	val = vals.join('.');
	return val;
}

function sortIP(a,b){	
	var aa = ipAddress(a.value.tf_LCase());
	var bb = ipAddress(b.value.tf_LCase());
	if (aa==bb)	return 0;
	else if (aa<bb) return -1;
	else return 1;
}