import { mount } from '@vue/test-utils'
import axios from 'axios'
import Avatar from 'primevue/avatar'
import Button from 'primevue/button'
import Card from 'primevue/card'
import ConstraintsManagment from './ConstraintsManagment.vue'
import ConstraintsManagmentDetail from './ConstraintsManagmentDetail.vue'
import Dropdown from 'primevue/dropdown'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import Listbox from 'primevue/listbox'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const predefinedMocks = [
    {
        checkId: 224,
        valueTypeId: 49,
        name: 'Internet Address',
        label: 'CK-FIX-01',
        description: 'Control if parameter is an Internet Address',
        valueTypeCd: 'INTERNET ADDRESS',
        firstValue: '',
        secondValue: ''
    },
    {
        checkId: 225,
        valueTypeId: 50,
        name: 'Numeric',
        label: 'CK-FIX-02',
        description: 'Control if  a parameter is Numeric',
        valueTypeCd: 'NUMERIC',
        firstValue: '',
        secondValue: ''
    },
    {
        checkId: 226,
        valueTypeId: 51,
        name: 'Alfanumeric',
        label: 'CK-FIX-03',
        description: 'Control if  a parameter is Alfanumeric',
        valueTypeCd: 'ALFANUMERIC',
        firstValue: '',
        secondValue: ''
    }
]

const customMocks = [
    {
        checkId: 278,
        valueTypeId: 43,
        name: 'Test name',
        label: 'Test123',
        description: '',
        valueTypeCd: 'DATE',
        firstValue: 'dd/mm/yyyy',
        secondValue: null
    },
    {
        checkId: 283,
        valueTypeId: 45,
        name: 'Test 2 name',
        label: 'Test 2',
        description: 'Test 2 description',
        valueTypeCd: 'MAXLENGTH',
        firstValue: '2',
        secondValue: null
    },
    {
        checkId: 311,
        valueTypeId: 47,
        name: 'Neko ime',
        label: 'Albnale',
        description: 'asdasd',
        valueTypeCd: 'DECIMALS',
        firstValue: '5',
        secondValue: null
    }
]

jest.mock('axios')

axios.get.mockImplementation((url) => {
    switch (url) {
        case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/predefinedChecks`:
            return Promise.resolve({ data: predefinedMocks })
        case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/customChecks`:
            return Promise.resolve({ data: customMocks })
        default:
            return Promise.resolve({ data: [] })
    }
})

axios.post.mockImplementation(() => Promise.resolve())

const $confirm = {
    require: jest.fn()
}

const $store = {
    commit: jest.fn()
}

const factory = () => {
    return mount(ConstraintsManagment, {
        global: {
            plugins: [],
            stubs: {
                Avatar,
                Button,
                Card,
                ConstraintsManagmentDetail,
                Dropdown,
                InputText,
                Listbox,
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

describe('Constraints Management loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('the list shows an hint component when loaded empty', async () => {
        axios.get
            .mockReturnValueOnce(
                Promise.resolve({
                    data: []
                })
            )
            .mockReturnValueOnce(
                Promise.resolve({
                    data: []
                })
            )
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.allCheks.length).toBe(0)
        expect(wrapper.find('[data-test="check-list"]').html()).toContain('common.info.noDataFound')
    })
})

describe('Constraints Management', () => {
    it('shows a prompt when user click on a constraint delete button to delete it', async () => {
        const wrapper = factory()
        await flushPromises()

        const deleteButton = wrapper.find('[data-test="delete-button"]')
        await deleteButton.trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)
    })
    it('shows and empty detail when clicking on the add button', async () => {
        const wrapper = factory()
        const openButton = wrapper.find('[data-test="open-form-button"]')

        await openButton.trigger('click')

        expect(wrapper.vm.formVisible).toBe(true)
        expect(wrapper.find('[data-test="constraints-form"]').exists()).toBe(true)
        expect(wrapper.vm.selectedCheck).toStrictEqual(undefined)
    })
    it('shows the detail when clicking on a item', async () => {
        const wrapper = factory()
        await flushPromises()
        await wrapper.find('[data-test="list-item"]').trigger('click')

        expect(wrapper.vm.formVisible).toBe(true)
        expect(wrapper.vm.selectedCheck).toStrictEqual({
            checkId: 278,
            valueTypeId: 43,
            name: 'Test name',
            predifined: false,
            label: 'Test123',
            description: '',
            valueTypeCd: 'DATE',
            firstValue: 'dd/mm/yyyy',
            secondValue: null
        })
    })
})
