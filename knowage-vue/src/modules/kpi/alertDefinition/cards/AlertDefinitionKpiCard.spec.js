import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { nextTick } from 'vue'
import axios from 'axios'
import AlertDefinitionKpiCard from './AlertDefinitionKpiCard.vue'
import Toolbar from 'primevue/toolbar'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import Menu from 'primevue/menu'
import InputText from 'primevue/inputtext'

const mockedKpi = {
    id: 216,
    version: 0,
    name: 'PROFIT MARGIN',
    author: 'demo_admin',
    dateCreation: 1477308600000,
    active: false,
    enableVersioning: false,
    definition: '{"formula":"((M0-M1)/M2)","measures":["STORE_SALES","STORE_COST","STORE_SALES"],"functions":["SUM","SUM","SUM"],"formulaDecoded":"((SUM(STORE_SALES)-SUM(STORE_COST))/SUM(STORE_SALES))","formulaSimple":" (  ( STORE_SALES - STORE_COST )  / STORE_SALES ) "}',
    cardinality:
        '{"measureList":[{"ruleId":209,"ruleVersion":2,"ruleName":"SALES","measureName":"STORE_SALES","attributes":{"MONTH":true,"YEAR":true,"PRODUCT_FAMILY":true}},{"ruleId":209,"ruleVersion":2,"ruleName":"SALES","measureName":"STORE_COST","attributes":{"MONTH":true,"YEAR":true,"PRODUCT_FAMILY":true}},{"ruleId":209,"ruleVersion":2,"ruleName":"SALES","measureName":"STORE_SALES","attributes":{"MONTH":true,"YEAR":true,"PRODUCT_FAMILY":true}}],"checkedAttribute":{"attributeUnion":{"YEAR":9,"PRODUCT_FAMILY":9,"MONTH":9},"attributeIntersection":{"YEAR":true,"PRODUCT_FAMILY":true,"MONTH":true}}}',
    placeholder: '',
    category: {
        valueId: 404,
        valueCd: 'PROFIT',
        valueName: 'PROFIT',
        valueDescription: 'PROFIT',
        domainCode: 'KPI_KPI_CATEGORY',
        domainName: 'KPI_KPI_CATEGORY',
        translatedValueName: 'PROFIT',
        translatedValueDescription: 'PROFIT'
    },
    threshold: {
        id: 47,
        description: 'Threshold_1',
        name: 'Threshold_1',
        typeId: 75,
        type: 'Range',
        thresholdValues: [
            {
                id: 285,
                position: 1,
                label: 'test',
                color: '#00FFFF',
                severityId: 81,
                severityCd: 'LOW',
                minValue: 20,
                includeMin: false,
                maxValue: 20,
                includeMax: false
            }
        ],
        usedByKpi: false
    }
}

const mockedAlert = {
    id: 15,
    name: 'test',
    jsonOptions: {
        actions: [
            {
                jsonActionParameters: {
                    mailTo: [
                        {
                            name: 'tettetetet@gmail.com',
                            userId: '',
                            email: 'tettetetet@gmail.com'
                        }
                    ],
                    subject: 'tettetet',
                    body: 'tetetetetet'
                },
                idAction: '62',
                thresholdValues: ['151'],
                data: {
                    id: 62,
                    name: 'Send mail',
                    className: 'it.eng.knowage.enterprise.tools.alert.action.SendMail',
                    template: 'angular_1.4/tools/alert/actions/sendMail/templates/sendMail.html'
                },
                thresholdData: [
                    {
                        id: 151,
                        position: 2,
                        label: 'Medium',
                        color: '#FFFF00',
                        severityId: 80,
                        severityCd: 'MEDIUM',
                        minValue: 60,
                        includeMin: false,
                        maxValue: 120,
                        includeMax: true
                    }
                ]
            }
        ],
        kpiId: 217,
        kpiVersion: 0
    },
    singleExecution: false,
    eventBeforeTriggerAction: null,
    alertListener: {
        id: 33,
        name: 'KPI Listener',
        className: 'it.eng.spagobi.tools.alert.listener.KpiListener',
        template: 'angular_1.4/tools/alert/listeners/kpiListener/templates/kpiListener.html'
    },
    jobStatus: 'ACTIVE',
    frequency: {
        cron: {
            type: 'minute',
            parameter: {
                numRepetition: '1'
            }
        },
        startDate: 1627631436000,
        endDate: 1628149860000,
        startTime: '',
        endTime: ''
    }
}

vi.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() =>
        Promise.resolve({
            data: mockedKpi
        })
    ),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}

const factory = () => {
    return mount(AlertDefinitionKpiCard, {
        props: {
            selectedAlert: mockedAlert
        },
        global: {
            plugins: [],
            stubs: {
                Button,
                Card,
                Toolbar,
                Dropdown,
                Menu,
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
    it('disables the save button if no kpi is selected', () => {
        const wrapper = factory()
        wrapper.vm.alert = mockedAlert

        expect(wrapper.vm.disableActionButton).toBe(true)
        wrapper.vm.kpi = mockedKpi
        expect(wrapper.vm.disableActionButton).toBe(false)
    })
})
describe('Alert Definition kpi action', () => {
    it('shows a dialog when kpi action is clicked', async () => {
        const wrapper = factory()
        wrapper.vm.kpi = mockedKpi
        await nextTick()
        expect(wrapper.vm.disableActionButton).toBe(false)

        const addActionButton = wrapper.find('[data-test="add-action-button"]')
        await addActionButton.trigger('click')

        expect(wrapper.emitted()).toHaveProperty('showDialog')
    })
})
