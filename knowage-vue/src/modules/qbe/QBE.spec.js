import { mount, flushPromises } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Chip from 'primevue/chip'
import InputSwitch from 'primevue/inputswitch'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
import ScrollPanel from 'primevue/scrollpanel'
import Menu from 'primevue/contextmenu'
import QBE from './QBE.vue'
import ProgressBar from 'primevue/progressbar'
import PrimeVue from 'primevue/config'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Toolbar from 'primevue/toolbar'
import Tooltip from 'primevue/tooltip'

const mockedQBE = [
    {
        id: 153,
        label: 'Bojan',
        name: 'Bojan',
        description: null,
        usedByNDocs: 0,
        catTypeVn: null,
        catTypeId: null,
        pars: [],
        meta: {
            dataset: [
                {
                    pname: 'resultNumber',
                    pvalue: '0'
                }
            ],
            columns: [
                {
                    column: 'Brand name',
                    pname: 'Type',
                    pvalue: 'java.lang.String'
                },
                {
                    column: 'Brand name',
                    pname: 'fieldType',
                    pvalue: 'ATTRIBUTE'
                },
                {
                    column: 'Brand name',
                    pname: 'fieldAlias',
                    pvalue: 'Brand name'
                },
                {
                    column: 'Brand name',
                    pname: 'uniqueName',
                    pvalue: 'it.eng.knowage.inventory.Product:brand_name'
                },
                {
                    column: 'Brand name',
                    pname: 'visible',
                    pvalue: 'true'
                },
                {
                    column: 'Brand name',
                    pname: 'aggregationFunction',
                    pvalue: 'NONE'
                },
                {
                    column: 'Brand name',
                    pname: 'calculated',
                    pvalue: 'false'
                },
                {
                    column: 'Product name',
                    pname: 'Type',
                    pvalue: 'java.lang.String'
                },
                {
                    column: 'Product name',
                    pname: 'fieldType',
                    pvalue: 'ATTRIBUTE'
                },
                {
                    column: 'Product name',
                    pname: 'fieldAlias',
                    pvalue: 'Product name'
                },
                {
                    column: 'Product name',
                    pname: 'uniqueName',
                    pvalue: 'it.eng.knowage.inventory.Product:product_name'
                },
                {
                    column: 'Product name',
                    pname: 'visible',
                    pvalue: 'true'
                },
                {
                    column: 'Product name',
                    pname: 'aggregationFunction',
                    pvalue: 'NONE'
                },
                {
                    column: 'Product name',
                    pname: 'calculated',
                    pvalue: 'false'
                }
            ]
        },
        dsVersions: [],
        dsTypeCd: 'Qbe',
        userIn: 'demo_user',
        versNum: 9,
        dateIn: '2022-01-31T17:27:47.000+01:00',
        qbeJSONQuery:
            '{"catalogue":{"queries":[{"id":"q1","name":"Main","fields":[{"id":"it.eng.knowage.inventory.Product:brand_name","alias":"Brand name","type":"datamartField","fieldType":"attribute","entity":"Product","field":"Brand name","funct":"NONE","color":"#F46036","group":true,"order":"NONE","include":true,"inUse":true,"visible":true,"iconCls":"attribute","dataType":"java.lang.String","format":"#,###","longDescription":"Product : Brand name","distinct":false,"leaf":true},{"id":"it.eng.knowage.inventory.Product:product_name","alias":"Product name","type":"datamartField","fieldType":"attribute","entity":"Product","field":"Product name","funct":"NONE","color":"#F46036","group":true,"order":"NONE","include":true,"inUse":true,"visible":true,"iconCls":"attribute","dataType":"java.lang.String","format":"#,###","longDescription":"Product : Product name","distinct":false,"leaf":true}],"distinct":false,"filters":[{"filterId":"Filter1","filterDescripion":"Filter1","filterInd":1,"promptable":false,"leftOperandValue":"it.eng.knowage.inventory.Product:brand_name","leftOperandDescription":"Product : Brand name","leftOperandLongDescription":"Product : Brand name","leftOperandType":"Field Content","leftOperandDefaultValue":null,"leftOperandLastValue":null,"leftOperandAlias":"Brand name","leftOperandDataType":"","operator":"EQUALS TO","rightOperandValue":["Test"],"rightOperandDescription":"Test","rightOperandLongDescription":"","rightOperandType":"Static Content","rightType":"manual","rightOperandDefaultValue":[""],"rightOperandLastValue":[""],"rightOperandAlias":null,"rightOperandDataType":"","booleanConnector":"AND","deleteButton":false,"color":"#F46036","entity":"Product"},{"filterId":"Filter2","filterDescripion":"Filter2","filterInd":2,"promptable":false,"leftOperandValue":"it.eng.knowage.inventory.Product:brand_name","leftOperandDescription":"Product : Brand name","leftOperandLongDescription":"Product : Brand name","leftOperandType":"Field Content","leftOperandDefaultValue":null,"leftOperandLastValue":null,"leftOperandAlias":"Brand name","leftOperandDataType":"","operator":"EQUALS TO","rightOperandValue":["Bla"],"rightOperandDescription":"Bla","rightOperandLongDescription":"","rightOperandType":"Static Content","rightType":"manual","rightOperandDefaultValue":[""],"rightOperandLastValue":[""],"rightOperandAlias":null,"rightOperandDataType":"","booleanConnector":"AND","deleteButton":false,"color":"#F46036","entity":"Product"},{"filterId":"Filter3","filterDescripion":"Filter3","filterInd":3,"promptable":false,"leftOperandValue":"it.eng.knowage.inventory.Product:product_name","leftOperandDescription":"Product : Product name","leftOperandLongDescription":"Product : Product name","leftOperandType":"Field Content","leftOperandDefaultValue":null,"leftOperandLastValue":null,"leftOperandAlias":"Product name","leftOperandDataType":"","operator":"EQUALS TO","rightOperandValue":["dfsfsd"],"rightOperandDescription":"dfsfsd","rightOperandLongDescription":"","rightOperandType":"Static Content","rightType":"manual","rightOperandDefaultValue":[""],"rightOperandLastValue":[""],"rightOperandAlias":null,"rightOperandDataType":"","booleanConnector":"AND","deleteButton":false,"color":"#F46036","entity":"Product"},{"filterId":"Filter4","filterDescripion":"Filter4","filterInd":4,"promptable":false,"leftOperandValue":"it.eng.knowage.inventory.Product:product_name","leftOperandDescription":"Product : Product name","leftOperandLongDescription":"Product : Product name","leftOperandType":"Field Content","leftOperandDefaultValue":null,"leftOperandLastValue":null,"leftOperandAlias":"Product name","leftOperandDataType":"","operator":"EQUALS TO","rightOperandValue":["fdsdfs"],"rightOperandDescription":"fdsdfs","rightOperandLongDescription":"","rightOperandType":"Static Content","rightType":"manual","rightOperandDefaultValue":[""],"rightOperandLastValue":[""],"rightOperandAlias":null,"rightOperandDataType":"","booleanConnector":"AND","deleteButton":false,"color":"#F46036","entity":"Product"}],"calendar":{},"expression":{"type":"NODE_OP","value":"AND","childNodes":[{"type":"NODE_CONST","childNodes":[],"value":"$F{Filter4}","details":{"leftOperandAlias":"Product name","operator":"EQUALS TO","entity":"Product","rightOperandValue":"fdsdfs"}},{"type":"NODE_OP","value":"AND","childNodes":[{"type":"NODE_CONST","childNodes":[],"value":"$F{Filter3}","details":{"leftOperandAlias":"Product name","operator":"EQUALS TO","entity":"Product","rightOperandValue":"dfsfsd"}},{"type":"NODE_OP","childNodes":[{"type":"NODE_CONST","childNodes":[],"value":"$F{Filter2}","details":{"leftOperandAlias":"Brand name","operator":"EQUALS TO","entity":"Product","rightOperandValue":"Bla"}},{"type":"NODE_CONST","childNodes":[],"value":"$F{Filter1}","details":{"leftOperandAlias":"Brand name","operator":"EQUALS TO","entity":"Product","rightOperandValue":"Test"}}],"value":"AND"}]}]},"isNestedExpression":false,"havings":[],"graph":[],"relationRoles":[],"subqueries":[]}]}}',
        qbeDataSource: 'Foodmart',
        qbeDataSourceId: 1,
        qbeDatamarts: 'Inventory',
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
        owner: 'demo_user',
        scopeCd: 'USER',
        scopeId: 186,
        tags: [],
        canLoadData: true,
        actions: [
            {
                name: 'detaildataset',
                description: 'Dataset detail'
            },
            {
                name: 'delete',
                description: 'Delete dataset'
            },
            {
                name: 'qbe',
                description: 'Show Qbe'
            }
        ]
    }
]

