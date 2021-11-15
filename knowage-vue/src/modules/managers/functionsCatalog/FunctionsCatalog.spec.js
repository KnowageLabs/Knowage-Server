import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Chip from 'primevue/chip'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import FunctionsCatalog from './FunctionsCatalog.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedFunctions = {
    functions: [
        {
            id: '4e230432-2332-4efa-97cc-66a42a29523b',
            name: 'echo_function',
            owner: 'demo_admin',
            label: 'echo_function',
            type: 'Machine Learning'
        },
        {
            id: '5196e42e-ae71-44e6-a1ac-854fba2144cf',
            name: 'Logarithm',
            owner: 'demo_admin',
            label: 'Logarithm',
            type: 'Utilities'
        },
        {
            id: '599b009b-707e-4033-ad1c-02f6bec275e7',
            name: 'Toy function',
            owner: 'demo_admin',
            label: 'toy_dataset_function',
            type: 'Machine Learning'
        }
    ]
}

jest.mock('axios')

axios.get.mockImplementation(() => Promise.resolve({ data: mockedFunctions }))
axios.delete.mockImplementation(() => Promise.resolve())

const $confirm = {
    require: jest.fn()
}

const $store = {
    commit: jest.fn()
}

const factory = () => {
    return mount(FunctionsCatalog, {
        global: {
            directives: {
                tooltip() {}
            },
            stubs: {
                Button,
                Chip,
                KnFabButton,
                FunctionsCatalogDatatable: true,
                FunctionsCatalogDetail: true,
                FunctionCatalogFilterCards: true,
                FunctionCatalogPreviewDialog: true,
                ProgressBar,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

// Can't mount because of CodeMirror import
describe('Functons Catalog loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
})
