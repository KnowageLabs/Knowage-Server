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

const mockedQBE = {
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
                pvalue: '1560'
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
    versNum: 11,
    dateIn: '2022-02-01T15:49:16.000+01:00',
    qbeJSONQuery:
        '{"catalogue":{"queries":[{"id":"q1","name":"Main","fields":[{"id":"it.eng.knowage.inventory.Product:brand_name","alias":"Brand name","type":"datamartField","fieldType":"attribute","entity":"Product","field":"Brand name","funct":"NONE","color":"#F46036","group":true,"order":"NONE","include":true,"inUse":true,"visible":true,"iconCls":"attribute","dataType":"java.lang.String","format":"#,###","longDescription":"Product : Brand name","distinct":false,"leaf":true},{"id":"it.eng.knowage.inventory.Product:product_name","alias":"Product name","type":"datamartField","fieldType":"attribute","entity":"Product","field":"Product name","funct":"NONE","color":"#F46036","group":true,"order":"NONE","include":true,"inUse":true,"visible":true,"iconCls":"attribute","dataType":"java.lang.String","format":"#,###","longDescription":"Product : Product name","distinct":false,"leaf":true}],"distinct":false,"filters":[],"calendar":{},"expression":{},"isNestedExpression":false,"havings":[],"graph":[],"relationRoles":[],"subqueries":[]}]}}',
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

const mockedQuery = {
    id: 'q1',
    name: 'Main',
    fields: [
        {
            id: 'it.eng.knowage.inventory.Product:brand_name',
            alias: 'Brand name',
            type: 'datamartField',
            fieldType: 'attribute',
            entity: 'Product',
            field: 'Brand name',
            funct: 'NONE',
            color: '#F46036',
            group: true,
            order: 'NONE',
            include: true,
            inUse: true,
            visible: true,
            iconCls: 'attribute',
            dataType: 'java.lang.String',
            format: '#,###',
            longDescription: 'Product : Brand name',
            distinct: false,
            leaf: true
        },
        {
            id: 'it.eng.knowage.inventory.Product:product_name',
            alias: 'Product name',
            type: 'datamartField',
            fieldType: 'attribute',
            entity: 'Product',
            field: 'Product name',
            funct: 'NONE',
            color: '#F46036',
            group: true,
            order: 'NONE',
            include: true,
            inUse: true,
            visible: true,
            iconCls: 'attribute',
            dataType: 'java.lang.String',
            format: '#,###',
            longDescription: 'Product : Product name',
            distinct: false,
            leaf: true
        }
    ],
    distinct: false,
    filters: [],
    calendar: {},
    expression: {},
    isNestedExpression: false,
    havings: [],
    graph: [],
    relationRoles: [],
    subqueries: []
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
    it('shows progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
    })

    it('removes all column when clicking on clear table', async () => {
        const wrapper = factory()

        wrapper.vm.selectedQuery = JSON.parse(JSON.stringify(mockedQuery))

        expect(wrapper.vm.selectedQuery.fields.length).toBe(2)

        wrapper.vm.deleteAllSelectedFields()

        expect(wrapper.vm.selectedQuery.fields.length).toBe(0)
    })

    it('adds a field to the list when dragging a field in the list', async () => {
        const wrapper = factory()
        const mockedField = {
            id: 'it.eng.knowage.inventory.Product:gross_weight',
            text: 'Gross weight',
            iconCls: 'attribute',
            dataType: 'java.lang.Double',
            aggtype: 'SUM',
            format: '#,###',
            leaf: true,
            qtip: 'Gross weight',
            attributes: {
                iconCls: 'attribute',
                type: 'field',
                entity: 'Product',
                field: 'Gross weight',
                longDescription: 'Product : Gross weight'
            },
            color: '#F46036'
        }

        wrapper.vm.selectedQuery = JSON.parse(JSON.stringify(mockedQuery))

        expect(wrapper.vm.selectedQuery.fields.length).toBe(2)

        wrapper.vm.onDropComplete(mockedField)

        expect(wrapper.vm.selectedQuery.fields.length).toBe(3)
        expect(wrapper.vm.selectedQuery.fields[wrapper.vm.selectedQuery.fields.length - 1]).toStrictEqual({
            alias: 'Gross weight',
            color: '#F46036',
            dataType: 'java.lang.Double',
            distinct: false,
            entity: 'Product',
            field: 'Gross weight',
            fieldType: 'attribute',
            format: '#,###',
            funct: 'NONE',
            group: true,
            iconCls: 'attribute',
            id: 'it.eng.knowage.inventory.Product:gross_weight',
            inUse: true,
            include: true,
            leaf: true,
            longDescription: 'Product : Gross weight',
            order: 'NONE',
            type: 'datamartField',
            visible: true
        })
    })

    it('adds all the entity columns to the list when clicking on an entity in the list', async () => {
        const wrapper = factory()
        const mockedFieldWithChildren = {
            id: 'it.eng.knowage.inventory.Warehouse_class::Warehouse_class',
            text: 'Warehouse class',
            iconCls: 'dimension',
            qtip: 'Warehouse class',
            attributes: {
                iconCls: 'dimension',
                type: 'entity',
                londDescription: 'Warehouse class',
                linkedToWords: false
            },
            children: [
                {
                    id: 'it.eng.knowage.inventory.Warehouse_class:warehouse_class_id',
                    text: 'Warehouse class id',
                    iconCls: 'attribute',
                    dataType: 'java.lang.Integer',
                    aggtype: 'SUM',
                    format: '#,###',
                    leaf: true,
                    qtip: 'Warehouse class id',
                    attributes: {
                        iconCls: 'attribute',
                        type: 'field',
                        entity: 'Warehouse class',
                        field: 'Warehouse class id',
                        longDescription: 'Warehouse class : Warehouse class id'
                    },
                    color: '#009688'
                },
                {
                    id: 'it.eng.knowage.inventory.Warehouse_class:description',
                    text: 'Description',
                    iconCls: 'attribute',
                    dataType: 'java.lang.String',
                    aggtype: 'SUM',
                    format: '#,###',
                    leaf: true,
                    qtip: 'Description',
                    attributes: {
                        iconCls: 'attribute',
                        type: 'field',
                        entity: 'Warehouse class',
                        field: 'Description',
                        longDescription: 'Warehouse class : Description'
                    },
                    color: '#009688'
                }
            ],
            relation: [],
            color: '#009688',
            expanded: true
        }

        wrapper.vm.selectedQuery = JSON.parse(JSON.stringify(mockedQuery))

        expect(wrapper.vm.selectedQuery.fields.length).toBe(2)

        wrapper.vm.onDropComplete(mockedFieldWithChildren)

        expect(wrapper.vm.selectedQuery.fields.length).toBe(4)
        expect(wrapper.vm.selectedQuery.fields[wrapper.vm.selectedQuery.fields.length - 2]).toStrictEqual({
            alias: 'Warehouse class id',
            color: '#009688',
            dataType: 'java.lang.Integer',
            distinct: false,
            entity: 'Warehouse class',
            field: 'Warehouse class id',
            fieldType: 'attribute',
            format: '#,###',
            funct: 'NONE',
            group: true,
            iconCls: 'attribute',
            id: 'it.eng.knowage.inventory.Warehouse_class:warehouse_class_id',
            inUse: true,
            include: true,
            leaf: true,
            longDescription: 'Warehouse class : Warehouse class id',
            order: 'NONE',
            type: 'datamartField',
            visible: true
        })
        expect(wrapper.vm.selectedQuery.fields[wrapper.vm.selectedQuery.fields.length - 1]).toStrictEqual({
            alias: 'Description',
            color: '#009688',
            dataType: 'java.lang.String',
            distinct: false,
            entity: 'Warehouse class',
            field: 'Description',
            fieldType: 'attribute',
            format: '#,###',
            funct: 'NONE',
            group: true,
            iconCls: 'attribute',
            id: 'it.eng.knowage.inventory.Warehouse_class:description',
            inUse: true,
            include: true,
            leaf: true,
            longDescription: 'Warehouse class : Description',
            order: 'NONE',
            type: 'datamartField',
            visible: true
        })
    })

    it('changes column position when dragging the column', async () => {
        const wrapper = factory()

        wrapper.vm.selectedQuery = JSON.parse(JSON.stringify(mockedQuery))

        expect(wrapper.vm.selectedQuery.fields[0].id).toBe('it.eng.knowage.inventory.Product:brand_name')

        wrapper.vm.smartViewReorder({ dragIndex: 0, dropIndex: 1 })

        expect(wrapper.vm.selectedQuery.fields[0].id).not.toBe('it.eng.knowage.inventory.Product:brand_name')
        expect(wrapper.vm.selectedQuery.fields[1].id).toBe('it.eng.knowage.inventory.Product:brand_name')
    })
})
