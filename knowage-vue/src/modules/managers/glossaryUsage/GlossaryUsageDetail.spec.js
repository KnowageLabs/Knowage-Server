import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import GlossaryUsageDetail from './GlossaryUsageDetail.vue'
import ProgressBar from 'primevue/progressbar'

const mockedDocumentInfo = {
    id: 1,
    name: 'mockedDocument',
    description: 'BestProduct single pameter - Composite Document',
    label: 'mockedDocument',
    drivers: [],
    functionalities: ['/Functionalities/Demo/Demo_DynamicMap/target document (cross navigation)']
}

const mockedRoles = ['/demo/admin', '/demo/user']

const mockedDatasetInfo = {
    SbiGlDataSetWlist: [],
    DataSet: {
        id: { dsId: 1, versionNum: 4, organization: 'DEMO' },
        name: 'Census NYC',
        description: null,
        label: 'Census_NYC',
        active: true,
        type: 'SbiFileDataSet',
        configuration: {}
    },
    Word: [{ WORD_ID: 262, WORD: 'Customer' }]
}

const mockedBusinessClassInfo = {
    sbiGlBnessClsWlist: [
        { name: 'Salad bar', columnId: 5888, type: 'attribute', word: [{ WORD_ID: 264, WORD: 'Product Sales' }] },
        { name: 'Region id', columnId: 5901, type: 'attribute', word: [] },
        { name: 'Store country', columnId: 5889, type: 'attribute', word: [] }
    ],
    metaBc: {
        bcId: 1,
        sbiMetaModel: { id: 1, name: 'Expenses', description: 'Expenses cube' },
        name: 'Store',
        sbiMetaDsBcs: [],
        sbiMetaTableBcs: [],
        sbiMetaBcAttributes: []
    },
    words: [{ WORD_ID: 265, WORD: 'Inventory' }]
}

const mockedTableInfo = {
    sbiGlTableWlist: [
        { name: 'product_class_id', columnId: 17661, type: 'INT', word: [] },
        { name: 'product_subcategory', columnId: 17659, type: 'VARCHAR', word: [] }
    ],
    metaTable: { tableId: 1352, name: 'product_class', deleted: false },
    metaSource: { sourceId: 293, name: 'foodmart_demo', type: 'database', url: 'jdbc:mysql://161.27.213.106:3306/foodmart_demo', location: null, sourceSchema: null, sourceCatalogue: null, role: null },
    words: [{ WORD_ID: 262, WORD: 'Customer' }]
}

const mockedSelectedWords = [
    { WORD_ID: 264, WORD: 'Product Sales' },
    { WORD_ID: 262, WORD: 'Customer' }
]

const mockedFilteredResponse = {
    document: [{ DOCUMENT_ID: 3219, DOCUMENT_LABEL: 'Accident_NYC' }],
    document_size: 1,
    word: [
        { WORD_ID: 262, WORD: 'Customer' },
        { WORD_ID: 265, WORD: 'Inventory' },
        { WORD_ID: 264, WORD: 'Product Sales' },
        { WORD_ID: 263, WORD: 'Product Store' },
        { WORD_ID: 261, WORD: 'Store City' },
        { WORD_ID: 260, WORD: 'Store Country' },
        { WORD_ID: 259, WORD: 'Store Sales' },
        { WORD_ID: 267, WORD: 'Unit Costs' },
        { WORD_ID: 266, WORD: 'Unit Sales' }
    ],
    word_size: 9,
    dataset: [{ DATASET_ID: 117, DATASET_NM: 'AUDIT_02', DATASET_ORG: 'DEMO' }],
    dataset_size: 1,
    bness_cls: [
        { BC_ID: 484, META_MODEL_NAME: 'Expenses', BC_NAME: null },
        { BC_ID: 488, META_MODEL_NAME: 'Expenses', BC_NAME: null }
    ],
    bness_cls_size: 2,
    table: [
        { TABLE_ID: 1352, META_SOURCE_NAME: 'foodmart_demo', TABLE_NM: 'product_class' },
        { TABLE_ID: 1357, META_SOURCE_NAME: 'foodmart_demo', TABLE_NM: 'account' },
        { TABLE_ID: 1379, META_SOURCE_NAME: 'foodmart_demo', TABLE_NM: 'department' }
    ],
    table_size: 3,
    Status: 'OK'
}

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/documents/mockedDocument':
                return Promise.resolve({ data: mockedDocumentInfo })
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/documents/1/roles':
                return Promise.resolve({ data: mockedRoles })
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/glossary/getDataSetInfo?DATASET_ID=1&ORGANIZATION=DEMO':
                return Promise.resolve({ data: mockedDatasetInfo })
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/glossary/getMetaBcInfo?META_BC_ID=1':
                return Promise.resolve({ data: mockedBusinessClassInfo })
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/glossary/getMetaTableInfo?META_TABLE_ID=1':
                return Promise.resolve({ data: mockedTableInfo })
            default:
                return Promise.resolve({ data: [] })
        }
    }),
    post: vi.fn().mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/glossary/loadNavigationItem':
                return Promise.resolve({ data: mockedFilteredResponse })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

