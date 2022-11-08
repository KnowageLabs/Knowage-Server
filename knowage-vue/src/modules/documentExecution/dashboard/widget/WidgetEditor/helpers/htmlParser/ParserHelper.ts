import { ISelection, IVariable, IWidget } from "@/modules/documentExecution/dashboard/Dashboard";
import { formatSelectionForDisplay } from "../../../ActiveSelectionsWidget/ActiveSelectionsWidgetHelpers";
import deepcopy from "deepcopy";

const widgetIdRegex = /\[kn-widget-id\]/g;
const activeSelectionsRegex = /(?:\[kn-active-selection(?:=\'([a-zA-Z0-9\_\-]+)\')?\s?\])/g;
const columnRegex = /(?:\[kn-column=\'([a-zA-Z0-9\_\-\s]+)\'(?:\s+row=\'(\d*)\')?(?:\s+aggregation=\'(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\')?(?:\s+precision=\'(\d)\')?(\s+format)?\s?\])/g;
const rowsRegex = /(?:\[kn-column=\'([a-zA-Z0-9\_\-\s]+)\'(?:\s+row=\'(\d+)\'){1}(?:\s+aggregation=\'(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\')?(?:\s+precision=\'(\d)\')?(\s+format)?\s?\])/g;
const noAggregationsExistRegex = /\[kn-column=\'[a-zA-Z0-9\_\-\s]+\'(?:\s+row=\'\d+\')?(?!\s+aggregation=\'(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\')(?:\s+precision=\'(?:\d)\')?(?:\s+format)?\s?\]/g;
const limitRegex = /<[\s\w\=\"\'\-\[\]]*(?!limit=)"([\-\d]+)"[\s\w\=\"\'\-\[\]]*>/g;
const aggregationsRegex = /(?:\[kn-column=[\']{1}([a-zA-Z0-9\_\-\s]+)[\']{1}(?:\s+row=\'(\d*)\')?(?:\s+aggregation=[\']{1}(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)[\']{1}){1}(?:\s+precision=\'(\d)\')?(\s+format)?\])/g;
const aggregationRegex = /(?:\[kn-column=[\']{1}([a-zA-Z0-9\_\-\s]+)[\']{1}(?:\s+row=\'(\d*)\')?(?:\s+aggregation=[\']{1}(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)[\']{1}){1}(?:\s+precision=\'(\d)\')?(\s+format)?\])/;
const paramsRegex = /(?:\[kn-parameter=[\'\"]{1}([a-zA-Z0-9\_\-\s]+)[\'\"]{1}(\s+value)?\])/g;
const calcRegex = /(?:\[kn-calc=\(([\[\]\w\s\-\=\>\<\"\'\!\+\*\/\%\&\,\.\|]*)\)(?:\s+min=\'(\d*)\')?(?:\s+max=\'(\d*)\')?(?:\s+precision=\'(\d)\')?(\s+format)?\])/g;
const advancedCalcRegex = /(?:\[kn-calc=\{([\(\)\[\]\w\s\-\=\>\<\"\'\!\+\*\/\%\&\,\.\|]*)\}(?:\s+min=\'(\d*)\')?(?:\s+max=\'(\d*)\')?(?:\s+precision=\'(\d)\')?(\s+format)?\])/g;
const repeatIndexRegex = /\[kn-repeat-index\]/g;
const variablesRegex = /(?:\[kn-variable=\'([a-zA-Z0-9\_\-\s]+)\'(?:\s+key=\'([a-zA-Z0-9\_\-\s]+)\')?\s?\])/g;
const i18nRegex = /(?:\[kn-i18n=\'([a-zA-Z0-9\_\-\s]+)\'\s?\])/g;
const gt = /(\<.*kn-.*=["].*)(>)(.*["].*\>)/g;
const lt = /(\<.*kn-.*=["].*)(<)(.*["].*\>)/g;

let drivers = [] as any[]
let variables = [] as IVariable[]
let activeSelections = [] as ISelection[]
let widgetModel = null as IWidget | null

import mockedData from './mockedData.json'

export const parseHtml = (tempWidgetModel: IWidget, tempDrivers: any[], tempVariables: IVariable[], tempSelections: ISelection[]) => {
    drivers = tempDrivers
    variables = tempVariables
    activeSelections = tempSelections
    widgetModel = tempWidgetModel


    const html = widgetModel.settings.editor.html
    console.log(">>>>>>>>>> LOADED HTML: ", html)

    if (html) {
        let wrappedHtmlToRender = "<div>" + html + " </div>";
        wrappedHtmlToRender = wrappedHtmlToRender.replace(gt, '$1&gt;$3');
        wrappedHtmlToRender = wrappedHtmlToRender.replace(lt, '$1&lt;$3');

        parseHtmlFunctions(wrappedHtmlToRender)
    }
}

const parseHtmlFunctions = (rawHtml: string) => {
    const parser = new DOMParser()
    const parsedHtml = parser.parseFromString(rawHtml, "text/html");
    let allElements = parsedHtml.getElementsByTagName('*');
    allElements = parseRepeat(allElements);
    // allElements = $scope.parseIf(allElements);  // TODO
    //  allElements = $scope.parseAttrs(allElements);  // TODO
    console.log(">>>>>>>>> ALL ELEMENTS: ", allElements)
    checkPlaceholders(parsedHtml)
}

const parseRepeat = (allElements: any) => {
    let i = 0;
    do {
        if (!allElements[i].innerHTML) allElements[i].innerHTML = ' ';
        if (allElements[i] && allElements[i].hasAttribute("kn-repeat")) {
            if (eval(checkAttributePlaceholders(allElements[i].getAttribute('kn-repeat')))) {
                allElements[i].removeAttribute("kn-repeat");
                let limit = allElements[i].hasAttribute("limit") && (allElements[i].hasAttribute("limit") <= mockedData.rows.length) ? allElements[i].getAttribute('limit') : mockedData.rows.length;
                if (allElements[i].hasAttribute("limit") && allElements[i].getAttribute('limit') == -1) limit = mockedData.rows.length;
                if (allElements[i].hasAttribute("limit")) allElements[i].removeAttribute("limit");
                const repeatedElement = deepcopy(allElements[i]);
                allElements[i].outerHTML = formatRepeatedElement(limit, repeatedElement);
            } else {
                allElements[i].outerHTML = "";
            }
        } i++;
    } while (i < allElements.length);
    return allElements;
}

const formatRepeatedElement = (limit: number, repeatedElement: any) => {
    let tempElement = null;
    for (var j = 0; j < limit; j++) {
        const tempRow = deepcopy(repeatedElement);
        tempRow.innerHTML = tempRow.innerHTML.replace(columnRegex, function (match: string, columnName: string, row: string, c3: string, precision: string, format: string) {
            let precisionPlaceholder = '';
            let formatPlaceholder = '';
            if (format) formatPlaceholder = ' format';
            if (precision) precisionPlaceholder = " precision='" + precision + "'";
            return "[kn-column=\'" + columnName + "\' row=\'" + (row || j) + "\'" + precisionPlaceholder + formatPlaceholder + "]";
        }
        );
        tempRow.innerHTML = tempRow.innerHTML.replace(repeatIndexRegex, j);
        j == 0 ? tempElement = tempRow.outerHTML : tempElement += tempRow.outerHTML
    }
    return tempElement
}

const columnPlaceholderReplacer = function (match, c1, c2, c3, precision, format) {
    let precisionPlaceholder = '';
    let formatPlaceholder = '';
    if (format) formatPlaceholder = ' format';
    if (precision) precisionPlaceholder = " precision='" + precision + "'";
    //  return "[kn-column=\'" + c1 + "\' row=\'" + (c2 || j) + "\'" + precisionPlaceholder + formatPlaceholder + "]";
}

const checkAttributePlaceholders = (rawAttribute: string) => {
    // TODO
    // let resultAttribute = rawAttribute.replace($scope.columnRegex, $scope.replacer); - TODO
    let resultAttribute = rawAttribute.replace(paramsRegex, paramsReplacer);
    return resultAttribute;
}

const checkPlaceholders = (document: Document) => {
    let resultHtml = document.firstChild ? (document.firstChild as any).innerHTML : '';


    // if ($scope.datasetLabel) {
    //     resultHtml = resultHtml.replace($scope.columnRegex, $scope.replacer);
    //  
    // }
    resultHtml = resultHtml.replace(activeSelectionsRegex, activeSelectionsReplacer);
    resultHtml = replaceWidgetId(resultHtml);  // TODO
    resultHtml = resultHtml.replace(paramsRegex, paramsReplacer);
    resultHtml = resultHtml.replace(variablesRegex, variablesReplacer);
    resultHtml = replaceI18N(resultHtml);  // TODO
    console.log(">>>>>>>>>> RESULT HTML: ", resultHtml)
    return resultHtml
}

const activeSelectionsReplacer = (match: string, columnName: string) => {
    const index = activeSelections.findIndex((selection: ISelection) => selection.datasetId === widgetModel?.dataset && selection.columnName === columnName)
    return index !== -1 ? formatSelectionForDisplay(activeSelections[index]) : 'null'
}



const replaceWidgetId = (rawHtml: string) => {
    // resultHtml.replace($scope.widgetIdRegex, 'w' + $scope.ngModel.id);

    return rawHtml
}

const paramsReplacer = (match: string, p1: string, p2: string) => {
    // TODO - Change when we finish drivers
    const index = drivers.findIndex((driver: any) => driver.urlName === p1)
    if (index === -1) return addSlashes(null)
    const result = p2 ? drivers[index].description : drivers[index].value
    return addSlashes(result)
}

const variablesReplacer = (match: string, p1: string, p2: string) => {
    const index = variables.findIndex((variable: IVariable) => variable.name === p1)
    if (index === -1) return null
    const result = p2 && variables[index].pivotedValues ? variables[index].pivotedValues[p2] : variables[index].value
    return result || null
}

const replaceI18N = (rawHtml: string) => {
    // resultHtml.replace($scope.i18nRegex, $scope.i18nReplacer);
    return rawHtml
}

const addSlashes = (value: string | null) => {
    return (value + '').replace(/\"/g, '&quot;').replace(/\'/g, '&apos;').replace(/\u0000/g, '\\0');
}