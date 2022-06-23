import { mount, flushPromises } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import HierarchyManagementHierarchiesTree from './HierarchyManagementHierarchiesTree.vue'
import PrimeVue from 'primevue/config'
import Toolbar from 'primevue/toolbar'
import Tree from 'primevue/tree'

const mockedPropTree = {
    name: 'M_Bojan',
    id: 'root',
    aliasId: 'HIER_CD',
    aliasName: 'HIER_NM',
    root: true,
    children: [
        {
            name: 'Bojan',
            id: 'Bojan',
            LEAF_ID: '',
            LEAF_PARENT_CD: '',
            LEAF_PARENT_NM: '',
            aliasId: 'CDC_OCD_LEV',
            aliasName: 'CDC_NM_LEV',
            children: [
                {
                    name: 'CEO',
                    id: 'AD000',
                    LEAF_ID: '1',
                    LEAF_PARENT_CD: 'Bojan',
                    LEAF_PARENT_NM: 'Bojan',
                    aliasId: 'CDC_CD_LEAF',
                    aliasName: 'CDC_NM_LEAF',
                    leaf: true,
                    HIER_TP_M: 'MASTER',
                    HIER_NM_M: 'Bojan',
                    NODE_CD_M: 'Bojan',
                    NODE_NM_M: 'Bojan',
                    NODE_LEV_M: 1,
                    BEGIN_DT: '2009-01-01',
                    END_DT: '2999-12-31',
                    CDC_CD_LEAF: 'AD000',
                    MAX_DEPTH: 2,
                    LEVEL: 2,
                    CDC_LEAF_ID: 1,
                    CDC_NM_LEAF: 'CEO'
                }
            ],
            leaf: false,
            BEGIN_DT: null,
            END_DT: null,
            CDC_OCD_LEV: 'Bojan',
            CDC_NM_LEV: 'Bojan',
            CDC_CD_LEV: 'df0532949759cad85b22296b0ff35d2cf0a67e579860d2b21ad8b176c6e69c9c',
            ORDER_LEV: 1,
            MAX_DEPTH: 2,
            LEVEL: 1,
            FORM_LIV: 'OTH'
        },
        {
            name: 'COO',
            id: 'C001',
            LEAF_ID: '3',
            LEAF_PARENT_CD: 'Bojan',
            LEAF_PARENT_NM: 'Bojan',
            aliasId: 'CDC_CD_LEAF',
            aliasName: 'CDC_NM_LEAF',
            leaf: true,
            HIER_TP_M: 'MASTER',
            HIER_NM_M: 'Bojan',
            NODE_CD_M: 'Bojan',
            NODE_NM_M: 'Bojan',
            NODE_LEV_M: 0,
            BEGIN_DT: '2009-01-01',
            END_DT: '2999-12-31',
            CDC_CD_LEAF: 'C001',
            MAX_DEPTH: 1,
            LEVEL: 1,
            CDC_LEAF_ID: 3,
            CDC_NM_LEAF: 'COO'
        }
    ],
    leaf: false,
    YEAR: 1,
    HIER_NM: 'Bojan',
    HIER_DS: 'Bojan',
    HIER_CD: 'M_Bojan',
    HIER_TP: 'MASTER',
    MAX_DEPTH: 2,
    COMPANY_SRC: 'ALL',
    SOURCE_SYSTEM: 'Hierarchy editor'
}

