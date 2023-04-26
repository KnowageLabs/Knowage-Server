export const widgetIdRegex = /#\[kn-widget-id\]/g
export const activeSelectionsRegex = /(?:\[kn-active-selection(?:=\'([a-zA-Z0-9\_\-]+)\')?\s?\])/g
export const columnRegex = /(?:\[kn-column=\'([a-zA-Z0-9\_\-\s]+)\'(?:\s+row=\'(\d*)\')?(?:\s+aggregation=\'(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\')?(?:\s+precision=\'(\d)\')?(\s+format)?(?:\s+prefix=\'([a-zA-Z0-9\_\-\s]+)\')?(?:\s+suffix=\'([a-zA-Z0-9\_\-\s]+)\')?\s?\])/g
export const paramsRegex = /(?:\[kn-parameter=[\'\"]{1}([a-zA-Z0-9\_\-\s]+)[\'\"]{1}(\s+value)?\])/g
export const calcRegex = /(?:\[kn-calc=\(([\[\]\w\s\-\=\>\<\"\'\!\+\*\/\%\&\,\.\|]*)\)(?:\s+min=\'(\d*)\')?(?:\s+max=\'(\d*)\')?(?:\s+precision=\'(\d)\')?(\s+format)?\])/g
export const advancedCalcRegex = /(?:\[kn-calc=\{([\(\)\[\]\w\s\-\=\>\<\"\'\!\+\*\/\%\&\,\.\|]*)\}(?:\s+min=\'(\d*)\')?(?:\s+max=\'(\d*)\')?(?:\s+precision=\'(\d)\')?(\s+format)?\])/g
export const repeatIndexRegex = /\[kn-repeat-index\]/g
export const variablesRegex = /(?:\[kn-variable=\'([a-zA-Z0-9\_\-\s]+)\'(?:\s+key=\'([a-zA-Z0-9\_\-\s]+)\')?\s?\])/g
export const i18nRegex = /(?:\[kn-i18n=\'([a-zA-Z0-9\_\-\s]+)\'\s?\])/g
export const gt = /(\<.*kn-.*=["].*)(>)(.*["].*\>)/g
export const lt = /(\<.*kn-.*=["].*)(<)(.*["].*\>)/g

export const limitRegex = /<[\s\w\=\"\'\-\[\]]*(?!limit=)"([\-\d]+)"[\s\w\=\"\'\-\[\]]*>/g
export const rowsRegex = /(?:\[kn-column=\'([a-zA-Z0-9\_\-\s]+)\'(?:\s+row=\'(\d+)\'){1}(?:\s+aggregation=\'(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\')?(?:\s+precision=\'(\d)\')?(\s+format)?\s?\])/g
export const aggregationRegex =
    /(?:\[kn-column=[\']{1}([a-zA-Z0-9\_\-\s]+)[\']{1}(?:\s+row=\'(\d*)\')?(?:\s+aggregation=[\']{1}(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)[\']{1}){1}(?:\s+precision=\'(\d)\')?(\s+format)?(\s+prefix=\'([a-zA-Z0-9\_\-\s]+)\')?(\s+suffix=\'([a-zA-Z0-9\_\-\s]+)\')?\])/
export const aggregationsRegex =
    /(?:\[kn-column=[\']{1}([a-zA-Z0-9\_\-\s]+)[\']{1}(?:\s+row=\'(\d*)\')?(?:\s+aggregation=[\']{1}(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)[\']{1}){1}(?:\s+precision=\'(\d)\')?(\s+format)?(\s+prefix=\'([a-zA-Z0-9\_\-\s]+)\')?(\s+suffix=\'([a-zA-Z0-9\_\-\s]+)\')?\])/g

export const parameterTextCompatibilityRegex = /\$P{(.+?)\}/g
export const variableTextCompatibilityRegex = /\$V{(.+?)\}/g;
export const columnTextCompatibilityRegex = /(SUM\(|AVG\(|MIN\(|MAX\(|COUNT\(|COUNT_DISTINCT\()?\$F{(.+?)\}\)?/g;
export const crossNavigationTextCompatibilityRegex = /ng-click="doSelection(.+?)"/g;