const factory = () => {
    return mount(GlossaryUsageDetail, {
        global: {
            stubs: { GlossaryUsageNavigationCard: true, GlossaryUsageLinkCard: true, ProgressBar },
            mocks: {
                $t: (msg) => msg,
                $http
            }
        }
    })
}

describe('Glossary Usage Detail', () => {
    it('loads and emits document info after click on info button', async () => {
        const wrapper = factory()

        await wrapper.vm.showDocumentInfo({ id: 1, label: 'mockedDocument' })

        expect(wrapper.emitted()).toHaveProperty('infoClicked')
        expect(wrapper.emitted().infoClicked[0][0]).toStrictEqual({ data: mockedDocumentInfo, type: 'document' })
    })
    it('loads and emits dataset info after click on info button', async () => {
        const wrapper = factory()

        await wrapper.vm.showDatasetInfo({ id: 1, label: 'mockedDataset', organization: 'DEMO', type: 'dataset' })

        expect(wrapper.emitted()).toHaveProperty('infoClicked')
        expect(wrapper.emitted().infoClicked[0][0]).toStrictEqual({ data: mockedDatasetInfo, type: 'dataset' })
    })
    it('loads and emits business class info after click on info button', async () => {
        const wrapper = factory()

        await wrapper.vm.showBusinessClassInfo({ id: 1, label: 'Mocked.BusinessModel', type: 'businessClass' })

        expect(wrapper.emitted()).toHaveProperty('infoClicked')
        expect(wrapper.emitted().infoClicked[0][0]).toStrictEqual({ data: mockedBusinessClassInfo, type: 'businessClass' })
    })
    it('loads and emits table info after click on info button', async () => {
        const wrapper = factory()

        await wrapper.vm.showTableInfo({ id: 1, label: 'mocked.table', type: 'table' })

        expect(wrapper.emitted()).toHaveProperty('infoClicked')
        expect(wrapper.emitted().infoClicked[0][0]).toStrictEqual({ data: mockedTableInfo, type: 'table' })
    })
})

describe('Glossary Usage Tree', () => {
    it('filters the navigation when an element in the tree is selected', async () => {
        const wrapper = factory()

        await wrapper.setProps({ selectedWords: mockedSelectedWords })

        await wrapper.vm.loadNavigationItems('all', 'word')

        expect(wrapper.vm.documents).toStrictEqual([{ id: 3219, label: 'Accident_NYC', type: 'document' }])
        expect(wrapper.vm.datasets).toStrictEqual([{ id: 117, label: 'AUDIT_02', organization: 'DEMO', type: 'dataset' }])
        expect(wrapper.vm.businessClasses).toStrictEqual([
            { id: 484, label: 'Expenses.null', type: 'businessClass' },
            { id: 488, label: 'Expenses.null', type: 'businessClass' }
        ])
        expect(wrapper.vm.tables).toStrictEqual([
            { id: 1352, label: 'foodmart_demo.product_class', type: 'table' },
            { id: 1357, label: 'foodmart_demo.account', type: 'table' },
            { id: 1379, label: 'foodmart_demo.department', type: 'table' }
        ])
        expect(wrapper.emitted()).toHaveProperty('wordsFiltered')
    })
})
