import { AxiosResponse } from "axios"
import X2JS from 'x2js'

export async function startOlap($http: any, user: any, sbiExecutionId: string, document: any, template: any) {
    const language = user.locale?.split('_')[0]
    const uniqueID = user.userUniqueIdentifier
    const country = user.locale?.split('_')[1]

    let fileType = template.name.split('.')
    const selectedTemplateFileType = fileType[fileType.length - 1]
    let selectedTemplateContent = '' as any
    let schemaName = ''
    await $http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${document.id}/templates/selected/${template.id}`, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } }).then((response: AxiosResponse<any>) => {
        selectedTemplateFileType == 'sbicockpit' || selectedTemplateFileType == 'json' || selectedTemplateFileType == 'sbigeoreport' ? (selectedTemplateContent = JSON.stringify(response.data, null, 4)) : (selectedTemplateContent = response.data)
        const x2js = new X2JS()
        selectedTemplateContent = x2js.xml2js(selectedTemplateContent)
        console.log("SELECTED TEMPLATE CONTENT: ", selectedTemplateContent)
        schemaName = selectedTemplateContent.olap.cube._reference
    })

    console.log("schema name: ", schemaName)
    let schema = null as any
    await $http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/mondrianSchemasResource/name=${schemaName}`).then((response: AxiosResponse<any>) => schema = response.data)

    console.log("SCHEMA: ", schema)

    // const tempBla =
    //     { "olap": { "cube": { "reference": "FoodMart_v2" }, "TOOLBAR": { "BUTTON_DRILL_THROUGH": { "visible": true, "clicked": false }, "BUTTON_MDX": { "visible": true, "clicked": false }, "BUTTON_EDIT_MDX": { "visible": true, "clicked": false }, "BUTTON_FATHER_MEMBERS": { "visible": true, "clicked": false }, "BUTTON_CC": { "visible": true, "clicked": false }, "BUTTON_HIDE_SPANS": { "visible": true, "clicked": false }, "BUTTON_SORTING_SETTINGS": { "visible": true, "clicked": false }, "BUTTON_SORTING": { "visible": true, "clicked": false }, "BUTTON_SHOW_PROPERTIES": { "visible": true, "clicked": false }, "BUTTON_HIDE_EMPTY": { "visible": true, "clicked": false }, "BUTTON_SAVE_SUBOBJECT": { "visible": true, "clicked": false }, "BUTTON_FLUSH_CACHE": { "visible": true, "clicked": false }, "BUTTON_SAVE_NEW": { "visible": true, "clicked": false }, "BUTTON_UNDO": { "visible": true, "clicked": false }, "BUTTON_VERSION_MANAGER": { "visible": true, "clicked": false }, "BUTTON_EXPORT_OUTPUT": { "visible": true, "clicked": false }, "BUTTON_EDITABLE_EXCEL_EXPORT": { "visible": true, "clicked": false }, "BUTTON_ALGORITHMS": { "visible": false, "clicked": false } }, "MDXMondrianQuery": { "XML_TAG_TEXT_CONTENT": "SELECT+Union(Union(Union(CrossJoin({[Stores].[All+Stores]},+{[Measures].[Store+Cost],+[Measures].[Store+Sales]}),+CrossJoin({[Stores].[Canada]},+{[Measures].[Store+Cost],+[Measures].[Store+Sales]})),+CrossJoin({[Stores].[Mexico]},+{[Measures].[Store+Cost],+[Measures].[Store+Sales]})),+CrossJoin({[Stores].[USA]},+{[Measures].[Store+Cost],+[Measures].[Store+Sales]}))+ON+COLUMNS,+Hierarchize(Union({[Customers.All+Customers].[All+Customers]},+[Customers.All+Customers].[All+Customers].Children))+ON+ROWS+FROM+[Sales]+WHERE+[Product].[All+Products]" }, "MDXQUERY": { "XML_TAG_TEXT_CONTENT": "SELECT+Union(Union(Union(CrossJoin({[Stores].[All+Stores]},+{[Measures].[Store+Cost],+[Measures].[Store+Sales]}),+CrossJoin({[Stores].[Canada]},+{[Measures].[Store+Cost],+[Measures].[Store+Sales]})),+CrossJoin({[Stores].[Mexico]},+{[Measures].[Store+Cost],+[Measures].[Store+Sales]})),+CrossJoin({[Stores].[USA]},+{[Measures].[Store+Cost],+[Measures].[Store+Sales]}))+ON+COLUMNS,+Hierarchize(Union({[Customers.All+Customers].[All+Customers]},+[Customers.All+Customers].[All+Customers].Children))+ON+ROWS+FROM+[Sales]+WHERE+[Product].[All+Products]", "parameter": [] }, "pagination": { "XML_TAG_TEXT_CONTENT": false }, "CROSS_NAVIGATION": { "PARAMETERS": { "PARAMETER": [{ "dimension": "Stores", "hierarchy": "[Stores]", "level": "[Stores].[Store+Country]", "name": "Test", "type": "From+Cell" }] } } } }
    const hiddenFormData = new URLSearchParams()
    hiddenFormData.set('user_id', decodeURIComponent(uniqueID))
    hiddenFormData.set('SBI_LANGUAGE', decodeURIComponent(language))
    hiddenFormData.set('SBI_COUNTRY', decodeURIComponent(country))
    hiddenFormData.set('ENGINE', 'knowageolapengine')
    hiddenFormData.set('SBI_EXECUTION_ID', decodeURIComponent(sbiExecutionId))
    hiddenFormData.set('DOCUMENT_LABEL', decodeURIComponent(document.label))
    hiddenFormData.set('document', decodeURIComponent('' + document.id))
    hiddenFormData.set('template', (selectedTemplateContent.olap.JSONTEMPLATE))
    hiddenFormData.set('SBI_ARTIFACT_ID', decodeURIComponent('' + schema.id))
    hiddenFormData.set('schemaID', decodeURIComponent('' + schema.id))
    hiddenFormData.set('SBI_ARTIFACT_VERSION_ID', decodeURIComponent('' + schema.currentContentId))
    hiddenFormData.set('schemaName', decodeURIComponent(schema.name))
    hiddenFormData.set('onEditMode', decodeURIComponent(''))

    $http.get(process.env.VUE_APP_OLAP_PATH + `olap/startolap`, { headers: { Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9' }, params: hiddenFormData }).then(() => { })

}