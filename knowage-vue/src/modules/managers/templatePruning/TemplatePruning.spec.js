import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Calendar from 'primevue/calendar'
import Card from 'primevue/card'
import flushPromises from 'flush-promises'
import moment from 'moment'
import PrimeVue from 'primevue/config'
import ProgressBar from 'primevue/progressbar'
import TemplatePruning from './TemplatePruning.vue'
import Toolbar from 'primevue/toolbar'
import Tree from 'primevue/tree'

const mockedFolders = [
    { id: 1, name: 'Functionalities', biObjects: [] },
    {
        id: 2,
        name: 'Test',
        biObjects: [
            {
                id: 20,
                name: 'TestCache'
            },
            {
                id: 21,
                name: 'TestNews'
            },
            {
                id: 22,
                name: 'TestRoles'
            }
        ]
    },
    {
        id: 2,
        name: 'Other',
        biObjects: [
            {
                id: 25,
                name: 'Some other document'
            }
        ]
    },
    {
        id: 3,
        name: 'Options',
        biObjects: [
            {
                id: 30,
                name: 'Settings'
            },
            {
                id: 312,
                name: 'Dummy'
            },
            {
                id: 32,
                name: 'Roles'
            }
        ]
    }
]

const mockedDocuments = [
    {
        id: 20,
        name: 'TestCache'
    },
    {
        id: 25,
        name: 'Some other document'
    },
    {
        id: 30,
        name: 'Settings'
    },
    {
        id: 312,
        name: 'Dummy'
    },
    {
        id: 32,
        name: 'Roles'
    }
]

const $confirm = {
    require: jest.fn()
}

const $store = {
    commit: jest.fn()
}

jest.mock('axios')

axios.get.mockImplementation((url) => {
    switch (url) {
        case process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/folders?includeDocs=true':
            return Promise.resolve({ data: mockedFolders })
        default:
            return Promise.resolve({ data: mockedDocuments })
    }
})

const factory = () => {
    return mount(TemplatePruning, {
        global: {
            plugins: [PrimeVue],
            stubs: {
                Button,
                Calendar,
                Card,
                ProgressBar,
                Toolbar,
                Tree
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm
            }
        }
    })
}

describe('Template Pruning', () => {
    it('when page is loaded the datepicker opens with the current date', () => {
        const wrapper = factory()
        const dateInput = wrapper.find('[data-test="date-input"]')

        expect(dateInput.wrapperElement._value).toBe(moment().format('MM/DD/YYYY'))
    })
    it('datepicker max date is the current date', () => {
        const wrapper = factory()

        expect(moment(wrapper.vm.maxDate).format('MM/DD/YYYY')).toBe(moment().format('MM/DD/YYYY'))
    })
    it('if a date is not selected the filter button is disabled', async () => {
        const wrapper = factory()
        const filterButton = wrapper.find('[data-test="filter-button"]')

        await wrapper.setData({ selectedDate: null })

        expect(wrapper.vm.filterDisabled).toBe(true)
        expect(filterButton.element.disabled).toBe(true)
    })
    it('if no template is available the card below shows a no available template message', async () => {
        axios.get.mockImplementation((url) => {
            switch (url) {
                case process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/folders?includeDocs=true':
                    return Promise.resolve({ data: [] })
                default:
                    return Promise.resolve({ data: [] })
            }
        })
        const wrapper = factory()

        await wrapper.find('[data-test="filter-button"]').trigger('click')
        await flushPromises()

        expect(wrapper.vm.folderStructure.length).toBe(0)
        expect(wrapper.vm.documents.length).toBe(0)
        expect(wrapper.find('[data-test="document-selection-card"]').html()).toContain('managers.templatePruning.noDocuments')
    })
    it('if no template is available the card below does not show a delete button', async () => {
        const wrapper = factory()

        await wrapper.find('[data-test="filter-button"]').trigger('click')
        await flushPromises()

        expect(wrapper.vm.folderStructure.length).toBe(0)
        expect(wrapper.vm.documents.length).toBe(0)
        expect(wrapper.find('[data-test="delete-button"]').exists()).toBe(false)
    })
    xit('if one or more templates are available the folder tree appears', () => {})
    xit('if one or more templates are available the search bar for the tree appears', () => {})
    xit('if one or more templates are selected from the tree the delete button is enabled', () => {})
    xit('if one or more templates are selected from the tree and the delete button is clicked a delete funciton starts', () => {})
})
