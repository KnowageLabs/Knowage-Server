import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import Column from 'primevue/column'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import DatasetManagementDetailCard from './DatasetManagementDetailCard.vue'
import ProgressBar from 'primevue/progressbar'
import Dropdown from 'primevue/dropdown'
import Chips from 'primevue/chips'
import Toolbar from 'primevue/toolbar'

const mockedDataset = {
    id: 5,
    label: 'DOC_measures_parallell_DEF',
    name: 'Sales and Costs by Product Name',
    description: 'Desc',
    usedByNDocs: 0,
    catTypeVn: 'General',
    catTypeId: 399,
    pars: [
        {
            name: 'par_category',
            type: 'String',
            defaultValue: '',
            multiValue: false
        }
    ],
    meta: {
        dataset: [
            {
                pname: 'resultNumber',
                pvalue: '0'
            }
        ],
        columns: [
            {
                column: 'revenues',
                pname: 'fieldAlias',
                pvalue: 'revenues'
            }
        ]
    },
    dsVersions: [],
    dsTypeCd: 'Query',
    userIn: 'demo_admin',
    versNum: 10,
    dateIn: '2019-02-05T18:48:10.000+01:00',
    query: 'select a.product_id, \na.product_name as "product name", \nb.product_department as "product department", \nb.product_category as "product category",\nb.product_family as "product family",\n sum(c.store_sales) as sales, sum(c.store_cost) as costs,\n sum(c.unit_sales) as units, sum(c.store_sales) - sum(c.store_cost) as revenues\nfrom product a, product_class b, sales_fact c \nwhere \na.product_class_id=b.product_class_id \nand a.product_id=c.product_id \n and product_family=$P{par_family}\n__PH__\ngroup by a.product_id,a.product_name,b.product_department,b.product_family\norder by sum(c.store_sales)\nlimit 100',
    queryScript:
        'var filters = "";\n\nif (parameters.get(\'par_department\')!=null && parameters.get(\'par_category\')==null){\nfilters += "and product_department=$P{par_department}";\n}\nelse if(parameters.get(\'par_department\')!=null && parameters.get(\'par_category\')!=null){\nfilters += "and product_department=$P{par_department} and product_category=$P{par_category}";\n}\nquery = query.replace("__PH__", filters);',
    queryScriptLanguage: 'ECMAScript',
    dataSource: 'Foodmart',
    trasfTypeCd: null,
    pivotColName: null,
    pivotColValue: null,
    pivotRowName: null,
    pivotIsNumRows: false,
    isPersisted: false,
    isPersistedHDFS: false,
    persistTableName: '',
    isScheduled: false,
    startDate: null,
    endDate: null,
    schedulingCronLine: null,
    isRealtime: false,
    isIterable: true,
    owner: 'demoadmin',
    scopeCd: 'TECHNICAL',
    scopeId: 188,
    tags: [],
    canLoadData: true,
    actions: [
        {
            name: 'detaildataset',
            description: 'Dataset detail'
        },
        {
            name: 'qbe',
            description: 'Show Qbe'
        }
    ]
}

const mockedScopeTypes = [
    {
        VALUE_NM: 'USER',
        VALUE_DS: 'Dataset scope',
        VALUE_ID: 186,
        VALUE_CD: 'USER'
    },
    {
        VALUE_NM: 'ENTERPRISE',
        VALUE_DS: 'Dataset scope',
        VALUE_ID: 187,
        VALUE_CD: 'ENTERPRISE'
    }
]
const mockedCategoryTypes = [
    {
        VALUE_NM: 'Sales',
        VALUE_DS: 'Sales',
        VALUE_ID: 151,
        VALUE_CD: 'Sales'
    },
    {
        VALUE_NM: 'Inventory',
        VALUE_DS: 'Inventory',
        VALUE_ID: 152,
        VALUE_CD: 'Inventory'
    }
]
const mockedSelectedDatasetVersions = [
    {
        type: 'Query',
        userIn: 'demo_admin',
        versNum: 1,
        dateIn: '2016-10-23T14:21:13.000+02:00',
        dsId: 5
    },
    {
        type: 'Query',
        userIn: 'demo_admin',
        versNum: 2,
        dateIn: '2016-10-26T14:43:38.000+02:00',
        dsId: 5
    }
]

vi.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() =>
        Promise.resolve({
            data: []
        })
    ),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}

const $confirm = {
    require: vi.fn()
}

const $store = {
    commit: jest.fn()
}

const $router = {
    push: jest.fn()
}

const factory = (scopeTypes, categoryTypes, selectedDataset, selectedDatasetVersions, loading) => {
    return mount(DatasetManagementDetailCard, {
        props: {
            scopeTypes,
            categoryTypes,
            selectedDataset,
            selectedDatasetVersions,
            loading
        },
        global: {
            stubs: {
                Button,
                Column,
                Card,
                DataTable,
                KnValidationMessages,
                InputText,
                ProgressBar,
                Toolbar,
                Dropdown,
                Chips
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $router,
                $http
            }
        }
    })
}

describe('Dataset managament detail', () => {
    it('should show progress bar when loading', () => {
        const wrapper = factory(mockedScopeTypes, mockedCategoryTypes, mockedDataset, mockedSelectedDatasetVersions, true)

        expect(wrapper.find('[data-test="versions-loading"]').exists()).toBe(true)
    })
    it('should show filled inputs if loaded from the list', () => {
        const wrapper = factory(mockedScopeTypes, mockedCategoryTypes, mockedDataset, mockedSelectedDatasetVersions, false)
        wrapper.vm.dataset = mockedDataset

        expect(wrapper.find('[data-test="label-input"]').wrapperElement._value).toBe('DOC_measures_parallell_DEF')
        expect(wrapper.find('[data-test="name-input"]').wrapperElement._value).toBe('Sales and Costs by Product Name')
        expect(wrapper.find('[data-test="description-input"]').wrapperElement._value).toBe('Desc')
    })
    it('should disable the save button if no type or name is set for the dataset', () => {
        const wrapper = factory(mockedScopeTypes, mockedCategoryTypes, mockedDataset, mockedSelectedDatasetVersions, false)
        wrapper.vm.dataset = mockedDataset

        expect(wrapper.find('[data-test="label-input"]').wrapperElement._value).toBe('DOC_measures_parallell_DEF')
        expect(wrapper.vm.v$.$invalid).toBe(false)

        wrapper.vm.dataset.label = null
        expect(wrapper.vm.v$.$invalid).toBe(true)
    })
})
