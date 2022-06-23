import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import ConstraintsManagementDetail from './ConstraintsManagementDetail.vue'
import Toolbar from 'primevue/toolbar'

const mockedPredefinedConstraint = {
    checkId: 224,
    valueTypeId: 49,
    name: 'Internet Address',
    predifined: true,
    label: 'CK-FIX-01',
    description: 'Control if parameter is an Internet Address',
    valueTypeCd: 'INTERNET ADDRESS',
    firstValue: '',
    secondValue: ''
}

const mockedDomains = [
    {
        VALUE_NM: 'Date',
        VALUE_DS: 'Date',
        VALUE_ID: 43,
        VALUE_CD: 'DATE'
    },
    {
        VALUE_NM: 'Regexp',
        VALUE_DS: 'Regular Expression',
        VALUE_ID: 44,
        VALUE_CD: 'REGEXP'
    },
    {
        VALUE_NM: 'Max Length',
        VALUE_DS: 'Max Length',
        VALUE_ID: 45,
        VALUE_CD: 'MAXLENGTH'
    },
    {
        VALUE_NM: 'Range',
        VALUE_DS: 'Range',
        VALUE_ID: 46,
        VALUE_CD: 'RANGE'
    },
    {
        VALUE_NM: 'Decimal',
        VALUE_DS: 'Decimal',
        VALUE_ID: 47,
        VALUE_CD: 'DECIMALS'
    },
    {
        VALUE_NM: 'Min Length',
        VALUE_DS: 'Min Length',
        VALUE_ID: 48,
        VALUE_CD: 'MINLENGTH'
    }
]

const factory = () => {
    return mount(ConstraintsManagementDetail, {
        global: {
            props: {
                selectedConstraint: {},
                domains: mockedDomains
            },
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

describe('Constraints Management Detail', () => {
    it('save button is disabled if one of the mandatory input is invalid', () => {
        const wrapper = factory()
        expect(wrapper.vm.constraint).toStrictEqual({})
        expect(wrapper.vm.buttonDisabled).toBe(true)
    })
    it('changes the specific input to number if ranges, decimal, min lenght or max length check type is selected', async () => {
        const wrapper = factory()
        await wrapper.setProps({ selectedConstraint: { valueTypeCd: 'MAXLENGTH' } })
        expect(wrapper.vm.numberType).toBe(true)
        await wrapper.setProps({ selectedConstraint: { valueTypeCd: 'RANGE' } })
        expect(wrapper.vm.numberType).toBe(true)
        await wrapper.setProps({ selectedConstraint: { valueTypeCd: 'DECIMALS' } })
        expect(wrapper.vm.numberType).toBe(true)
        await wrapper.setProps({ selectedConstraint: { valueTypeCd: 'MINLENGTH' } })
        expect(wrapper.vm.numberType).toBe(true)
    })
    it('disabled the input fields if the constraint is predefined', async () => {
        const wrapper = factory()
        await wrapper.setProps({ selectedConstraint: mockedPredefinedConstraint })
        expect(wrapper.vm.inputDisabled).toBe(true)
    })
})
