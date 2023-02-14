import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import FabButton from '@/components/UI/KnFabButton.vue'
import flushPromises from 'flush-promises'
import KnPerspectiveCard from '@/components/UI/KnPerspectiveCard/KnPerspectiveCard.vue'
import InputText from 'primevue/inputtext'
import PrimeVue from 'primevue/config'
import ProgressBar from 'primevue/progressbar'
import ScorecardsDesigner from './ScorecardsDesigner.vue'
import ScorecardsTable from './ScorecardsTable/ScorecardsTable.vue'
import Toolbar from 'primevue/toolbar'

const mockedScorecard = {
    id: 1,
    name: 'Company Scorecard',
    creationDate: 1477324102000,
    author: 'demo_admin',
    perspectives: [
        {
            id: 115,
            name: 'Perspective One',
            criterion: {
                valueId: 250,
                valueCd: 'PRIORITY',
                valueName: 'sbidomains.nm.prioritycrit',
                valueDescription: 'it.eng.spagobi.kpi.statusCriterion.Priority',
                domainCode: 'KPI_SCORECARD_CRITE',
                domainName: 'KPI SCORECARD CRITERIA',
                translatedValueName: 'Policy "Priority"',
                translatedValueDescription: 'it.eng.spagobi.kpi.statusCriterion.Priority'
            },
            options: {
                criterionPriority: ['Target One']
            },
            status: 'GRAY',
            targets: [
                {
                    id: 116,
                    name: 'Target One',
                    criterion: {
                        valueId: 240,
                        valueCd: 'MAJORITY',
                        valueName: 'sbidomains.nm.majoritycrit',
                        valueDescription: 'it.eng.spagobi.kpi.statusCriterion.Majority',
                        domainCode: 'KPI_SCORECARD_CRITE',
                        domainName: 'KPI SCORECARD CRITERIA',
                        translatedValueName: 'Policy "Majority"',
                        translatedValueDescription: 'it.eng.spagobi.kpi.statusCriterion.Majority'
                    },
                    options: {
                        criterionPriority: []
                    },
                    status: 'GRAY',
                    kpis: [
                        {
                            id: 217,
                            version: 0,
                            name: 'MARKUP',
                            author: 'demo_admin',
                            dateCreation: 1477312334000,
                            active: false,
                            enableVersioning: false,
                            definition:
                                '{"formula":"((M0-M1)/M2)*100","measures":["STORE_SALES","STORE_COST","STORE_COST"],"functions":["SUM","SUM","SUM"],"formulaDecoded":"((SUM(STORE_SALES)-SUM(STORE_COST))/SUM(STORE_COST))*100","formulaSimple":" (  ( STORE_SALES - STORE_COST )  / STORE_COST )  * 100"}',
                            cardinality:
                                '{"measureList":[{"ruleId":209,"ruleVersion":2,"ruleName":"SALES","measureName":"STORE_SALES","attributes":{"YEAR":true,"PRODUCT_FAMILY":true,"MONTH":true},"$$hashKey":"object:900"},{"ruleId":209,"ruleVersion":2,"ruleName":"SALES","measureName":"STORE_COST","attributes":{"YEAR":true,"PRODUCT_FAMILY":true,"MONTH":true},"$$hashKey":"object:901"},{"ruleId":209,"ruleVersion":2,"ruleName":"SALES","measureName":"STORE_COST","attributes":{"YEAR":true,"PRODUCT_FAMILY":true,"MONTH":true},"$$hashKey":"object:902"}],"checkedAttribute":{"attributeUnion":{"YEAR":3,"PRODUCT_FAMILY":3,"MONTH":3},"attributeIntersection":{"YEAR":true,"PRODUCT_FAMILY":true,"MONTH":true}}}',
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
                                id: 48,
                                description: 'Threshold_1',
                                name: 'Threshold_1 (Clone)',
                                typeId: 75,
                                type: 'Range',
                                thresholdValues: [
                                    {
                                        id: 150,
                                        position: 1,
                                        label: 'OK',
                                        color: '#00FF00',
                                        severityId: 78,
                                        severityCd: 'URGENT',
                                        minValue: 0,
                                        includeMin: false,
                                        maxValue: 60,
                                        includeMax: true
                                    },
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
                                    },
                                    {
                                        id: 152,
                                        position: 3,
                                        label: 'KO',
                                        color: '#FF0000',
                                        severityId: 81,
                                        severityCd: 'LOW',
                                        minValue: 120,
                                        includeMin: false,
                                        maxValue: 200,
                                        includeMax: true
                                    }
                                ],
                                usedByKpi: false
                            },
                            status: null
                        },
                        {
                            id: 216,
                            version: 0,
                            name: 'PROFIT MARGIN',
                            author: 'demo_admin',
                            dateCreation: 1477312200000,
                            active: false,
                            enableVersioning: false,
                            definition:
                                '{"formula":"((M0-M1)/M2)*100","measures":["STORE_SALES","STORE_COST","STORE_SALES"],"functions":["SUM","SUM","SUM"],"formulaDecoded":"((SUM(STORE_SALES)-SUM(STORE_COST))/SUM(STORE_SALES))*100","formulaSimple":" (  ( STORE_SALES - STORE_COST )  / STORE_SALES )  * 100"}',
                            cardinality:
                                '{"measureList":[{"ruleId":209,"ruleVersion":2,"ruleName":"SALES","measureName":"STORE_SALES","attributes":{"YEAR":true,"PRODUCT_FAMILY":true,"MONTH":true},"$$hashKey":"object:413"},{"ruleId":209,"ruleVersion":2,"ruleName":"SALES","measureName":"STORE_COST","attributes":{"YEAR":true,"PRODUCT_FAMILY":true,"MONTH":true},"$$hashKey":"object:414"},{"ruleId":209,"ruleVersion":2,"ruleName":"SALES","measureName":"STORE_SALES","attributes":{"YEAR":true,"PRODUCT_FAMILY":true,"MONTH":true},"$$hashKey":"object:415"}],"checkedAttribute":{"attributeUnion":{"YEAR":3,"PRODUCT_FAMILY":3,"MONTH":3},"attributeIntersection":{"YEAR":true,"PRODUCT_FAMILY":true,"MONTH":true}}}',
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
                                        id: 147,
                                        position: 1,
                                        label: 'OK',
                                        color: '#00FF00',
                                        severityId: 78,
                                        severityCd: 'URGENT',
                                        minValue: 0,
                                        includeMin: false,
                                        maxValue: 500000,
                                        includeMax: true
                                    },
                                    {
                                        id: 148,
                                        position: 2,
                                        label: 'Medium',
                                        color: '#FFFF00',
                                        severityId: 80,
                                        severityCd: 'MEDIUM',
                                        minValue: -30,
                                        includeMin: false,
                                        maxValue: -80,
                                        includeMax: true
                                    },
                                    {
                                        id: 149,
                                        position: 3,
                                        label: '-KO',
                                        color: '#FF0000',
                                        severityId: 81,
                                        severityCd: 'LOW',
                                        minValue: -80,
                                        includeMin: false,
                                        maxValue: 100,
                                        includeMax: true
                                    }
                                ],
                                usedByKpi: false
                            },
                            status: null
                        }
                    ],
                    groupedKpis: [
                        {
                            status: null,
                            count: 2
                        }
                    ],
                    updated: false,
                    statusColor: 'GRAY'
                }
            ],
            groupedKpis: [
                {
                    status: null,
                    count: 4
                }
            ],
            statusColor: 'GRAY'
        }
    ]
}

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/kpiee/1/loadScorecard`:
                return Promise.resolve({ data: mockedScorecard })
            default:
                return Promise.resolve({ data: [] })
        }
    }),
    post: vi.fn().mockImplementation(() => Promise.resolve({ data: [] }))
}

const $confirm = {
    require: vi.fn()
}

const factory = () => {
    return mount(ScorecardsDesigner, {
        props: { id: '1' },
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [createTestingPinia()],
            stubs: {
                Button,
                Card,
                Column,
                DataTable,
                InputText,
                FabButton,
                KnPerspectiveCard,
                ProgressBar,
                ScorecardsTable,
                Toolbar,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
                $http,
                $confirm
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Scorecards Designer', () => {
    it('should show an item in the preview after that item is created in the editor', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.scorecard).toStrictEqual(mockedScorecard)
        expect(wrapper.find(['[data-test="perspective-Perspective One"]']).html()).toContain('Perspective One')

        wrapper.vm.scorecard.perspectives.push({
            name: 'New Perspective',
            status: 'GRAY',
            criterion: {
                valueId: 240,
                valueCd: 'MAJORITY',
                valueName: 'sbidomains.nm.majoritycrit',
                valueDescription: 'it.eng.spagobi.kpi.statusCriterion.Majority',
                domainCode: 'KPI_SCORECARD_CRITE',
                domainName: 'KPI SCORECARD CRITERIA',
                translatedValueName: 'Policy "Majority"',
                translatedValueDescription: 'it.eng.spagobi.kpi.statusCriterion.Majority'
            },
            options: { criterionPriority: [] },
            targets: [],
            groupedKpis: []
        })

        await nextTick()

        expect(wrapper.find(['[data-test="perspective-New Perspective"]']).html()).toContain('New Perspective')
    })

    it('should delete an item in the preview after that item is deleted in the editor', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.scorecard).toStrictEqual(mockedScorecard)
        expect(wrapper.find(['[data-test="perspective-Perspective One"]']).html()).toContain('Perspective One')

        wrapper.vm.scorecard.perspectives = []

        await nextTick()

        expect(wrapper.find(['[data-test="perspective-New Perspective"]']).exists()).toBe(false)
    })
})
