import { AxiosResponse } from "axios"
import X2JS from 'x2js'

export async function startOlap($http: any, user: any, sbiExecutionId: string, document: any, template: any) {
    const language = user.locale?.split('_')[0]
    const uniqueID = user.userUniqueIdentifier
    const country = user.locale?.split('_')[1]

    console.log("DOCUMENT: ", document)
    console.log("Template: ", template)

    let fileType = template.name.split('.')
    const selectedTemplateFileType = fileType[fileType.length - 1]
    let selectedTemplateContent = ''

    await $http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${document.id}/templates/selected/${template.id}`, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } }).then((response: AxiosResponse<any>) => {
        selectedTemplateFileType == 'sbicockpit' || selectedTemplateFileType == 'json' || selectedTemplateFileType == 'sbigeoreport' ? (selectedTemplateContent = JSON.stringify(response.data, null, 4)) : (selectedTemplateContent = response.data)
        const x2js = new X2JS()
        selectedTemplateContent = x2js.xml2js(selectedTemplateContent)
        console.log("SELECTED TEMPLATE CONTENT: ", selectedTemplateContent)
    })


    const hiddenFormData = new URLSearchParams()
    hiddenFormData.set('user_id', decodeURIComponent(uniqueID))
    hiddenFormData.set('SBI_LANGUAGE', decodeURIComponent(language))
    hiddenFormData.set('SBI_COUNTRY', decodeURIComponent(country))
    hiddenFormData.set('ENGINE', 'knowageolapengine')
    hiddenFormData.set('SBI_EXECUTION_ID', decodeURIComponent(sbiExecutionId))
    hiddenFormData.set('DOCUMENT_LABEL', decodeURIComponent(document.label))
    hiddenFormData.set('document', decodeURIComponent('' + document.id))
    hiddenFormData.set('template', (JSON.stringify(selectedTemplateContent)))

    // TODO - Hardcoded
    hiddenFormData.set('SBI_ARTIFACT_ID', decodeURIComponent('' + 25))
    hiddenFormData.set('schemaID', decodeURIComponent('' + 25))
    hiddenFormData.set('SBI_ARTIFACT_VERSION_ID', decodeURIComponent('' + 231))
    hiddenFormData.set('schemaName', decodeURIComponent('FoodMart_v2'))
    hiddenFormData.set('onEditMode', decodeURIComponent(''))

    $http.get(process.env.VUE_APP_OLAP_PATH + `olap/startolap`, { headers: { Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9' }, params: hiddenFormData }).then(() => { })

}