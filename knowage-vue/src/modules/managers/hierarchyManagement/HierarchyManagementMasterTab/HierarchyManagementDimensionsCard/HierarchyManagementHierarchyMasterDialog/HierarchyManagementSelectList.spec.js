import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import Listbox from 'primevue/listbox'
import HierarchyManagementHierarchyMasterSelectList from './HierarchyManagementHierarchyMasterSelectList.vue'
import ProgressSpinner from 'primevue/progressspinner'
import PrimeVue from 'primevue/config'
import Toolbar from 'primevue/toolbar'

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

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() => Promise.resolve({ data: [] }))
}

const factory = () => {
    return mount(HierarchyManagementHierarchyMasterSelectList, {
        props: {
            dimensionMetadata: mockedDimensionMetadata
        },
        global: {
            plugins: [PrimeVue],
            stubs: { Button, Calendar, Dropdown, Listbox, ProgressSpinner, Toolbar },
            mocks: {
                $t: (msg) => msg,
                $http
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Hierarchy Management Hierarchy Master Select List', () => {
    it('Should prevent the user to select more than two items from the left list', async () => {
        const wrapper = factory()

        await wrapper.find("[data-test='list-item-End Hier Date']").trigger('click')
        await wrapper.find("[data-test='list-item-Business Area']").trigger('click')

        expect(wrapper.vm.selectedSourceFields.length).toBe(2)
        await wrapper.find("[data-test='list-item-Consolidation Segment']").trigger('click')
        expect(wrapper.vm.selectedSourceFields.length).toBe(2)
        expect(wrapper.vm.errorMessageVisible).toBe(true)
        expect(wrapper.html()).toContain('managers.hierarchyManagement.createHierarchyMasterErrorMessage')
    })

    it('Should allow the user to enable recursive mode only for the last level', async () => {
        const wrapper = factory()

        await wrapper.find("[data-test='list-item-End Hier Date']").trigger('click')
        await wrapper.find("[data-test='list-item-Business Area']").trigger('click')
        expect(wrapper.vm.selectedSourceFields.length).toBe(2)
        await wrapper.find("[data-test='move-right-button']").trigger('click')

        await wrapper.find("[data-test='list-item-Code']").trigger('click')
        await wrapper.find("[data-test='list-item-Name']").trigger('click')
        expect(wrapper.vm.selectedSourceFields.length).toBe(2)
        await wrapper.find("[data-test='move-right-button']").trigger('click')

        expect(wrapper.find("[data-test='selected-destinations-list']").html()).toContain('End Hier Date')
        expect(wrapper.find("[data-test='selected-destinations-list']").html()).toContain('Business Area')

        expect(wrapper.find("[data-test='recursive-button-End Hier Date']").exists()).toBe(false)
        expect(wrapper.find("[data-test='recursive-button-Code']").exists()).toBe(true)

        expect(wrapper.vm.recursive).toBe(null)
        await wrapper.find("[data-test='recursive-button-Code']").trigger('click')
        expect(wrapper.vm.recursive).toStrictEqual({
            code: {
                EDITABLE: false,
                ID: 'CDC_CD',
                NAME: 'Code',
                PARENT: false,
                TYPE: 'String',
                VISIBLE: true,
                level: 2
            },
            hasCopy: false,
            isLast: false,
            name: {
                EDITABLE: false,
                ID: 'CDC_NM',
                NAME: 'Name',
                PARENT: false,
                TYPE: 'String',
                VISIBLE: true,
                level: 2
            }
        })
    })

    it('Should prevent the user to enable more than one recursive', async () => {
        const wrapper = factory()

        await wrapper.find("[data-test='list-item-End Hier Date']").trigger('click')
        await wrapper.find("[data-test='list-item-Business Area']").trigger('click')
        await wrapper.find("[data-test='move-right-button']").trigger('click')

        await wrapper.find("[data-test='list-item-Code']").trigger('click')
        await wrapper.find("[data-test='list-item-Name']").trigger('click')
        await wrapper.find("[data-test='move-right-button']").trigger('click')

        expect(wrapper.find("[data-test='recursive-button-End Hier Date']").exists()).toBe(false)
        expect(wrapper.find("[data-test='recursive-button-Code']").exists()).toBe(true)

        expect(wrapper.vm.recursive).toBe(null)
        await wrapper.find("[data-test='recursive-button-Code']").trigger('click')
        expect(wrapper.vm.recursive).toStrictEqual({
            code: {
                EDITABLE: false,
                ID: 'CDC_CD',
                NAME: 'Code',
                PARENT: false,
                TYPE: 'String',
                VISIBLE: true,
                level: 2
            },
            hasCopy: false,
            isLast: false,
            name: {
                EDITABLE: false,
                ID: 'CDC_NM',
                NAME: 'Name',
                PARENT: false,
                TYPE: 'String',
                VISIBLE: true,
                level: 2
            }
        })

        expect(wrapper.find("[data-test='recursive-button-End Hier Date']").exists()).toBe(true)
        await wrapper.find("[data-test='recursive-button-End Hier Date']").trigger('click')

        expect(wrapper.vm.recursive).toStrictEqual({
            code: {
                EDITABLE: false,
                ID: 'END_HIER_DT',
                NAME: 'End Hier Date',
                PARENT: false,
                TYPE: 'Date',
                VISIBLE: false,
                level: 1
            },
            hasCopy: false,
            isLast: false,
            name: {
                EDITABLE: false,
                ID: 'BUSINESS_AREA_ENG',
                NAME: 'Business Area',
                PARENT: false,
                TYPE: 'String',
                VISIBLE: false,
                level: 1
            }
        })
    })
})
