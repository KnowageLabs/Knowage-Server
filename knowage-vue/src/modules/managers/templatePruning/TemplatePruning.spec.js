import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
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
    { id: 1, parentId: null, name: 'Functionalities', biObjects: [] },
    {
        id: 2,
        parentId: 1,
        name: 'Test',
        biObjects: [
            { id: 20, name: 'TestCache' },
            { id: 21, name: 'TestNews' },
            { id: 22, name: 'TestRoles' }
        ]
    },
    {
        id: 2,
        parentId: 1,
        name: 'Other',
        biObjects: [{ id: 25, name: 'Some other document' }]
    },
    {
        id: 3,
        parentId: 1,
        name: 'Options',
        biObjects: [
            { id: 30, name: 'Settings' },
            { id: 31, name: 'Dummy' },
            { id: 32, name: 'Roles' }
        ]
    },
    {
        id: 4,
        parentId: null,
        name: 'Root Test Folder',
        biObjects: [
            { id: 40, name: 'Testing' },
            { id: 41, name: 'Tests' }
        ]
    }
]

const mockedDocuments = [
    { id: 20, name: 'TestCache' },
    { id: 25, name: 'Some other document' },
    { id: 30, name: 'Settings' },
    { id: 31, name: 'Dummy' },
    { id: 32, name: 'Roles' }
]

const $confirm = {
    require: vi.fn()
}

vi.mock('axios')

const $http = {
    post: vi.fn().mockImplementation(() => Promise.resolve())
}

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

                $confirm,
                $http
            }
        }
    })
}

describe('Template Pruning', () => {
    it('when page is loaded the datepicker opens with the current date', () => {
        const wrapper = factory()
        const dateInput = wrapper.find('[data-test="date-input"]')

        expect(wrapper.vm.selectedDate).toBeTruthy()
        expect(dateInput.exists()).toBe(true)
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
        $http.get = axios.get.mockImplementation((url) => {
            switch (url) {
                case import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/folders?includeDocs=true':
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
    it('if one or more templates are available the folder tree appears with the search bar', async () => {
        $http.get = axios.get.mockImplementation((url) => {
            switch (url) {
                case import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/folders?includeDocs=true':
                    return Promise.resolve({ data: mockedFolders })
                default:
                    return Promise.resolve({ data: mockedDocuments })
            }
        })

        const wrapper = factory()

        expect(wrapper.find('[data-test="document-tree"]').exists()).toBe(false)

        await wrapper.find('[data-test="filter-button"]').trigger('click')
        await flushPromises()

        expect(wrapper.vm.nodes.length).toBeGreaterThan(0)
        expect(wrapper.vm.documentsAvailable).toBe(true)
        expect(wrapper.find('[data-test="document-tree"]').exists()).toBe(true)
        expect(wrapper.find('.p-tree-filter-container').exists()).toBe(true)
    })
    it('if one or more templates are selected from the tree the delete button is enabled', async () => {
        const wrapper = factory()

        await wrapper.find('[data-test="filter-button"]').trigger('click')
        await flushPromises()

        await wrapper.find('[role="checkbox"]').trigger('click')

        expect(wrapper.vm.selectedDocuments).toEqual(expect.objectContaining({ 20: { checked: true, partialChecked: false }, 25: { checked: true, partialChecked: false }, 30: { checked: true, partialChecked: false }, 32: { checked: true, partialChecked: false } }))
        expect(wrapper.vm.deleteDisabled).toBe(false)
        expect(wrapper.find('[data-test="delete-button"]').element.disabled).toBe(false)
    })
    it('if one or more templates are selected from the tree and the delete button is clicked a delete funciton starts', async () => {
        const wrapper = factory()
        const currentDate = wrapper.vm.formatDate(new Date())

        await wrapper.find('[data-test="filter-button"]').trigger('click')
        await flushPromises()

        await wrapper.find('[role="checkbox"]').trigger('click')

        expect(wrapper.vm.selectedDocuments).toEqual(
            expect.objectContaining({ 20: { checked: true, partialChecked: false }, 25: { checked: true, partialChecked: false }, 30: { checked: true, partialChecked: false }, 31: { checked: true, partialChecked: false }, 32: { checked: true, partialChecked: false } })
        )

        await wrapper.find('[data-test="delete-button"]').trigger('click')

        wrapper.vm.deleteDocuments()

        expect($http.post).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + 'template/deleteTemplate', [
            { id: 20, data: currentDate },
            { id: 25, data: currentDate },
            { id: 30, data: currentDate },
            { id: 31, data: currentDate },
            { id: 32, data: currentDate }
        ])
    })
})
