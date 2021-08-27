import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import DriversManagement from './DriversManagement.vue'
import FabButton from '@/components/UI/KnFabButton.vue'
import flushPromises from 'flush-promises'
import KnHint from '@/components/UI/KnHint.vue'
import Listbox from 'primevue/listbox'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedDrivers = [
    {
        id: 1,
        label: 'Manual Input String',
        name: 'Manual Input String',
        description: 'Manual Input String',
        type: 'STRING',
        typeId: 30,
        length: 0,
        mask: '',
        modality: '',
        modalityValue: null,
        modalityValueForDefault: null,
        modalityValueForMax: null,
        defaultFormula: '',
        valueSelection: null,
        selectedLayer: null,
        selectedLayerProp: null,
        checks: null,
        temporal: false,
        functional: true
    },
    {
        id: 2,
        label: 'DEMO_ProductFamily',
        name: 'DEMO_ProductFamily',
        description: 'DEMO_ProductFamily',
        type: 'STRING',
        typeId: 30,
        length: 0,
        mask: '',
        modality: '',
        modalityValue: null,
        modalityValueForDefault: null,
        modalityValueForMax: null,
        defaultFormula: '',
        valueSelection: null,
        selectedLayer: null,
        selectedLayerProp: null,
        checks: null,
        temporal: false,
        functional: true
    },
    {
        id: 3,
        label: 'DEMO_BRAND_NAME',
        name: 'DEMO_BRAND_NAME',
        description: 'DEMO_BRAND_NAME',
        type: 'STRING',
        typeId: 30,
        length: 0,
        mask: '',
        modality: '',
        modalityValue: null,
        modalityValueForDefault: null,
        modalityValueForMax: null,
        defaultFormula: '',
        valueSelection: null,
        selectedLayer: null,
        selectedLayerProp: null,
        checks: null,
        temporal: false,
        functional: true
    }
]

jest.mock('axios')

axios.get.mockImplementation(() => Promise.resolve({ data: mockedDrivers }))
axios.delete.mockImplementation(() => Promise.resolve())

const $confirm = {
    require: jest.fn()
}

const $store = {
    commit: jest.fn()
}

const $router = {
    push: jest.fn(),
    replace: jest.fn()
}

const factory = () => {
    return mount(DriversManagement, {
        global: {
            stubs: {
                Button,
                Card,
                FabButton,
                Listbox,
                KnHint,
                ProgressBar,
                Toolbar,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $router
                // $route
            }
        }
    })
}

describe('Drivers Management', () => {
    it('shows a prompt when user click on a driver delete button to delete it', async () => {
        const wrapper = factory()
        await flushPromises()
        await wrapper.find('[data-test="delete-button"]').trigger('click')
        expect($confirm.require).toHaveBeenCalledTimes(1)
    })
    it('shows and empty detail when clicking on the add button', () => {})
    it('shows the detail when clicking on a item', () => {})
})
