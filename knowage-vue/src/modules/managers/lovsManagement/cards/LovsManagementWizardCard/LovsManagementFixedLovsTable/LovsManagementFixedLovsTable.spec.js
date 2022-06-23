import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import LovsManagementFixedLovsTable from './LovsManagementFixedLovsTable.vue'
import InputText from 'primevue/inputtext'

const mockedListForFixedLovs = [
    { VALUE: 'Dummy value', DESCRIPTION: 'Dummy description' },
    { VALUE: 'Test', DESCRIPTION: 'abc' },
    { VALUE: 'Fixed lov', DESCRIPTION: 'Fixed description' },
    { VALUE: '80-90', DESCRIPTION: '80-90' }
]

const $confirm = {
    require: vi.fn()
}

const $store = {
    commit: jest.fn()
}

const factory = () => {
    return mount(LovsManagementFixedLovsTable, {
        props: {
            listForFixLov: [...mockedListForFixedLovs]
        },
        global: {
            stubs: {
                Button,
                Column,
                DataTable,
                InputText
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

describe('Lovs Management fixed list card', () => {
    it("creates a new line if the 'new' button is clicked", async () => {
        const wrapper = factory()

        expect(wrapper.vm.values.length).toBe(4)

        await wrapper.find('[data-test="new-button"]').trigger('click')

        expect(wrapper.vm.values.length).toBe(5)
        expect(wrapper.vm.values[wrapper.vm.values.length - 1]).toStrictEqual({ VALUE: '', DESCRIPTION: '' })
    })
    it('deletes a value clicking on the delete button', async () => {
        const wrapper = factory()

        await wrapper.find('[data-test="delete-button-1"]').trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)

        await wrapper.vm.deleteValue(0)

        expect(wrapper.vm.values.length).toBe(3)
        expect(wrapper.vm.values[0]).not.toStrictEqual(mockedListForFixedLovs[0])
    })
    it('filters the table when entering a search text', async () => {
        const wrapper = factory()

        const fixedLovsValuesList = wrapper.find('[data-test="values-list"]')
        const searchInput = fixedLovsValuesList.find(['[data-test="filter-input"]'])

        expect(fixedLovsValuesList.html()).toContain('Dummy value')
        expect(fixedLovsValuesList.html()).toContain('Test')
        expect(fixedLovsValuesList.html()).toContain('Fixed lov')

        // Value
        await searchInput.setValue('Fixed lov')
        await fixedLovsValuesList.trigger('filter')
        expect(fixedLovsValuesList.html()).not.toContain('Dummy value')
        expect(fixedLovsValuesList.html()).not.toContain('Test')
        expect(fixedLovsValuesList.html()).toContain('Fixed lov')

        // Description
        await searchInput.setValue('Dummy description')
        await fixedLovsValuesList.trigger('filter')
        expect(fixedLovsValuesList.html()).toContain('Dummy value')
        expect(fixedLovsValuesList.html()).not.toContain('Test')
        expect(fixedLovsValuesList.html()).not.toContain('Fixed lov')
    })
    it('allows editing a line value or description clicking it', async () => {
        const wrapper = factory()

        expect(wrapper.find('[data-test="value-input"]').exists()).toBe(false)
        await wrapper.find('[data-test="value-body"]').trigger('click')
        expect(wrapper.find('[data-test="value-input"]').exists()).toBe(true)

        expect(wrapper.find('[data-test="description-input"]').exists()).toBe(false)
        await wrapper.find('[data-test="description-body"]').trigger('click')
        expect(wrapper.find('[data-test="description-input"]').exists()).toBe(true)
    })
    it('allows reordering the values in the list', () => {
        const wrapper = factory()

        expect(wrapper.find('[data-test="values-list"]').html()).toContain('p-datatable-reorderablerow-handle')
    })
})
