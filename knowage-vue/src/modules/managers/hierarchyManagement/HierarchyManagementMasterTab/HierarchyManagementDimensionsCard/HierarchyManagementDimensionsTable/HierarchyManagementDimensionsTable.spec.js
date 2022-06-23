import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import HierarchyManagementDimensionsTable from './HierarchyManagementDimensionsTable.vue'
import PrimeVue from 'primevue/config'
import Toolbar from 'primevue/toolbar'

const mockedDimensionData = {
    root: [
        {
            CDC_ID: 1,
            CDC_CD: 'AD000',
            CDC_NM: 'CEO',
            DEN_MANAGER_CD: 'E2684',
            DEN_MANAGER_FN: 'CEONAME CEOSURNAME',
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
        },
        {
            CDC_ID: 2,
            CDC_CD: 'AD001',
            CDC_NM: 'CEO 2',
            DEN_MANAGER_CD: 'E2684',
            DEN_MANAGER_FN: 'CEONAME CEOSURNAME 2',
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
        },
        {
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
    ],
    columns: [
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
    columns_search: ['CDC_CD', 'CDC_NM', 'BEGIN_DT', 'END_DT']
}

const factory = () => {
    return mount(HierarchyManagementDimensionsTable, {
        props: {
            dimensionData: mockedDimensionData
        },
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue],
            stubs: { Button, Column, DataTable, InputText, Toolbar },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Hierarchy Management Dimensions Table', () => {
    it('loads dimensions table with data', async () => {
        const wrapper = factory()

        expect(wrapper.html()).toContain('CEO')
        expect(wrapper.html()).toContain('CEO 2')
        expect(wrapper.html()).toContain('COO')
    })
})
