import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import Button from 'primevue/button'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import FunctionsCatalogInputVariable from './FunctionsCatalogInputVariable.vue'

const mockedVariable = {
    name: 'scale_factor',
    type: 'STRING',
    value: 'default value'
}

const factory = () => {
    return mount(FunctionsCatalogInputVariable, {
        props: {
            variable: mockedVariable,
            readonly: false
        },
        global: {
            directives: {
                tooltip() {}
            },
            stubs: {
                Button,
                Calendar,
                Dropdown,
                InputText
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Functions Catalog Input Tab', () => {
    it('should allow to specify a default value for each input variable', async () => {
        const wrapper = factory()

        expect(wrapper.vm.variable).toStrictEqual(mockedVariable)
        expect(wrapper.find('[data-test="variable-name-input"]').wrapperElement._value).toBe('scale_factor')
        expect(wrapper.find('[data-test="variable-defult-value-input"]').wrapperElement._value).toBe('default value')

        await wrapper.find('[data-test="variable-defult-value-input"]').setValue('edited default value')

        expect(wrapper.find('[data-test="variable-defult-value-input"]').wrapperElement._value).toBe('edited default value')
        expect(wrapper.vm.variable.value).toBe('edited default value')
    })
})
