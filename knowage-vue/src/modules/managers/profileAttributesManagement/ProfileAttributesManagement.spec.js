import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import flushPromises from 'flush-promises'
import ProfileAttributesManagement from './ProfileAttributesManagement.vue'
import InputText from 'primevue/inputtext'
import ProgressBar from 'primevue/progressbar'
import Card from 'primevue/card'
import Toolbar from 'primevue/toolbar'

const mockedAttributes = [
    {
        attributeId: 3,
        attributeName: 'address',
        attributeDescription: 'address',
        allowUser: null,
        multivalue: null,
        syntax: null,
        lovId: null,
        value: null
    },
    {
        attributeId: 4,
        attributeName: 'birth_date',
        attributeDescription: 'birth date',
        allowUser: null,
        multivalue: null,
        syntax: null,
        lovId: null,
        value: null
    },
    {
        attributeId: 5,
        attributeName: 'email',
        attributeDescription: 'email',
        allowUser: null,
        multivalue: null,
        syntax: null,
        lovId: null,
        value: null
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() =>
        Promise.resolve({
            data: mockedAttributes
        })
    ),
    post: vi.fn().mockImplementation(() => Promise.resolve()),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}

const $confirm = {
    require: vi.fn()
}

const factory = () => {
    return mount(ProfileAttributesManagement, {
        attachToDocument: true,
        global: {
            plugins: [],
            stubs: { Button, InputText, ProgressBar, Toolbar, Card },
            mocks: {
                $t: (msg) => msg,

                $confirm,
                $http
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('ProfileAttributes Management loading', () => {
    it('show progress bar when loading', async () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })

    it('shows "no data" label when loaded empty', async () => {
        $http.get.mockReturnValueOnce(
            Promise.resolve({
                data: []
            })
        )
        const wrapper = factory()
        await flushPromises()
        expect(wrapper.vm.attributes.length).toBe(0)
        expect(wrapper.find('[data-test="profile-attributes-listbox"]').html()).toContain('common.info.noDataFound')
    })
})

describe('Profile Attributes Management', () => {
    it("opens empty form when the '+' button is clicked", async () => {
        const wrapper = factory()
        const openButton = wrapper.find('[data-test="open-form-button"]')
        await openButton.trigger('click')
        expect(wrapper.vm.hideForm).toBe(false)
    })

    it('shows form when a row is clicked', async () => {
        const wrapper = factory()
        await flushPromises()
        const dataTable = wrapper.find('[data-test="profile-attributes-listbox"]')
        await dataTable.find('ul li').trigger('click')

        expect(wrapper.vm.hideForm).toBe(false)
        expect(wrapper.vm.attribute).toStrictEqual({
            attributeId: 3,
            attributeName: 'address',
            attributeDescription: 'address',
            allowUser: null,
            multivalue: null,
            syntax: null,
            lovId: null,
            value: null
        })
    })
})

describe('Profille Attributes Management Search', () => {
    it('filters the list if a label or name is provided', async () => {
        const wrapper = factory()
        await flushPromises()
        const attributesListBox = wrapper.find('[data-test="profile-attributes-listbox"]')
        const searchInput = attributesListBox.find('input')

        expect(attributesListBox.html()).toContain('address')
        expect(attributesListBox.html()).toContain('birth_date')
        expect(attributesListBox.html()).toContain('email')

        // attributeName
        await searchInput.setValue('email')
        await attributesListBox.trigger('filter')
        expect(attributesListBox.html()).toContain('email')
        expect(attributesListBox.html()).not.toContain('address')
        expect(attributesListBox.html()).not.toContain('birth_date')

        // attributeDescription
        await searchInput.setValue('birth_date')
        await attributesListBox.trigger('filter')
        expect(attributesListBox.html()).not.toContain('address')
        expect(attributesListBox.html()).not.toContain('email')
        expect(attributesListBox.html()).toContain('birth_date')
    })
    it('returns no data if the label is not present', async () => {
        const wrapper = factory()
        await flushPromises()
        const attributesListBox = wrapper.find('[data-test="profile-attributes-listbox"]')
        const searchInput = attributesListBox.find('input')

        expect(attributesListBox.html()).toContain('address')
        expect(attributesListBox.html()).toContain('email')
        expect(attributesListBox.html()).toContain('birth_date')

        await searchInput.setValue('not present value')
        await attributesListBox.trigger('filter')

        expect(attributesListBox.html()).not.toContain('birth_date')
        expect(attributesListBox.html()).not.toContain('email')
        expect(attributesListBox.html()).not.toContain('address')
    })
})
