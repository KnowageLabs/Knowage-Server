import { IVariable, IWidget } from "@/modules/documentExecution/dashboard/Dashboard";


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

export const parseHtml = (widgetModel: IWidget, tempDrivers: any[], tempVariables: IVariable[]) => {
    drivers = tempDrivers
    variables = tempVariables

    const html = widgetModel.settings.editor.html
    console.log('>>> PARSE HTML: ', html)

    if (html) {
        let wrappedHtmlToRender = "<div>" + html + " </div>";
        wrappedHtmlToRender = wrappedHtmlToRender.replace(gt, '$1&gt;$3');
        wrappedHtmlToRender = wrappedHtmlToRender.replace(lt, '$1&lt;$3');

        parseHtmlFunctions(wrappedHtmlToRender)
    }
}

const parseHtmlFunctions = (wrappedHtmlToRender: string) => {
    checkPlaceholders(wrappedHtmlToRender)
}


const checkPlaceholders = (rawHtml: string) => {
    let resultHtml = rawHtml;


    // if ($scope.datasetLabel) {
    //     resultHtml = resultHtml.replace($scope.columnRegex, $scope.replacer);
    //     resultHtml = resultHtml.replace($scope.activeSelectionsRegex, $scope.activeSelectionsReplacer);
    // }
    resultHtml = replaceWidgetId(resultHtml);
    resultHtml = resultHtml.replace(paramsRegex, paramsReplacer);
    resultHtml = resultHtml.replace(variablesRegex, variablesReplacer);
    resultHtml = replaceI18N(resultHtml);
    console.log(">>>>>>>>>> RESULT HTML: ", resultHtml)
    return resultHtml
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