import { AxiosResponse } from 'axios'
import i18n from '@/App.i18n'
import mainStore from '@/App.store'
import axios from 'axios'

export async function getCorrectRolesForExecution(document, dataset?) {
    let id = null
    let label = null
    let typeCode = ''

    if (dataset) {
        typeCode = dataset.dsTypeCd === 'Qbe' ? 'DATAMART' : 'DATASET'
        if (dataset.id) {
            id = dataset.id
        } else if (dataset.federation_id) {
            typeCode = 'FEDERATED_DATASET'
            id = dataset.federation_id
        }
        if (dataset.label) {
            label = dataset.label
        }
    } else if (document) {
        typeCode = 'DOCUMENT'
        if (document.type === 'businessModel') {
            typeCode = 'DATAMART'
        } else if (document.dsTypeCd) {
            typeCode = 'DATASET'
        }

        if (document.id) {
            id = document.id
        }

        if (document.label) {
            label = document.label
        }
    }

    return callGetCorrectRolesForExecution(typeCode, id, label)
}

export async function getCorrectRolesForExecutionForType(typeCode, id, label) {
    return callGetCorrectRolesForExecution(typeCode, id, label)
}

async function callGetCorrectRolesForExecution(typeCode, id, label) {
    let params = 'typeCode=' + typeCode + '&' + (id ? `id=${id}` : `label=${label}`)

    let url = import.meta.env.VITE_RESTFUL_SERVICES_PATH + `3.0/documentexecution/correctRolesForExecution?` + params

    const store = mainStore()
    return new Promise((resolve, reject) => {
        axios.get(url).then((response: AxiosResponse<any>) => {
            let rolesForExecution = response.data
            if (rolesForExecution.length == 0) {
                let msg = ''
                switch (typeCode) {
                    case 'DOCUMENT':
                        msg = i18n.global.t('documentExecution.main.userRoleError')
                        break
                    case 'DATAMART':
                        msg = i18n.global.t('workspace.myModels.userRoleError')
                        break
                    case 'DATASET':
                        msg = i18n.global.t('workspace.myData.userRoleError')
                        break
                }
                store.setError({
                    title: i18n.global.t('common.error.generic'),
                    msg: msg
                })
                reject()
            } else {
                resolve(rolesForExecution)
            }
        })
    })
}
