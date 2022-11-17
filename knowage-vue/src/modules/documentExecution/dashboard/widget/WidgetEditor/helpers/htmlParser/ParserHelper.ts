import { ISelection, IVariable, IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { formatSelectionForDisplay } from '../../../ActiveSelectionsWidget/ActiveSelectionsWidgetHelpers'
import deepcopy from 'deepcopy'
import { formatNumberWithLocale } from '@/helpers/commons/localeHelper'
import i18n from '@/App.i18n'
import * as sanitizeHtml from 'sanitize-html';

const { t } = i18n.global

const widgetIdRegex = /#\[kn-widget-id\]/g
const activeSelectionsRegex = /(?:\[kn-active-selection(?:=\'([a-zA-Z0-9\_\-]+)\')?\s?\])/g
const columnRegexOld = /(?:\[kn-column=\'([a-zA-Z0-9\_\-\s]+)\'(?:\s+row=\'(\d*)\')?(?:\s+aggregation=\'(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\')?(?:\s+precision=\'(\d)\')?(\s+format)?\s?\])/g
const columnRegex = /(?:\[kn-column=\'([a-zA-Z0-9\_\-\s]+)\'(?:\s+row=\'(\d*)\')?(?:\s+aggregation=\'(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\')?(?:\s+precision=\'(\d)\')?(\s+format)?(?:\s+prefix=\'([a-zA-Z0-9\_\-\s]+)\')?(?:\s+suffix=\'([a-zA-Z0-9\_\-\s]+)\')?\s?\])/g
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
let widgetData = {} as any

let aggregationDataset = null as any

export const parseText = (tempWidgetModel: IWidget, tempDrivers: any[], tempVariables: IVariable[], tempSelections: ISelection[], internationalization: any, tempWidgetData: any, toast: any) => {
    drivers = tempDrivers
    variables = tempVariables
    activeSelections = tempSelections
    widgetModel = tempWidgetModel
    translatedValues = internationalization
    widgetData = tempWidgetData?.tempResponse
    aggregationDataset = tempWidgetData?.aggregationDataset

    let parsedText = ''

    try {
        const unparsedText = widgetModel.settings.editor.text
        if (!unparsedText) return ''

        parsedText = checkTextWidgetPlaceholders(unparsedText)
        parsedText = replaceTextFunctions(parsedText)
    } catch (error: any) {
        setError(tempWidgetModel, toast, error, 'text')
    }

    return parsedText
}

const checkTextWidgetPlaceholders = (unparsedText: string) => {
    unparsedText = unparsedText.replace(columnRegex, columnsReplacer)
    unparsedText = unparsedText.replace(paramsRegex, paramsReplacer)
    unparsedText = unparsedText.replace(variablesRegex, variablesReplacer)
    return unparsedText
}

const replaceTextFunctions = (parsedText: string) => {
    const parser = new DOMParser()
    const parsedHtml = parser.parseFromString(parsedText, 'text/html')
    let allElements = parsedHtml.getElementsByTagName('*')
    allElements = parseAttrs(allElements)
    return parsedHtml.firstChild ? (parsedHtml.firstChild as any).innerHTML : ''
}

export const parseHtml = (tempWidgetModel: IWidget, tempDrivers: any[], tempVariables: IVariable[], tempSelections: ISelection[], tempInternationalization: any, tempWidgetData: any, toast: any) => {
    drivers = tempDrivers
    variables = tempVariables
    activeSelections = tempSelections
    widgetModel = tempWidgetModel
    translatedValues = tempInternationalization
    widgetData = tempWidgetData?.tempResponse
    aggregationDataset = tempWidgetData?.aggregationDataset

    // console.group(`STUFF`)
    // console.log(`widget data: `, widgetData)
    // console.log(`widget data: `, aggregationDataset)
    // console.groupEnd()

    let trustedCss = ''
    let trustedHtml = ''

    try {
        const css = widgetModel.settings.editor.css
        if (css) {
            let placeholderResultCss = checkPlaceholders(css)
            placeholderResultCss = parseCalc(placeholderResultCss)
            trustedCss = sanitizeHtml(placeholderResultCss)
        }

        const html = widgetModel.settings.editor.html
        if (html) {
            let wrappedHtmlToRender = '<div style="position: absolute;height: 100%;width: 100%;">' + html + ' </div>'

            wrappedHtmlToRender = wrappedHtmlToRender.replace(gt, '$1&gt;$3')
            wrappedHtmlToRender = wrappedHtmlToRender.replace(lt, '$1&lt;$3')

            const parseHtmlFunctionsResult = parseHtmlFunctions(wrappedHtmlToRender)
            trustedHtml = sanitizeHtml(parseHtmlFunctionsResult, {
                allowedAttributes: { '*': ['*'] }
            })
        }
    } catch (error: any) {
        setError(tempWidgetModel, toast, error, 'html')
    }

    return { html: trustedHtml, css: trustedCss }
}

const setError = (tempWidgetModel: IWidget, toast: any, error: any, type: 'html' | 'text') => {
    if (toast) {
        const title = type === 'html' ? t('dashboard.widgetEditor.htmlParsingError') : t('dashboard.widgetEditor.textParsingError')
        toast.add({
            severity: 'error',
            summary: title + ' ' + tempWidgetModel?.id,
            detail: error.message,
            baseZIndex: 0,
            life: import.meta.env.VITE_TOAST_DURATION
        })
    }
}

const getColumnFromName = (columnName: string, datasetData: any, aggregation: any) => {
    if (datasetData) {
        for (var i in datasetData.metaData.fields) {
            if (typeof datasetData.metaData.fields[i].header != 'undefined' && datasetData.metaData.fields[i].header.toLowerCase() == (aggregation ? columnName + '_' + aggregation : columnName).toLowerCase()) {
                return { name: datasetData.metaData.fields[i].name, type: datasetData.metaData.fields[i].type }
            }
        }
    }
}

const parseHtmlFunctions = (rawHtml: string) => {
    const parser = new DOMParser()
    const parsedHtml = parser.parseFromString(rawHtml, 'text/html')
    let allElements = parsedHtml.getElementsByTagName('*')
    allElements = parseRepeat(allElements)
    allElements = parseIf(allElements)
    allElements = parseAttrs(allElements)
    const placeholderResultHtml = checkPlaceholders(parsedHtml.firstChild ? (parsedHtml.firstChild as any).innerHTML : '')
    const parseCalcResultHtml = parseCalc(placeholderResultHtml)
    return parseCalcResultHtml
}

const parseRepeat = (allElements: any) => {
    let i = 0
    do {
        if (!allElements[i].innerHTML) allElements[i].innerHTML = ' '
        if (allElements[i] && allElements[i].hasAttribute('kn-repeat')) {
            if (eval(checkAttributePlaceholders(allElements[i].getAttribute('kn-repeat')))) {
                allElements[i].removeAttribute('kn-repeat')
                let limit = allElements[i].hasAttribute('limit') && allElements[i].hasAttribute('limit') <= widgetData?.rows.length ? allElements[i].getAttribute('limit') : widgetData?.rows.length
                if (allElements[i].hasAttribute('limit') && allElements[i].getAttribute('limit') == -1) limit = widgetData?.rows.length
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
    for (let j = 0; j < limit; j++) {
        const tempRow = deepcopy(repeatedElement)
        tempRow.innerHTML = tempRow.innerHTML.replace(columnRegex, function (match: string, columnName: string, row: string, c3: string, precision: string, format: string) {
            let precisionPlaceholder = ''
            let formatPlaceholder = ''
            if (format) formatPlaceholder = ' format'
            if (precision) precisionPlaceholder = " precision='" + precision + "'"
            return "[kn-column='" + columnName + "' row='" + j + "'" + precisionPlaceholder + formatPlaceholder + ']'
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
            condition = condition.replace(paramsRegex, ifConditionParamsReplacer)
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
    let j = 0
    const nodesNumber = allElements.length
    do {
        if (allElements[j] && allElements[j].hasAttribute('kn-preview')) {
            allElements[j].classList.add('preview-class-temp')
        }
        if (allElements[j] && allElements[j].hasAttribute('kn-cross')) {
            allElements[j].classList.add('cross-nav-class-temp')
        }
        if (allElements[j] && allElements[j].hasAttribute('kn-selection-column')) {
            allElements[j].classList.add('select-class-temp')
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

const checkPlaceholders = (document: string) => {
    let resultHtml = document ?? ''

    resultHtml = resultHtml.replace(columnRegex, columnsReplacer)
    resultHtml = resultHtml.replace(activeSelectionsRegex, activeSelectionsReplacer)
    resultHtml = resultHtml.replace(widgetIdRegex, '')
    resultHtml = resultHtml.replace(paramsRegex, paramsReplacer)
    resultHtml = resultHtml.replace(variablesRegex, variablesReplacer)
    resultHtml = resultHtml.replace(i18nRegex, i18nReplacer)

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

const ifConditionReplacer = (match: string, p1: any, row: string, aggr: string, precision: number) => {
    const columnInfo = getColumnFromName(p1, aggr ? aggregationDataset : widgetData, aggr)
    if (!columnInfo) return p1
    if (aggr) {
        p1 = aggregationDataset && aggregationDataset.rows[0] && aggregationDataset.rows[0][columnInfo.name] !== '' && typeof aggregationDataset.rows[0][columnInfo.name] != 'undefined' ? aggregationDataset.rows[0][columnInfo.name] : null
    } else if (widgetData && widgetData.rows[row || 0] && typeof widgetData.rows[row || 0][columnInfo.name] != 'undefined' && widgetData.rows[row || 0][columnInfo.name] !== '') {
        let columnValue = widgetData.rows[row || 0][columnInfo.name]
        if (typeof columnValue == 'string') columnValue = addSlashes(columnValue)
        p1 = columnInfo.type == 'string' ? "'" + columnValue + "'" : columnValue
    } else {
        p1 = null
    }
    return precision && !isNaN(p1) ? parseFloat(p1).toFixed(precision) : p1
}

const ifConditionParamsReplacer = (match: string, p1: string, p2: string) => {
    const index = drivers.findIndex((driver: any) => driver.urlName === p1)
    if (index === -1) return addSlashes(null)
    let result = p2 ? drivers[index].description : drivers[index].value
    if (typeof result == 'string') {
        result = "'" + addSlashes(result) + "'"
    }
    return result
}

const columnsReplacer = (match, column, row, aggr, precision, format, prefix, suffix) => {
    //  console.log('COLUMNS REPLACER', match, column, row, aggr, precision, format, prefix, suffix)

    const columnInfo = getColumnFromName(column, aggr ? aggregationDataset : widgetData, aggr)
    // console.log('%c columnInfo columnInfo columnInfo ', 'color: white; background-color: #61dbfb')
    // console.log(columnInfo)

    if (!columnInfo) return column = (prefix || '') + null + (suffix || '')

    if (aggr) {
        column = aggregationDataset && aggregationDataset.rows[0] && aggregationDataset.rows[0][columnInfo.name] !== '' && typeof aggregationDataset.rows[0][columnInfo.name] != 'undefined' ? aggregationDataset.rows[0][columnInfo.name] : null
    } else if (widgetData && widgetData.rows[row || 0] && typeof widgetData.rows[row || 0][columnInfo.name] != 'undefined' && widgetData.rows[row || 0][columnInfo.name] !== '') {
        column = widgetData.rows[row || 0][columnInfo.name]
    } else {
        column = null
    }

    if (column != null && (columnInfo.type == 'int' || columnInfo.type == 'float')) {
        if (format) column = precision ? formatNumberWithLocale(column, precision, null) : formatNumberWithLocale(column, undefined, null)
        else column = precision ? parseFloat(column).toFixed(precision) : parseFloat(column)
    }
    column = (prefix || '') + column + (suffix || '')

    // console.log('%c returned  column column ', 'color: white; background-color: #61dbfb')
    // console.log(column)
    return column
}

export const paramsReplacer = (match: string, p1: string, p2: string) => {
    // TODO - Change when we finish drivers
    const index = drivers.findIndex((driver: any) => driver.urlName === p1)
    if (index === -1) return addSlashes(null)
    const result = p2 ? drivers[index].description : drivers[index].value
    return addSlashes(result)
}

export const variablesReplacer = (match: string, p1: string, p2: string) => {
    const index = variables.findIndex((variable: IVariable) => variable.name === p1)
    if (index === -1) return null
    const result = p2 && variables[index].pivotedValues ? variables[index].pivotedValues[p2] : variables[index].value
    return result || null
}

const i18nReplacer = (match: string, p1: string) => {
    const result = translatedValues[p1] ? translatedValues[p1] : p1
    return result || null
}

const checkAttributePlaceholders = (rawAttribute: string) => {
    let resultAttribute = rawAttribute.replace(columnRegex, columnsReplacer)
    resultAttribute = resultAttribute.replace(paramsRegex, paramsReplacer)
    return resultAttribute
}
