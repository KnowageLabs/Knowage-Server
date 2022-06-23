import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Message from 'primevue/message'
import SchedulerDocumentsTable from './SchedulerDocumentsTable.vue'
import Toolbar from 'primevue/toolbar'

const mockedDocuments = [
    {
        name: 'DEMO_Report',
        nameTitle: 'Store Sales Analysis',
        condensedParameters: ' age_range = 30-40; 40-50; 60-70; 50-60  |  par_brand_name =  |  prod_category =  |  product_hierarchy =  | ',
        parameters: [
            {
                name: 'age_range',
                value: '30-40; 40-50; 60-70; 50-60 ',
                type: 'fixed',
                iterative: false
            },
            {
                name: 'par_brand_name',
                value: '',
                type: 'fixed',
                iterative: false
            },
            {
                name: 'prod_category',
                value: '',
                type: 'fixed',
                iterative: false
            },
            {
                name: 'product_hierarchy',
                value: '',
                type: 'fixed',
                iterative: false
            }
        ]
    },
    {
        name: 'Filled parameters only',
        nameTitle: 'Filled parameters only',
        condensedParameters: 'age_range = 30-40; 40-50; 60-70; 50-60 ',
        parameters: [
            {
                name: 'age_range',
                value: '30-40; 40-50; 60-70; 50-60 ',
                type: 'fixed',
                iterative: false
            }
        ]
    }
]

vi.mock('axios')

axios.get.mockImplementation(() => Promise.resolve({ data: [] }))

const factory = () => {
    return mount(SchedulerDocumentsTable, {
        props: {
            jobDocuments: mockedDocuments
        },
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Button,
                Column,
                DataTable,
                Message,
                SchedulerDocumentsSelectionDialog: true,
                SchedulerDocumentParameterDialog: true,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Scheduler Documents table', () => {
    it('should show a warning icon if documents provide parameters and they are not set', () => {
        const wrapper = factory()

        expect(wrapper.vm.documents).toStrictEqual(mockedDocuments)
        expect(wrapper.vm.documents[0].parameters[0].value).toBeTruthy()
        expect(wrapper.vm.documents[0].parameters[1].value).toBeFalsy()
        expect(wrapper.vm.documents[0].parameters[2].value).toBeFalsy()
        expect(wrapper.vm.documents[0].parameters[3].value).toBeFalsy()
        expect(wrapper.vm.documents[1].parameters[0].value).toBeTruthy()

        expect(wrapper.find('[data-test="warning-icon-DEMO_Report"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="warning-icon-Filled parameters only"]').exists()).toBe(false)
    })
})
