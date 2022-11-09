import { ISelection, IVariable, IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { formatSelectionForDisplay } from '../../../ActiveSelectionsWidget/ActiveSelectionsWidgetHelpers'
import deepcopy from 'deepcopy'

const widgetIdRegex = /\[kn-widget-id\]/g
const activeSelectionsRegex = /(?:\[kn-active-selection(?:=\'([a-zA-Z0-9\_\-]+)\')?\s?\])/g
const columnRegex = /(?:\[kn-column=\'([a-zA-Z0-9\_\-\s]+)\'(?:\s+row=\'(\d*)\')?(?:\s+aggregation=\'(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\')?(?:\s+precision=\'(\d)\')?(\s+format)?\s?\])/g
const rowsRegex = /(?:\[kn-column=\'([a-zA-Z0-9\_\-\s]+)\'(?:\s+row=\'(\d+)\'){1}(?:\s+aggregation=\'(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\')?(?:\s+precision=\'(\d)\')?(\s+format)?\s?\])/g
const noAggregationsExistRegex = /\[kn-column=\'[a-zA-Z0-9\_\-\s]+\'(?:\s+row=\'\d+\')?(?!\s+aggregation=\'(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\')(?:\s+precision=\'(?:\d)\')?(?:\s+format)?\s?\]/g
const limitRegex = /<[\s\w\=\"\'\-\[\]]*(?!limit=)"([\-\d]+)"[\s\w\=\"\'\-\[\]]*>/g
const aggregationsRegex = /(?:\[kn-column=[\']{1}([a-zA-Z0-9\_\-\s]+)[\']{1}(?:\s+row=\'(\d*)\')?(?:\s+aggregation=[\']{1}(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)[\']{1}){1}(?:\s+precision=\'(\d)\')?(\s+format)?\])/g
const aggregationRegex = /(?:\[kn-column=[\']{1}([a-zA-Z0-9\_\-\s]+)[\']{1}(?:\s+row=\'(\d*)\')?(?:\s+aggregation=[\']{1}(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)[\']{1}){1}(?:\s+precision=\'(\d)\')?(\s+format)?\])/
const paramsRegex = /(?:\[kn-parameter=[\'\"]{1}([a-zA-Z0-9\_\-\s]+)[\'\"]{1}(\s+value)?\])/g
const calcRegex = /(?:\[kn-calc=\(([\[\]\w\s\-\=\>\<\"\'\!\+\*\/\%\&\,\.\|]*)\)(?:\s+min=\'(\d*)\')?(?:\s+max=\'(\d*)\')?(?:\s+precision=\'(\d)\')?(\s+format)?\])/g
const advancedCalcRegex = /(?:\[kn-calc=\{([\(\)\[\]\w\s\-\=\>\<\"\'\!\+\*\/\%\&\,\.\|]*)\}(?:\s+min=\'(\d*)\')?(?:\s+max=\'(\d*)\')?(?:\s+precision=\'(\d)\')?(\s+format)?\])/g
const repeatIndexRegex = /\[kn-repeat-index\]/g
const variablesRegex = /(?:\[kn-variable=\'([a-zA-Z0-9\_\-\s]+)\'(?:\s+key=\'([a-zA-Z0-9\_\-\s]+)\')?\s?\])/g
const i18nRegex = /(?:\[kn-i18n=\'([a-zA-Z0-9\_\-\s]+)\'\s?\])/g
const gt = /(\<.*kn-.*=["].*)(>)(.*["].*\>)/g
const lt = /(\<.*kn-.*=["].*)(<)(.*["].*\>)/g

let drivers = [] as any[]
let variables = [] as IVariable[]
let activeSelections = [] as ISelection[]
let widgetModel = null as IWidget | null
let translatedValues = {} as any

import mockedData from './mockedData.json'
const aggregationDataset = null as any // TODO
let limitRows = null as any // TODO - do we need it?

// TODO - DO WE NEED THIS?
const maxRow = () => {
    if (!widgetModel) return
    const str = widgetModel.settings.editor.css + widgetModel.settings.editor.html
    let tempMaxRow = 1
    const repeaters = str.replace(limitRegex, function (match: string, p1: any) {
        if (parseInt(p1) == -1) tempMaxRow = -1
        else if (p1 > tempMaxRow) tempMaxRow = parseInt(p1) + 1
    })
    const occurrencies = str.replace(rowsRegex, function (match: string, p1: any, p2: any) {
        if (p2 >= tempMaxRow) tempMaxRow = parseInt(p2) + 1
    })
    limitRows = { enable: true, rows: tempMaxRow }
    return tempMaxRow
}

export const parseHtml = (tempWidgetModel: IWidget, tempDrivers: any[], tempVariables: IVariable[], tempSelections: ISelection[], $sanitize: any, internationalization: any) => {
    drivers = tempDrivers
    variables = tempVariables
    activeSelections = tempSelections
    widgetModel = tempWidgetModel
    translatedValues = internationalization

    const css = widgetModel.settings.editor.css
    console.log('>>>>>>>>>> LOADED CSS: ', css)

    if (css) {
        let placeholderResultCss = checkPlaceholders(css)
        placeholderResultCss = parseCalc(placeholderResultCss)
        const trustedCss = $sanitize('<style>' + placeholderResultCss + '</style>')
        console.log('-------------- TRUSTED CSS: ', trustedCss)
    }

    const html = widgetModel.settings.editor.html
    // console.log(">>>>>>>>>> LOADED HTML: ", html)

    if (html) {
        let wrappedHtmlToRender = '<div>' + html + ' </div>'
        wrappedHtmlToRender = wrappedHtmlToRender.replace(gt, '$1&gt;$3')
        wrappedHtmlToRender = wrappedHtmlToRender.replace(lt, '$1&lt;$3')

        const parseHtmlFunctionsResult = parseHtmlFunctions(wrappedHtmlToRender)
        const trustedHtml = $sanitize(parseHtmlFunctionsResult)
        console.log('-------------- TRUSTED HTML: ', trustedHtml)
    }
}

const getColumnFromName = (columnName: string, datasetData: any, aggregation: any) => {
    for (var i in datasetData.metaData.fields) {
        if (typeof datasetData.metaData.fields[i].header != 'undefined' && datasetData.metaData.fields[i].header.toLowerCase() == (aggregation ? columnName + '_' + aggregation : columnName).toLowerCase()) {
            return { name: datasetData.metaData.fields[i].name, type: datasetData.metaData.fields[i].type }
        }
    }
}

const parseHtmlFunctions = (rawHtml: string) => {
    const parser = new DOMParser()
    const parsedHtml = parser.parseFromString(rawHtml, 'text/html')
    let allElements = parsedHtml.getElementsByTagName('*')
    allElements = parseRepeat(allElements)
    allElements = parseIf(allElements) // TODO - additional
    allElements = parseAttrs(allElements) // TODO - change function names???
    const placeholderResultHtml = checkPlaceholders(parsedHtml)
    const parseCalcResultHtml = parseCalc(placeholderResultHtml)
    return parseCalcResultHtml
}

// TODO
const parseAggregations = () => {
    // TODO
}

const parseRepeat = (allElements: any) => {
    let i = 0
    do {
        if (!allElements[i].innerHTML) allElements[i].innerHTML = ' '
        if (allElements[i] && allElements[i].hasAttribute('kn-repeat')) {
            if (eval(checkAttributePlaceholders(allElements[i].getAttribute('kn-repeat')))) {
                allElements[i].removeAttribute('kn-repeat')
                let limit = allElements[i].hasAttribute('limit') && allElements[i].hasAttribute('limit') <= mockedData.rows.length ? allElements[i].getAttribute('limit') : mockedData.rows.length
                if (allElements[i].hasAttribute('limit') && allElements[i].getAttribute('limit') == -1) limit = mockedData.rows.length
                if (allElements[i].hasAttribute('limit')) allElements[i].removeAttribute('limit')
                const repeatedElement = deepcopy(allElements[i])
                allElements[i].outerHTML = formatRepeatedElement(limit, repeatedElement)
            } else {
                allElements[i].outerHTML = ''
            }
        }
        i++
    } while (i < allElements.length)
    return allElements
}

const formatRepeatedElement = (limit: number, repeatedElement: any) => {
    let tempElement = null
    for (var j = 0; j < limit; j++) {
        const tempRow = deepcopy(repeatedElement)
        tempRow.innerHTML = tempRow.innerHTML.replace(columnRegex, function (match: string, columnName: string, row: string, c3: string, precision: string, format: string) {
            let precisionPlaceholder = ''
            let formatPlaceholder = ''
            if (format) formatPlaceholder = ' format'
            if (precision) precisionPlaceholder = " precision='" + precision + "'"
            return "[kn-column='" + columnName + "' row='" + (row || j) + "'" + precisionPlaceholder + formatPlaceholder + ']'
        })
        tempRow.innerHTML = tempRow.innerHTML.replace(repeatIndexRegex, j)
        j == 0 ? (tempElement = tempRow.outerHTML) : (tempElement += tempRow.outerHTML)
    }
    return tempElement
}

const parseIf = (allElements: any) => {
    var j = 0
    var nodesNumber = allElements.length
    do {
        if (allElements[j] && allElements[j].hasAttribute('kn-if')) {
            var condition = allElements[j].getAttribute('kn-if').replace(columnRegex, ifConditionReplacer)
            condition = condition.replace(activeSelectionsRegex, activeSelectionsReplacer)
            //  condition = condition.replace(paramsRegex, ifConditionParamsReplacer);  // TODO
            condition = condition.replace(calcRegex, calcReplacer)
            condition = condition.replace(variablesRegex, variablesReplacer)
            condition = condition.replace(i18nRegex, i18nReplacer)
            if (eval(condition)) {
                allElements[j].removeAttribute('kn-if')
            } else {
                allElements[j].parentNode.removeChild(allElements[j])
                j--
            }
        }
        j++
    } while (j < nodesNumber)
    return allElements
}

const parseAttrs = (allElements: any) => {
    // TODO - change function signatures
    let j = 0
    const nodesNumber = allElements.length
    do {
        if (allElements[j] && allElements[j].hasAttribute('kn-preview')) {
            const datasetPreviewLabel = allElements[j].getAttribute('kn-preview')
            allElements[j].setAttribute('ng-click', "showPreview('" + datasetPreviewLabel + "')")
        }
        if (allElements[j] && allElements[j].hasAttribute('kn-cross')) {
            allElements[j].setAttribute('ng-click', "doSelection(null,'" + allElements[j].getAttribute('kn-cross') + "')")
        }
        if (allElements[j] && allElements[j].hasAttribute('kn-selection-column')) {
            const columnSelectionLabel = allElements[j].getAttribute('kn-selection-column')
            let columnSelectionValue = allElements[j].getAttribute('kn-selection-value')
            if (columnSelectionValue.charAt(0) != '[') columnSelectionValue = "'" + columnSelectionValue + "'"
            allElements[j].setAttribute('ng-click', columnSelectionValue ? "select('" + columnSelectionLabel + "'," + columnSelectionValue + ')' : "select('" + columnSelectionLabel + "')")
        }
        j++
    } while (j < nodesNumber)
    return allElements
}

const parseCalc = (rawHtml: string) => {
    rawHtml = rawHtml.replace(advancedCalcRegex, calcReplacer)
    rawHtml = rawHtml.replace(calcRegex, calcReplacer)
    return rawHtml
}

const checkPlaceholders = (document: Document) => {
    let resultHtml = document.firstChild ? (document.firstChild as any).innerHTML : ''

    // if ($scope.datasetLabel) {
    //     resultHtml = resultHtml.replace($scope.columnRegex, $scope.replacer);
    //
    // }
    resultHtml = resultHtml.replace(activeSelectionsRegex, activeSelectionsReplacer)
    resultHtml = resultHtml.replace(widgetIdRegex, 'w' + widgetModel?.id)
    resultHtml = resultHtml.replace(paramsRegex, paramsReplacer)
    resultHtml = resultHtml.replace(variablesRegex, variablesReplacer)
    resultHtml = resultHtml.replace(i18nRegex, i18nReplacer)
    console.log('>>>>>>>>>> RESULT HTML: ', resultHtml)
    return resultHtml
}

const activeSelectionsReplacer = (match: string, columnName: string) => {
    const index = activeSelections.findIndex((selection: ISelection) => selection.datasetId === widgetModel?.dataset && selection.columnName === columnName)
    return index !== -1 ? formatSelectionForDisplay(activeSelections[index]) : 'null'
}

const addSlashes = (value: string | null) => {
    return (value + '')
        .replace(/\"/g, '&quot;')
        .replace(/\'/g, '&apos;')
        .replace(/\u0000/g, '\\0')
}

const calcReplacer = (match: string, p1: string, min: string, max: string, precision: any, format: string) => {
    var result = eval(p1)
    if (min && result < min) result = min
    if (max && result > max) result = max
    if (format) return precision ? parseFloat(result).toFixed(precision) : result
    return precision && !isNaN(result) ? parseFloat(result).toFixed(precision) : result
}

// TODO - aggregationDataset ???
const ifConditionReplacer = (match: string, p1: any, row: string, aggr: string, precision: number) => {
    const columnInfo = getColumnFromName(p1, aggr ? aggregationDataset : mockedData, aggr)
    if (!columnInfo) return p1
    if (aggr) {
        p1 = aggregationDataset && aggregationDataset.rows[0] && aggregationDataset.rows[0][columnInfo.name] !== '' && typeof aggregationDataset.rows[0][columnInfo.name] != 'undefined' ? aggregationDataset.rows[0][columnInfo.name] : null
    } else if (mockedData && mockedData.rows[row || 0] && typeof mockedData.rows[row || 0][columnInfo.name] != 'undefined' && mockedData.rows[row || 0][columnInfo.name] !== '') {
        let columnValue = mockedData.rows[row || 0][columnInfo.name]
        if (typeof columnValue == 'string') columnValue = addSlashes(columnValue)
        p1 = columnInfo.type == 'string' ? "'" + columnValue + "'" : columnValue
    } else {
        p1 = null
    }
    return precision && !isNaN(p1) ? parseFloat(p1).toFixed(precision) : p1
}

// TODO
const ifConditionParamsReplacer = () => {
    // TODO
}

// TODO
const replacer = () => {
    // TODO
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

// TODO
const i18nReplacer = (match: string, p1: string) => {
    console.log('i18nReplacer match--------------------', match)
    console.log('i18nReplacer p1--------------------', p1)
    console.log('translatedValues translatedValues--------------------', translatedValues[p1])

    const result = translatedValues[p1] ? translatedValues[p1] : p1
    return result || null
}

const checkAttributePlaceholders = (rawAttribute: string) => {
    // TODO
    // let resultAttribute = rawAttribute.replace($scope.columnRegex, $scope.replacer); - TODO
    let resultAttribute = rawAttribute.replace(paramsRegex, paramsReplacer)
    return resultAttribute
}
