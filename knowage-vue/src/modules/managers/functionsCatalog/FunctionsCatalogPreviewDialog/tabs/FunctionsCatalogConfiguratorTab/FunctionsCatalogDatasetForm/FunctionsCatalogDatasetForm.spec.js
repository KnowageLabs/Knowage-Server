import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { nextTick } from 'vue'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Card from 'primevue/card'
import Column from 'primevue/column'
import Dropdown from 'primevue/dropdown'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import FunctionsCatalogDatasetForm from './FunctionsCatalogDatasetForm.vue'
import FunctionsCatalogDatasetEnvironmentTable from './FunctionsCatalogDatasetEnvironmentTable.vue'
import FunctionsCatalogParametersForm from './FunctionsCatalogParametersForm.vue'
import Toolbar from 'primevue/toolbar'

const mockedDataset = {
    id: 5,
    label: 'DOC_measures_parallell_DEF',
    name: 'Sales and Costs by Product Name',
    description: null,
    usedByNDocs: 0,
    catTypeVn: 'General',
    catTypeId: 399,
    pars: [
        {
            name: 'par_category',
            type: 'String',
            defaultValue: 'category default value',
            multiValue: false,
            value: 'category default value'
        },
        {
            name: 'par_department',
            type: 'String',
            defaultValue: '',
            multiValue: false,
            value: ''
        },
        {
            name: 'par_family',
            type: 'String',
            defaultValue: '',
            multiValue: false,
            value: ''
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
                column: 'product_id',
                pname: 'Type',
                pvalue: 'java.lang.Integer'
            },
            {
                column: 'product_id',
                pname: 'fieldType',
                pvalue: 'ATTRIBUTE'
            },
            {
                column: 'product_id',
                pname: 'fieldAlias',
                pvalue: 'product_id'
            },
            {
                column: 'product name',
                pname: 'Type',
                pvalue: 'java.lang.String'
            },
            {
                column: 'product name',
                pname: 'fieldType',
                pvalue: 'ATTRIBUTE'
            },
            {
                column: 'product name',
                pname: 'fieldAlias',
                pvalue: 'product name'
            },
            {
                column: 'product department',
                pname: 'Type',
                pvalue: 'java.lang.String'
            },
            {
                column: 'product department',
                pname: 'fieldType',
                pvalue: 'ATTRIBUTE'
            },
            {
                column: 'product department',
                pname: 'fieldAlias',
                pvalue: 'product department'
            },
            {
                column: 'product category',
                pname: 'Type',
                pvalue: 'java.lang.String'
            },
            {
                column: 'product category',
                pname: 'fieldType',
                pvalue: 'ATTRIBUTE'
            },
            {
                column: 'product category',
                pname: 'fieldAlias',
                pvalue: 'product category'
            },
            {
                column: 'product family',
                pname: 'Type',
                pvalue: 'java.lang.String'
            },
            {
                column: 'product family',
                pname: 'fieldType',
                pvalue: 'ATTRIBUTE'
            },
            {
                column: 'product family',
                pname: 'fieldAlias',
                pvalue: 'product family'
            },
            {
                column: 'sales',
                pname: 'Type',
                pvalue: 'java.math.BigDecimal'
            },
            {
                column: 'sales',
                pname: 'fieldType',
                pvalue: 'MEASURE'
            },
            {
                column: 'sales',
                pname: 'fieldAlias',
                pvalue: 'sales'
            },
            {
                column: 'costs',
                pname: 'Type',
                pvalue: 'java.math.BigDecimal'
            },
            {
                column: 'costs',
                pname: 'fieldType',
                pvalue: 'MEASURE'
            },
            {
                column: 'costs',
                pname: 'fieldAlias',
                pvalue: 'costs'
            },
            {
                column: 'units',
                pname: 'Type',
                pvalue: 'java.math.BigDecimal'
            },
            {
                column: 'units',
                pname: 'fieldType',
                pvalue: 'MEASURE'
            },
            {
                column: 'units',
                pname: 'fieldAlias',
                pvalue: 'units'
            },
            {
                column: 'revenues',
                pname: 'Type',
                pvalue: 'java.math.BigDecimal'
            },
            {
                column: 'revenues',
                pname: 'fieldType',
                pvalue: 'MEASURE'
            },
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

const mockedLibraries = [
    {
        name: 'Werkzeug',
        version: '2.0.1'
    },
    {
        name: 'urllib3',
        version: '1.25.11'
    }
]

const mockedFunction = {
    id: '4e230432-2332-4efa-97cc-66a42a29523b',
    name: 'echo_function',
    description: 'Echoes a string column',
    benchmarks: 'Works well in any scenario',
    language: 'Python',
    family: null,
    onlineScript: '${echo_out} = ${echo_in}',
    offlineScriptTrainModel: '',
    offlineScriptUseModel: '',
    owner: 'demo_admin',
    label: 'echo_function',
    type: 'Machine Learning',
    keywords: ['echo'],
    inputVariables: [],
    inputColumns: [
        {
            name: 'echo_in',
            type: 'STRING'
        }
    ],
    outputColumns: [
        {
            name: 'echo_out',
            fieldType: 'ATTRIBUTE',
            type: 'STRING'
        }
    ]
}

const factory = () => {
    return mount(FunctionsCatalogDatasetForm, {
        props: {
            selectedDataset: mockedDataset,
            libraries: mockedLibraries,
            propFunction: mockedFunction
        },
        global: {
            directives: {
                tooltip() {}
            },
            stubs: {
                Accordion,
                AccordionTab,
                Card,
                Column,
                Dropdown,
                DataTable,
                InputText,
                FunctionsCatalogDatasetFormColumnsTable: true,
                FunctionsCatalogDatasetFormVariablesTable: true,
                FunctionsCatalogDatasetEnvironmentTable,
                FunctionsCatalogParametersForm,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Functions Catalog Dataset Form', () => {
    it('should show the list of available libraries', async () => {
        const wrapper = factory()

        wrapper.vm.selectedEnvironment = 'python.default.environment.url'
        await nextTick()

        expect(wrapper.html()).toContain('Werkzeug')
        expect(wrapper.html()).toContain('2.0.1')
        expect(wrapper.html()).toContain('urllib3')
        expect(wrapper.html()).toContain('1.25.11')
    })

    it('should have parameters set if the dataset has parameters', () => {
        const wrapper = factory()

        expect(wrapper.vm.selectedDataset.pars.length).toBe(3)
        expect(wrapper.html()).toContain('par_category')
        expect(wrapper.html()).toContain('par_department')
        expect(wrapper.html()).toContain('par_family')
        expect(wrapper.find('[data-test="parameter-input"]').wrapperElement._value).toBe('category default value')
    })
})