const mockedNodeMetadata = {
    CONFIGS: {
        NUM_LEVELS: '15',
        ALLOW_DUPLICATE: 'false',
        TREE_LEAF_CD: 'CDC_CD_LEAF',
        NODE: 'CDC_CD_LEV*|CDC_NM_LEV*|FORM_LIV*|ORDER_LEV*',
        FILL_EMPTY: 'NO',
        ORIG_NODE: 'ORIG_HIER_CD|ORIG_HIER_NM|ORIG_NODE_CD|ORIG_NODE_NM|ORIG_HIER_LEV',
        TREE_NODE_CD: 'CDC_OCD_LEV',
        UNIQUE_NODE: 'true',
        DIMENSION_ID: 'CDC_ID',
        FORCE_NAME_AS_LEVEL: 'true',
        TREE_NODE_NM: 'CDC_NM_LEV',
        FILL_VALUE: 'OTHER',
        DIMENSION_NM: 'CDC_NM',
        TREE_LEAF_NM: 'CDC_NM_LEAF',
        DIMENSION_CD: 'CDC_CD',
        LEAF: 'CDC_CD_LEAF|CDC_NM_LEAF|LEAF_PARENT_CD|LEAF_PARENT_NM',
        TREE_LEAF_ID: 'CDC_LEAF_ID'
    },
    GENERAL_FIELDS: [
        {
            ID: 'HIER_CD',
            NAME: 'Code',
            VISIBLE: true,
            EDITABLE: true,
            PARENT: false,
            TYPE: 'String',
            SINGLE_VALUE: true,
            REQUIRED: false,
            ORDER_FIELD: false
        },
        {
            ID: 'HIER_NM',
            NAME: 'Name',
            VISIBLE: true,
            EDITABLE: true,
            PARENT: false,
            TYPE: 'String',
            SINGLE_VALUE: true,
            REQUIRED: false,
            ORDER_FIELD: false
        },
        {
            ID: 'HIER_DS',
            NAME: 'Description',
            VISIBLE: true,
            EDITABLE: true,
            PARENT: false,
            TYPE: 'String',
            SINGLE_VALUE: true,
            REQUIRED: false,
            ORDER_FIELD: false
        },
        {
            ID: 'HIER_TP',
            NAME: 'Type',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String',
            SINGLE_VALUE: true,
            REQUIRED: false,
            ORDER_FIELD: false
        },
        {
            ID: 'YEAR',
            NAME: 'Year',
            VISIBLE: true,
            EDITABLE: true,
            PARENT: false,
            TYPE: 'Number',
            SINGLE_VALUE: true,
            REQUIRED: false,
            ORDER_FIELD: false
        },
        {
            ID: 'MAX_DEPTH',
            NAME: 'Max Depth.',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'Number',
            SINGLE_VALUE: true,
            REQUIRED: false,
            ORDER_FIELD: false
        },
        {
            ID: 'COMPANY_SRC',
            NAME: 'Company',
            FIX_VALUE: 'ALL',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String',
            SINGLE_VALUE: true,
            REQUIRED: false,
            ORDER_FIELD: false
        },
        {
            ID: 'SOURCE_SYSTEM',
            NAME: 'Source System',
            FIX_VALUE: 'Hierarchy editor',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String',
            SINGLE_VALUE: true,
            REQUIRED: false,
            ORDER_FIELD: false
        }
    ],
    NODE_FIELDS: [
        {
            ID: 'CDC_CD_LEV',
            NAME: 'Unique code',
            VISIBLE: true,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String',
            SINGLE_VALUE: false,
            REQUIRED: false,
            ORDER_FIELD: false
        },
        {
            ID: 'CDC_OCD_LEV',
            NAME: 'Code',
            VISIBLE: true,
            EDITABLE: true,
            PARENT: false,
            TYPE: 'String',
            SINGLE_VALUE: false,
            REQUIRED: false,
            ORDER_FIELD: false
        },
        {
            ID: 'CDC_NM_LEV',
            NAME: 'Name',
            VISIBLE: true,
            EDITABLE: true,
            PARENT: false,
            TYPE: 'String',
            SINGLE_VALUE: false,
            REQUIRED: false,
            ORDER_FIELD: false
        },
        {
            ID: 'ORDER_LEV',
            NAME: 'Order',
            VISIBLE: true,
            EDITABLE: true,
            PARENT: false,
            TYPE: 'Number',
            SINGLE_VALUE: false,
            REQUIRED: false,
            ORDER_FIELD: false
        },
        {
            ID: 'FORM_LIV',
            NAME: 'Form Liv.',
            VISIBLE: true,
            EDITABLE: true,
            PARENT: false,
            TYPE: 'String',
            SINGLE_VALUE: false,
            REQUIRED: false,
            ORDER_FIELD: false
        }
    ],
    LEAF_FIELDS: [
        {
            ID: 'CDC_LEAF_ID',
            NAME: 'Leaf Id.',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'Number',
            SINGLE_VALUE: true,
            REQUIRED: false,
            ORDER_FIELD: false
        },
        {
            ID: 'CDC_CD_LEAF',
            NAME: 'Code',
            VISIBLE: true,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String',
            SINGLE_VALUE: true,
            REQUIRED: false,
            ORDER_FIELD: false
        },
        {
            ID: 'CDC_NM_LEAF',
            NAME: 'Name',
            VISIBLE: true,
            EDITABLE: true,
            PARENT: false,
            TYPE: 'String',
            SINGLE_VALUE: true,
            REQUIRED: false,
            ORDER_FIELD: false
        },
        {
            ID: 'LEAF_PARENT_CD',
            NAME: 'Parent Code',
            VISIBLE: true,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String',
            SINGLE_VALUE: true,
            REQUIRED: false,
            ORDER_FIELD: false
        },
        {
            ID: 'LEAF_PARENT_NM',
            NAME: 'Parent Name',
            VISIBLE: true,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String',
            SINGLE_VALUE: true,
            REQUIRED: false,
            ORDER_FIELD: false
        },
        {
            ID: 'BEGIN_DT',
            NAME: 'Begin Date',
            VISIBLE: true,
            EDITABLE: true,
            PARENT: false,
            TYPE: 'Date',
            SINGLE_VALUE: true,
            REQUIRED: false,
            ORDER_FIELD: false
        },
        {
            ID: 'END_DT',
            NAME: 'End Date',
            VISIBLE: true,
            EDITABLE: true,
            PARENT: false,
            TYPE: 'Date',
            SINGLE_VALUE: true,
            REQUIRED: false,
            ORDER_FIELD: false
        }
    ]
}

