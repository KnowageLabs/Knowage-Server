import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import MetadataManagementDetail from './MetadataManagementDetail.vue'
import Toolbar from 'primevue/toolbar'

const mockedMetadata = {
    label: 'metadata1',
    name: 'name1',
    description: 'description1',
    dataType: 'SHORT_TEXT'
}

jest.mock('axios', () => ({
    post: jest.fn(() => Promise.resolve())
}))

const $store = {
    commit: jest.fn()
}

const factory = () => {
    return mount(MetadataManagementDetail, {
        global: {
            stubs: {
                Button,
                Card,
                Dropdown,
                InputText,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $store
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Metadata Management Detail', () => {
    it('save button is disabled if a mandatory input is empty', () => {
        const wrapper = factory()
        expect(wrapper.vm.metadata).toStrictEqual({})
        expect(wrapper.vm.buttonDisabled).toBe(true)
    })
    it('shows success info if data is saved', async () => {
        const wrapper = factory()
        wrapper.vm.metadata = mockedMetadata
        wrapper.vm.v$.$invalid = false
        wrapper.vm.handleSubmit()
        await flushPromises()
        expect(axios.post).toHaveBeenCalledTimes(1)
        expect(axios.post).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/objMetadata', mockedMetadata)
        expect($store.commit).toHaveBeenCalledTimes(1)

        const mockedMetadataUpdate = { ...mockedMetadata, id: 1 }
        wrapper.vm.domain = mockedMetadataUpdate
        wrapper.vm.handleSubmit()
        await flushPromises()
        expect(axios.post).toHaveBeenCalledTimes(2)
        expect(axios.post).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/objMetadata', mockedMetadata)
        expect($store.commit).toHaveBeenCalledTimes(2)
    })
    it('shows three different metadata types', () => {
        const wrapper = factory()
        expect(wrapper.vm.metadataTypes).toMatchObject([
            {
                name: 'SHORT_TEXT',
                value: 'SHORT_TEXT'
            },
            {
                name: 'LONG_TEXT',
                value: 'LONG_TEXT'
            },
            {
                name: 'FILE',
                value: 'FILE'
            }
        ])
    })

    it('shows filled inputs card when metadata is passed', async () => {
        const wrapper = factory()
        await wrapper.setProps({ model: mockedMetadata })
        const labelInput = wrapper.find('[data-test="label-input"]')
        const nameInput = wrapper.find('[data-test="name-input"]')
        const descriptionInput = wrapper.find('[data-test="description-input"]')

        expect(wrapper.vm.metadata).toStrictEqual(mockedMetadata)
        expect(labelInput.wrapperElement._value).toBe('metadata1')
        expect(nameInput.wrapperElement._value).toBe('name1')
        expect(descriptionInput.wrapperElement._value).toBe('description1')
    })
})
