import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Column from 'primevue/column'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import RolesCard from './DriversManagementRolesCard.vue'
import InputText from 'primevue/inputtext'

const mockedRoles = [
    {
        id: 1,
        name: 'user1',
        description: 'user1',
        roleTypeCD: 'ADMIN'
    },
    {
        id: 2,
        name: 'user2',
        description: 'user2',
        roleTypeCD: 'ADMIN'
    },
    {
        id: 3,
        name: 'user3',
        description: 'user3',
        roleTypeCD: 'ADMIN'
    }
]

const $confirm = {
    require: vi.fn()
}

const factory = () => {
    return mount(RolesCard, {
        props: {
            roles: [...mockedRoles]
        },
        global: {
            plugins: [],
            stubs: {
                Column,
                Card,
                DataTable,
                InputText
            },
            mocks: {
                $t: (msg) => msg,

                $confirm
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Drivers Management Use modes', () => {
    it('filters constraints when entering a roles text search', async () => {
        const wrapper = factory()

        const valuesList = wrapper.find('[data-test="values-list"]')
        const searchInput = valuesList.find(['[data-test="filter-input"]'])

        expect(valuesList.html()).toContain('user1')
        expect(valuesList.html()).toContain('user2')
        expect(valuesList.html()).toContain('user3')

        await searchInput.setValue('user1')
        await valuesList.trigger('filter')

        expect(valuesList.html()).toContain('user1')
        expect(valuesList.html()).not.toContain('user2')
        expect(valuesList.html()).not.toContain('user3')

        await searchInput.setValue('user2')
        await valuesList.trigger('filter')

        expect(valuesList.html()).not.toContain('user1')
        expect(valuesList.html()).toContain('user2')
        expect(valuesList.html()).not.toContain('user3')

        await searchInput.setValue('user3')
        await valuesList.trigger('filter')

        expect(valuesList.html()).not.toContain('user1')
        expect(valuesList.html()).not.toContain('user2')
        expect(valuesList.html()).toContain('user3')
    })
})