const mockedSelectedDimension = {
    DIMENSION_NM: 'CDC',
    DIMENSION_PREFIX: 'CDC',
    DIMENSION_DS: 'local_BIENG'
}

const mockedSelectedHierarchy = {
    HIER_CD: 'M_Bojan',
    HIER_NM: 'Bojan',
    HIER_TP: 'MASTER',
    HIER_DS: 'Bojan'
}

const mockedDimensionMetadata = {
    DIM_FIELDS: [
        {
            ID: 'CDC_ID',
            NAME: 'Identifier',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'Number'
        },
        {
            ID: 'CDC_CD',
            NAME: 'Code',
            VISIBLE: true,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String'
        },
        {
            ID: 'CDC_NM',
            NAME: 'Name',
            VISIBLE: true,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String'
        },
        {
            ID: 'DEN_MANAGER_CD',
            NAME: 'Manager code',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String'
        },
        {
            ID: 'DEN_MANAGER_FN',
            NAME: 'Manager Name',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String'
        },
        {
            ID: 'CDC_CD_TYPE',
            NAME: 'M/P/S',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String'
        },
        {
            ID: 'TYPE_CDC',
            NAME: 'Type',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String'
        },
        {
            ID: 'DEN_COMPANY_NM',
            NAME: 'Company Name',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String'
        },
        {
            ID: 'FL_PATR',
            NAME: 'Flag Patr.',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String'
        },
        {
            ID: 'BEGIN_DT',
            NAME: 'Begin Date',
            VISIBLE: true,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'Date'
        },
        {
            ID: 'END_DT',
            NAME: 'End Date',
            VISIBLE: true,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'Date'
        },
        {
            ID: 'CONS_SEG_ENG',
            NAME: 'Consolidation Segment',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String'
        },
        {
            ID: 'SEGMENTS_CD_ENG',
            NAME: 'Segment',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String'
        },
        {
            ID: 'BUSINESS_UNIT_CD_ENG',
            NAME: 'Business unit',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String'
        },
        {
            ID: 'BUSINESS_AREA_ENG',
            NAME: 'Business Area',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String'
        },
        {
            ID: 'DEPARTMENT_1_ENG',
            NAME: 'Department',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String'
        },
        {
            ID: 'DEPARTMENT_2_ENG',
            NAME: 'Sub Department i',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String'
        },
        {
            ID: 'DEPARTMENT_3_ENG',
            NAME: 'Sub Department i.i',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String'
        },
        {
            ID: 'DEPARTMENT_4_ENG',
            NAME: 'Sub Department i.i.i',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'String'
        },
        {
            ID: 'CDC_PARENT_CD',
            NAME: 'Cdc Parent Code',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: true,
            TYPE: 'String'
        },
        {
            ID: 'CDC_PARENT_NM',
            NAME: 'Cdc Parent Name',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: true,
            TYPE: 'String'
        },
        {
            ID: 'BEGIN_HIER_DT',
            NAME: 'Begin Hier Date',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'Date'
        },
        {
            ID: 'END_HIER_DT',
            NAME: 'End Hier Date',
            VISIBLE: false,
            EDITABLE: false,
            PARENT: false,
            TYPE: 'Date'
        }
    ],
    MATCH_LEAF_FIELDS: {
        CDC_ID: 'CDC_LEAF_ID',
        CDC_CD: 'CDC_CD_LEAF',
        CDC_NM: 'CDC_NM_LEAF'
    }
}

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() => Promise.resolve({ data: { root: [] } }))
}

