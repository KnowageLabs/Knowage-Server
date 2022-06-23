import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import FunctionalitiesManagement from './FunctionalitiesManagement.vue'
import FabButton from '@/components/UI/KnFabButton.vue'
import flushPromises from 'flush-promises'
import KnHint from '@/components/UI/KnHint.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import Tree from 'primevue/tree'

const mockedFunctionalities = [
    { id: 1, parentId: null, name: 'Functionalities', prog: 1 },
    {
        id: 2,
        parentId: 1,
        name: 'Test',
        prog: 1
    },
    {
        id: 3,
        parentId: 1,
        name: 'Other',
        prog: 2
    },
    {
        id: 4,
        parentId: 1,
        name: 'Options',
        prog: 3
    },
    {
        id: 5,
        parentId: null,
        name: 'Root Test Folder',
        prog: 1
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() =>
        Promise.resolve({
            data: mockedFunctionalities
        })
    ),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}

afterEach(() => {
    vi.clearAllMocks()
})

const $confirm = {
    require: vi.fn()
}

const factory = () => {
    return mount(FunctionalitiesManagement, {
        global: {
            directives: {
                tooltip() {}
            },
            stubs: { Button, Card, FabButton, FunctionalitiesManagementDetail: true, KnHint, ProgressBar, Toolbar, Tree },
            mocks: {
                $t: (msg) => msg,

                $confirm,
                $http
            }
        }
    })
}

describe('Functionalities loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
})

describe('Functionalities', () => {
    it('when loaded a tree with just the root is shown if no child are present', async () => {
        $http.get.mockReturnValueOnce(
            Promise.resolve({
                data: [{ id: 1, parentId: null, name: 'Functionalities' }]
            })
        )
        const wrapper = factory()
        const tree = wrapper.find('[data-test="functionality-tree"]')

        await flushPromises()

        expect(wrapper.vm.functionalities.length).toBe(1)
        expect(wrapper.vm.nodes.length).toBe(1)
        expect(tree.html()).toContain('Functionalities')
        expect(tree.html()).not.toContain('Test')
        expect(tree.html()).not.toContain('Other')
    })
    it('when loaded a tree with hyerarchical nodes will be visible expanded', async () => {
        const wrapper = factory()
        const tree = wrapper.find('[data-test="functionality-tree"]')

        await flushPromises()

        expect(wrapper.vm.functionalities.length).toBe(5)
        expect(wrapper.vm.nodes.length).toBe(2)
        expect(wrapper.vm.expandedKeys).toStrictEqual({ 1: true })
        expect(tree.html()).toContain('Functionalities')
        expect(tree.html()).toContain('Test')
        expect(tree.html()).toContain('Other')
    })
    it('shows an hint if no item is selected from the tree', () => {
        const wrapper = factory()

        expect(wrapper.vm.showHint).toBe(true)
        expect(wrapper.find('[data-test="functionality-hint"]').exists()).toBe(true)
    })
    it('ask a confirm if delete button is clicked', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.functionalities.length).toBe(5)

        await wrapper.find('[data-test="delete-button-3"]').trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)

        await wrapper.vm.deleteFunctionality(3)
        expect($http.delete).toHaveBeenCalledTimes(1)
        expect($http.delete).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/functionalities/' + 3)
        expect(store.setInfo).toHaveBeenCalledTimes(1)
    })
    it('moves the item up in the tree if the move up button is clicked', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.functionalities.length).toBe(5)

        await wrapper.find('[data-test="move-up-button-3"]').trigger('click')

        expect($http.get).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/functionalities/moveUp/' + 3)
    })
    it('moves the item down in the tree if the move down button is clicked', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.functionalities.length).toBe(5)

        await wrapper.find('[data-test="move-down-button-3"]').trigger('click')

        expect($http.get).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/functionalities/moveDown/' + 3)
    })
    it('shows an empty detail if add new button is clicked', async () => {
        const wrapper = factory()

        await flushPromises()
        await wrapper.find('[data-test="tree-item-3"]').trigger('click')
        await wrapper.find('[data-test="new-button"]').trigger('click')

        expect(wrapper.vm.selectedFunctionality).toBe(null)
    })
    it('shows a detail if one item is selected from the tree', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.functionalities.length).toBe(5)
        await wrapper.find('[data-test="tree-item-3"]').trigger('click')

        expect(wrapper.vm.selectedFunctionality).toStrictEqual({ id: 3, name: 'Other', parentId: 1, prog: 2 })
    })
})
