import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import FunctionalitiesManagement from './FunctionalitiesManagement.vue'
import FabButton from '@/components/UI/KnFabButton.vue'
import flushPromises from 'flush-promises'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import Tree from 'primevue/tree'

const mockedFunctionalities = [
    { id: 1, parentId: null, name: 'Functionalities' },
    {
        id: 2,
        parentId: 1,
        name: 'Test'
    },
    {
        id: 3,
        parentId: 1,
        name: 'Other'
    },
    {
        id: 4,
        parentId: 2,
        name: 'Options'
    },
    {
        id: 5,
        parentId: null,
        name: 'Root Test Folder'
    }
]

jest.mock('axios')

axios.get.mockImplementation(() => Promise.resolve({ data: mockedFunctionalities }))
axios.delete.mockImplementation(() => Promise.resolve())

afterEach(() => {
    jest.clearAllMocks()
})

const $confirm = {
    require: jest.fn()
}

const $store = {
    commit: jest.fn()
}

const factory = () => {
    return mount(FunctionalitiesManagement, {
        global: {
            directives: {
                tooltip() {}
            },
            stubs: { Button, FabButton, ProgressBar, Toolbar, Tree },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm
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
        axios.get.mockReturnValueOnce(
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
        expect(wrapper.vm.expandedKeys).toStrictEqual({ '1': true, '2': true })
        expect(tree.html()).toContain('Functionalities')
        expect(tree.html()).toContain('Test')
        expect(tree.html()).toContain('Other')
    })
})