const mockedField = {
    id: 'it.eng.knowage.inventory.Product_class:product_class_id',
    text: 'Product class id',
    iconCls: 'attribute',
    dataType: 'java.lang.Integer',
    aggtype: 'SUM',
    format: '#,###',
    leaf: true,
    qtip: 'Product class id',
    attributes: {
        iconCls: 'attribute',
        type: 'field',
        entity: 'Product class',
        field: 'Product class id',
        longDescription: 'Product class : Product class id'
    },
    color: '#F46036'
}

jest.mock('axios')
const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/Bojan`:
                return Promise.resolve({ data: [mockedQBE] })
            default:
                return Promise.resolve({ data: [] })
        }
    }),

    post: axios.post.mockImplementation((url) => {
        switch (url) {
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

const $route = { name: '' }

const $store = {
    state: {
        user: {}
    }
}

const factory = () => {
    return mount(QBE, {
        props: {
            id: '1'
        },
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue],
            stubs: {
                Button,
                Chip,
                KnOverlaySpinnerPanel,
                InputSwitch,
                Menu,
                ProgressBar,
                ScrollPanel,
                TabView,
                TabPanel,
                Toolbar,
                Tooltip,
                QBEAdvancedFilterDialog: true,
                QBEFilterDialog: true,
                QBEHavingDialog: true,
                QBESimpleTable: true,
                QBESqlDialog: true,
                QBERelationDialog: true,
                QBEParamDialog: true,
                QBESavingDialog: true,
                QBESmartTable: true,
                ExpandableEntity: true,
                SubqueryEntity: true,
                QBEJoinDefinitionDialog: true,
                KnParameterSidebar: true,
                QBEPreviewDialog: true
            },
            mocks: {
                $t: (msg) => msg,
                $http,
                $route,
                $store
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('QBE', () => {
    it('shows progress bar when loading', async () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
    })
})
