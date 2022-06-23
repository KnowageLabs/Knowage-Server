import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import FunctionsCatalogPreviewDialog from './FunctionsCatalogPreviewDialog.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

const mockedFunction = {
    id: '9acfe566-36f1-470d-bb96-00542b9ff1e7',
    name: 'Skewness',
    description:
        '<div>The function compute the sample skewness of a column. It measure the asymmetry of the distribution of a real-valued variable around its mean, so it is usefull to understand how data are distributed.</div><div>For normally distributed data the skewness should be about zero, for unimodal continous distribution a skewness greater than zero means that there is more weight in the right tail of the distribution.</div><div><br></div><div><div>In the function no sorting or grouping is performed.<br></div><div><br></div><div><span style="font-weight: bold;">Input variables:</span></div><div>&nbsp; &nbsp;&nbsp;<span style="font-weight: bold;">column</span>: the data on which apply the skewness</div><div>&nbsp; &nbsp;&nbsp;<span style="font-weight: bold;">bias</span>:&nbsp;<span style="font-style: italic;">boolean</span>, default True</div><div>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; If False, then the calculations are corrected for statistical bias.</div><div>&nbsp; &nbsp;&nbsp;<span style="font-weight: 700;">nan_policy</span>: {\'propagate\', \'raise\', \'omit\'}, default \'propagate\'</div><div>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;Defines how to handle when input contains nan. The following options are available (default is ‘propagate’):</div><div><ul><li>‘propagate’: returns nan</li><li>‘raise’: throws an error</li><li>‘omit’: performs the calculations ignoring nan values</li></ul></div><div>&nbsp; &nbsp;&nbsp;</div><div><span style="font-weight: bold;">Output:</span></div><div>&nbsp; &nbsp; The skewness of the column selected. The value is repeted in all the dataset rows.</div></div>',
    benchmarks: '',
    language: 'Python',
    family: 'online',
    onlineScript: 'from scipy.stats import skew\nimport pandas as pd\n\ninput_col = ${column}\ninput_bias = eval(${bias})\ninput_nan_policy = ${nan_policy}\n\nres_value = skew(input_col, bias=input_bias, nan_policy=input_nan_policy)\n  \n${skewness_col} = pd.Series(input_col.size*[res_value])',
    offlineScriptTrain: '',
    offlineScriptUse: '',
    owner: 'demo_admin',
    label: 'Skewness',
    type: 'Utilities',
    keywords: ['skewness', 'statistics'],
    inputVariables: [
        {
            name: 'bias',
            type: 'STRING',
            value: ''
        },
        {
            name: 'nan_policy',
            type: 'STRING',
            value: ''
        }
    ],
    inputColumns: [
        {
            name: 'column',
            type: 'NUMBER'
        }
    ],
    outputColumns: [
        {
            name: 'skewness_col',
            fieldType: 'MEASURE',
            type: 'NUMBER'
        }
    ]
}

const mockedDataset = {
    id: 3,
    label: 'DOC_PROD_WEIGHT',
    name: 'Product weights',
    description: 'Product weights',
    usedByNDocs: 0,
    catTypeVn: 'General',
    catTypeId: 399,
    pars: [],
    meta: {
        dataset: [
            {
                pname: 'resultNumber',
                pvalue: '5'
            }
        ],
        columns: [
            {
                column: 'brand_name',
                pname: 'Type',
                pvalue: 'java.lang.String'
            },
            {
                column: 'brand_name',
                pname: 'fieldType',
                pvalue: 'ATTRIBUTE'
            },
            {
                column: 'brand_name',
                pname: 'fieldAlias',
                pvalue: 'brand_name'
            },
            {
                column: 'total_units',
                pname: 'Type',
                pvalue: 'java.math.BigDecimal'
            },
            {
                column: 'total_units',
                pname: 'fieldType',
                pvalue: 'MEASURE'
            },
            {
                column: 'total_units',
                pname: 'fieldAlias',
                pvalue: 'total_units'
            },
            {
                column: 'gross_weight',
                pname: 'Type',
                pvalue: 'java.lang.Double'
            },
            {
                column: 'gross_weight',
                pname: 'fieldType',
                pvalue: 'MEASURE'
            },
            {
                column: 'gross_weight',
                pname: 'fieldAlias',
                pvalue: 'gross_weight'
            },
            {
                column: 'net_weight',
                pname: 'Type',
                pvalue: 'java.lang.Double'
            },
            {
                column: 'net_weight',
                pname: 'fieldType',
                pvalue: 'MEASURE'
            },
            {
                column: 'net_weight',
                pname: 'fieldAlias',
                pvalue: 'net_weight'
            }
        ]
    },
    dsVersions: [],
    dsTypeCd: 'Query',
    userIn: 'demo_admin',
    versNum: 4,
    dateIn: '2017-06-01T12:10:50.000+02:00',
    query: "select distinct brand_name, sum(units_per_case) as total_units ,\nsum(gross_weight) as gross_weight ,\nsum(net_weight) as net_weight\nfrom product\nwhere brand_name in ('Johnson','Urban','Ebony','Colony','American')\ngroup by brand_name\norder by brand_name",
    queryScript: '',
    queryScriptLanguage: '',
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

vi.mock('axios')

const $http = {
    post: vi.fn().mockImplementation(() => Promise.reject({ message: '100' }))
}

const $store = {
    commit: jest.fn()
}

const factory = () => {
    return mount(FunctionsCatalogPreviewDialog, {
        props: {
            propFunction: mockedFunction
        },
        global: {
            directives: {
                tooltip() {}
            },
            stubs: {
                Button,
                Dialog,
                FunctionsCatalogConfiguratorTab: true,
                FunctionsCatalogPreviewTable: true,
                FunctionsCatalogPreviewWarningDialog: true,
                ProgressBar,
                TabView,
                TabPanel,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $http
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Functons Catalog Preview Dialog', () => {
    it('should show an error message if the function execution fails', async () => {
        const wrapper = factory()

        expect(wrapper.vm.checkColumnsConfiguration()).toBe(false)
        await wrapper.vm.goToPreview()
        expect(wrapper.vm.warningMessage).toBe('managers.functionsCatalog.datasetColumnsError')

        wrapper.vm.propFunction.inputColumns[0].dsColumn = 'brand_name'
        expect(wrapper.vm.checkColumnsConfiguration()).toBe(true)

        expect(wrapper.vm.checkVariablesConfiguration()).toBe(false)
        await wrapper.vm.goToPreview()
        expect(wrapper.vm.warningMessage).toBe('managers.functionsCatalog.inputVariablesError')

        wrapper.vm.propFunction.inputVariables[0].value = 'test'
        wrapper.vm.propFunction.inputVariables[1].value = 'test'
        expect(wrapper.vm.checkVariablesConfiguration()).toBe(true)

        await wrapper.vm.goToPreview()
        expect(wrapper.vm.warningMessage).toBe('managers.functionsCatalog.environmentError')

        wrapper.vm.environment = 'python.default.environment.url'
        wrapper.vm.selectedDataset = mockedDataset
        await wrapper.vm.goToPreview()
        expect(wrapper.vm.warningTitle).toBe('managers.functionsCatalog.dataServiceErrorTitle')
        expect(wrapper.vm.warningMessage).toBe('managers.functionsCatalog.dataServiceErrorMessage')
    })
})
