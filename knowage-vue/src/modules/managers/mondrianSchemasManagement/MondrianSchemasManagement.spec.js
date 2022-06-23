import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import MondrianSchemasManagement from './MondrianSchemasManagement.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import Card from 'primevue/card'

const mockedSchemas = [
    {
        id: 1,
        name: 'Main Schema',
        description: 'big bonk',
        type: 'MONDRIAN_SCHEMA'
    },
    {
        id: 2,
        name: 'Sub Schematic',
        description: 'small bokn',
        type: 'MONDRIAN_SCHEMA'
    },
    {
        id: 3,
        name: 'Meta Option',
        description: 'conditional subject',
        type: 'MONDRIAN_SCHEMA'
    }
]

vi.mock('axios')

const $route = {
    fullPath: '/mondrian-schemas-management'
}

const $http = {
    get: vi.fn().mockImplementation(() =>
        Promise.resolve({
            data: mockedSchemas
        })
    ),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}

const $confirm = {
    require: vi.fn()
}

const $store = {
    commit: jest.fn()
}

const $router = {
    push: jest.fn()
}

const factory = () => {
    return mount(MondrianSchemasManagement, {
        global: {
            stubs: {
                Button,
                InputText,
                ProgressBar,
                Toolbar,
                Card,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $router,
                $http,
                $route
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Mondrian Schema Management loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('shows "no data" label when loaded empty', async () => {
        $http.get.mockReturnValueOnce(Promise.resolve({ data: [] }))
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.schemas.length).toBe(0)
        expect(wrapper.find('[data-test="schemas-list"]').html()).toContain('common.info.noDataFound')
    })
})

describe('Mondrian Schema Management', () => {
    it('deletes schema clicking on delete icon', async () => {
        const wrapper = factory()
        await flushPromises()

        expect(wrapper.vm.schemas.length).toBe(3)

        const deleteButton = wrapper.find('[data-test="delete-button"]')
        await deleteButton.trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)

        await wrapper.vm.deleteSchema(1)
        expect($http.delete).toHaveBeenCalledTimes(1)
        expect($http.delete).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/mondrianSchemasResource/' + 1)
    })
    it('opens empty detail form when the ' + ' button is clicked', async () => {
        const wrapper = factory()
        const openButton = wrapper.find('[data-test="open-form-button"]')

        await openButton.trigger('click')

        expect($router.push).toHaveBeenCalledWith('/mondrian-schemas-management/new-schema')
    })
    it('open filled detail when a row is clicked', async () => {
        const wrapper = factory()
        await flushPromises()
        await wrapper.find('[data-test="list-item"]').trigger('click')

        expect($router.push).toHaveBeenCalledWith('/mondrian-schemas-management/' + 1)
    })
})

describe('Mondrian Schema Management Search', () => {
    it('filters the list if a Name (or description) is provided', async () => {
        const wrapper = factory()
        await flushPromises()
        const schemasList = wrapper.find('[data-test="schemas-list"]')
        const searchInput = schemasList.find('input')

        expect(schemasList.html()).toContain('Main Schema')
        expect(schemasList.html()).toContain('Sub Schematic')
        expect(schemasList.html()).toContain('Meta Option')

        // Name
        await searchInput.setValue('Sub Schematic')
        await schemasList.trigger('filter')
        expect(schemasList.html()).not.toContain('Main Schema')
        expect(schemasList.html()).toContain('Sub Schematic')
        expect(schemasList.html()).not.toContain('Meta Option')

        // Description
        await searchInput.setValue('small bokn')
        await schemasList.trigger('filter')
        expect(schemasList.html()).not.toContain('Main Schema')
        expect(schemasList.html()).toContain('Sub Schematic')
        expect(schemasList.html()).not.toContain('Meta Option')
    })
    it('returns no data if the label is not present', async () => {
        const wrapper = factory()
        await flushPromises()
        const schemasList = wrapper.find('[data-test="schemas-list"]')
        const searchInput = schemasList.find('input')

        expect(schemasList.html()).toContain('Main Schema')
        expect(schemasList.html()).toContain('Sub Schematic')
        expect(schemasList.html()).toContain('Meta Option')

        await searchInput.setValue('not present value')
        await schemasList.trigger('filter')

        expect(schemasList.html()).not.toContain('Main Schema')
        expect(schemasList.html()).not.toContain('Sub Schematic')
        expect(schemasList.html()).not.toContain('Meta Option')
    })
})