const $store = {
    commit: jest.fn()
}

const factory = (treeMode) => {
    return mount(HierarchyManagementHierarchiesTree, {
        props: {
            propTree: mockedPropTree,
            nodeMetadata: mockedNodeMetadata,
            selectedDimension: mockedSelectedDimension,
            selectedHierarchy: mockedSelectedHierarchy,
            dimensionMetadata: mockedDimensionMetadata,
            treeMode: treeMode,
            propRelationsMasterTree: []
        },
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue],
            stubs: { Button, Dropdown, InputText, HierarchyManagementNodeDetailDialog: true, HierarchyManagementHierarchiesTargetDialog: true, Toolbar, Tree },
            mocks: {
                $t: (msg) => msg,
                $http,
                $store
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Hierarchy Management Hierarchies Tree', () => {
    it('On MASTER tab, it should allow to drag and drop a leaf from dimensions tajble to hierarchies tree', async () => {
        const wrapper = factory(undefined)
        const mockedTableItem = {
            CDC_ID: 3,
            CDC_CD: 'C001',
            CDC_NM: 'COO',
            DEN_MANAGER_CD: 'E2684',
            DEN_MANAGER_FN: 'COONAME COOSURNAME',
            CDC_CD_TYPE: 'PRODUCTION',
            TYPE_CDC: '',
            DEN_COMPANY_NM: 'ENGINEERING',
            FL_PATR: null,
            BEGIN_DT: '2009-01-01',
            END_DT: '2999-12-31',
            CONS_SEG_ENG: '',
            SEGMENTS_CD_ENG: '',
            BUSINESS_UNIT_CD_ENG: '',
            BUSINESS_AREA_ENG: '',
            DEPARTMENT_1_ENG: '',
            DEPARTMENT_2_ENG: '',
            DEPARTMENT_3_ENG: '',
            DEPARTMENT_4_ENG: '',
            CDC_PARENT_CD: 'EXENGISUD',
            CDC_PARENT_NM: 'ENGISUD',
            BEGIN_HIER_DT: '2009-01-01',
            END_HIER_DT: '2015-12-31'
        }

        expect(wrapper.vm.nodes[0].children[0].children[1]).toBeFalsy()

        wrapper.vm.copyNodeFromTableToTree(mockedTableItem, wrapper.vm.nodes[0].children[0])

        expect(wrapper.vm.nodes[0].children[0].children[1].id).toBe(mockedTableItem.CDC_NM)
        expect(wrapper.vm.nodes[0].children[0].children[1].label).toBe(mockedTableItem.CDC_NM)
    })

    it('On TECHNICAL tab, it should be possible to drag and drop a leaf or a subtree from hierarchies soruce to hierarchies target tree', async () => {
        const wrapper = factory(undefined)
        const mockedTreeItem = {
            children: [],
            data: {
                name: 'AD000',
                id: '1',
                LEAF_ID: '1',
                LEAF_PARENT_CD: 'E2684',
                LEAF_PARENT_NM: 'CEONAME CEOSURNAME',
                aliasId: 'CDC_CD_LEAF',
                aliasName: 'CDC_NM_LEAF',
                leaf: true,
                HIER_TP_M: 'MASTER',
                HIER_NM_M: 'Name',
                NODE_CD_M: 'E2684',
                NODE_NM_M: 'CEONAME CEOSURNAME',
                NODE_LEV_M: 2,
                BEGIN_DT: '2009-01-01',
                END_DT: '2999-12-31',
                CDC_CD_LEAF: '1',
                MAX_DEPTH: 3,
                LEVEL: 3,
                CDC_LEAF_ID: 1,
                CDC_NM_LEAF: 'AD000'
            },
            id: '1',
            key: '3469b257bec832262da865819d9db446',
            label: 'AD000',
            leaf: true,
            parentKey: 'a016c84f0c1ece9f910c396624912452'
        }

        expect(wrapper.vm.nodes[0].children[0].children[1]).toBeFalsy()

        wrapper.vm.addNodeFromSourceTree(mockedTreeItem, wrapper.vm.nodes[0].children[0])

        expect(wrapper.vm.nodes[0].children[0].children[1].id).toBe(mockedTreeItem.id)
        expect(wrapper.vm.nodes[0].children[0].children[1].label).toBe(mockedTreeItem.label)
    })
})
