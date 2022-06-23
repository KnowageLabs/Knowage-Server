import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Card from 'primevue/card'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import MondrianSchemasDetailTab from './MondrianSchemasDetailTab.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import Button from 'primevue/button'

const mockedSchema = {
    id: 1,
    name: 'schema',
    description: 'schema',
    type: 'MONDRIAN_SCHEMA'
}

vi.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() =>
        Promise.resolve({
            data: []
        })
    )
}

const factory = () => {
    return mount(MondrianSchemasDetailTab, {
        global: {
            stubs: {
                Card,
                ProgressBar,
                Toolbar,
                Button,
                KnValidationMessages,
                InputText
            },
            mocks: {
                $t: (msg) => msg,
                $http
            }
        }
    })
}

describe('Role Detail Tab', () => {
    it('shows filled input fields when role is passed', async () => {
        const wrapper = factory()
        await wrapper.setProps({ selectedSchema: mockedSchema })
        const nameInput = wrapper.find('[data-test="name-input"]')
        const descriptionInput = wrapper.find('[data-test="description-input"]')

        expect(wrapper.vm.schema).toStrictEqual(mockedSchema)

        expect(nameInput.wrapperElement._value).toBe('schema')
        expect(descriptionInput.wrapperElement._value).toBe('schema')
    })

    it('emits correct value on input change', async () => {
        const wrapper = factory()
        await flushPromises()

        const nameInput = wrapper.find('[data-test="name-input"]')
        const descriptionInput = wrapper.find('[data-test="description-input"]')

        await nameInput.setValue('test name')
        expect(wrapper.emitted().fieldChanged[0][0].value).toBe('test name')

        await descriptionInput.setValue('test description')
        expect(wrapper.emitted().fieldChanged[2][0].value).toBe('test description')
    })
})
