import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import flushPromises from 'flush-promises'
import AlertDetail from './AlertDefinitionDetail.vue'
import axios from 'axios'
import Toolbar from 'primevue/toolbar'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import Menu from 'primevue/menu'
import Message from 'primevue/message'
import InputText from 'primevue/inputtext'

const mockedAlert = {
    id: 25,
    name: 'test crone',
    jsonOptions:
        '{"actions":[{"jsonActionParameters":"{\\"mailTo\\":[{\\"name\\":\\"meme@fakemail.com\\",\\"userId\\":\\"\\",\\"email\\":\\"meme@fakemail.com\\"}],\\"subject\\":\\"Meee dadad\\",\\"body\\":\\"<h2>eaaarrra</h2>\\"}","idAction":"62","thresholdValues":["225"]}],"kpiId":230,"kpiVersion":1}',
    singleExecution: true,
    eventBeforeTriggerAction: null,
    alertListener: {
        id: 33,
        name: 'KPI Listener',
        className: 'it.eng.spagobi.tools.alert.listener.KpiListener',
        template: 'angular_1.4/tools/alert/listeners/kpiListener/templates/kpiListener.html'
    },
    jobStatus: 'ACTIVE',
    frequency: {
        cron: '{"type":"minute","parameter":{"numRepetition":"1"}}',
        startDate: 1627465013000,
        endDate: 1627724220000,
        startTime: '11:36',
        endTime: '11:37'
    }
}
const mockedActionList = [
    {
        id: 62,
        name: 'Send mail',
        className: 'it.eng.knowage.enterprise.tools.alert.action.SendMail',
        template: 'angular_1.4/tools/alert/actions/sendMail/templates/sendMail.html'
    },
    {
        id: 63,
        name: 'Execute ETL Document',
        className: 'it.eng.knowage.enterprise.tools.alert.action.ExecuteETLDocument',
        template: 'angular_1.4/tools/alert/actions/executeETL/templates/executeETL.html'
    },
    {
        id: 86,
        name: 'Notify to Context Broker',
        className: 'it.eng.spagobi.tools.alert.action.NotifyContextBroker',
        template: 'js/src/angular_1.4/tools/alert/actions/contextBroker/templates/contextBroker.html'
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/alert/listAction`:
                return Promise.resolve({ data: mockedActionList })
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `'1.0/alert/25/load'`:
                return Promise.resolve({ data: mockedAlert })
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `'2.0/documents/listDocument?includeType=ETL'`:
                return Promise.resolve({ data: [] })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

const factory = () => {
    return mount(AlertDetail, {
        props: {
            id: '33'
        },
        global: {
            plugins: [createTestingPinia()],
            stubs: {
                Button,
                Card,
                Toolbar,
                Dropdown,
                Menu,
                Message,
                NameCard: true,
                KpiCard: true,
                EventsCard: true,
                KnCron: true,
                AddActionDialog: true,
                InputText
            },
            mocks: {
                $t: (msg) => msg,
                $http
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})
describe('Alert Definition Detail', () => {
    it('disables the save button if one required input is empty', async () => {
        const formWrapper = factory()

        await flushPromises()
        expect(formWrapper.vm.selectedAlert.name).toStrictEqual(undefined)
        expect(formWrapper.vm.selectedAlert.alertListener).toStrictEqual(undefined)

        expect(formWrapper.vm.buttonDisabled).toBe(true)
    })
})
