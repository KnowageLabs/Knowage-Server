import { AxiosResponse } from "axios"
import X2JS from 'x2js'

export async function startOlap($http: any, user: any, sbiExecutionId: string, document: any, template: any, router: any) {
    console.log("--- startOlap() - document: ", document)
    const language = user.locale?.split('_')[0]
    const uniqueID = user.userUniqueIdentifier
    const country = user.locale?.split('_')[1]
    let fileType = template.name.split('.')
    const selectedTemplateFileType = fileType[fileType.length - 1]
    let selectedTemplateContent = await getSelectedTemplate($http, selectedTemplateFileType, document, template)
    const schemaName = selectedTemplateContent.olap.cube._reference
    let schema = await getSchema($http, schemaName) as any
    if (!schema) return

    const params = createUrlParameters(uniqueID, language, country, sbiExecutionId, document, selectedTemplateContent, schema)

    $http.get(process.env.VUE_APP_OLAP_PATH + `olap/startolap`, { headers: { Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9' }, params: params }).then(() => {
        router.push(`/olap-designer/${sbiExecutionId}?olapId=${document.id}&olapName=${document.name}&olapLabel=${document.label}`)
    })
}

async function getSelectedTemplate($http: any, selectedTemplateFileType: string, document: any, template: any) {
    let selectedTemplateContent = {} as any
    await $http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${document.id}/templates/selected/${template.id}`, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } }).then((response: AxiosResponse<any>) => {
        selectedTemplateFileType == 'sbicockpit' || selectedTemplateFileType == 'json' || selectedTemplateFileType == 'sbigeoreport' ? (selectedTemplateContent = JSON.stringify(response.data, null, 4)) : (selectedTemplateContent = response.data)
        const x2js = new X2JS()
        selectedTemplateContent = x2js.xml2js(selectedTemplateContent)
    })
    return selectedTemplateContent
}

async function getSchema($http: any, schemaName: string) {
    let schema = null
    await $http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/mondrianSchemasResource/name=${schemaName}`).then((response: AxiosResponse<any>) => schema = response.data)
    return schema
}

function createUrlParameters(uniqueID: string, language: string, country: string, sbiExecutionId: string, document: any, selectedTemplateContent: any, schema: any) {
    const params = new URLSearchParams()
    params.set('user_id', decodeURIComponent(uniqueID))
    params.set('SBI_LANGUAGE', decodeURIComponent(language))
    params.set('SBI_COUNTRY', decodeURIComponent(country))
    params.set('ENGINE', document.engine)
    params.set('SBI_EXECUTION_ID', decodeURIComponent(sbiExecutionId))
    params.set('DOCUMENT_LABEL', decodeURIComponent(document.label))
    params.set('document', decodeURIComponent('' + document.id))
    params.set('template', (selectedTemplateContent.olap.JSONTEMPLATE))
    params.set('SBI_ARTIFACT_ID', decodeURIComponent('' + schema.id))
    params.set('schemaID', decodeURIComponent('' + schema.id))
    params.set('SBI_ARTIFACT_VERSION_ID', decodeURIComponent('' + schema.currentContentId))
    params.set('schemaName', decodeURIComponent(schema.name))
    params.set('onEditMode', decodeURIComponent(''))
    return params;

}