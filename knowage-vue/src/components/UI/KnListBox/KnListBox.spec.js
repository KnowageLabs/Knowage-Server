import { mount } from '@vue/test-utils'
import Avatar from 'primevue/avatar'
import Badge from 'primevue/badge'
import Button from 'primevue/button'
import KnListButtonRenderer from './KnListButtonRenderer.vue'
import Listbox from 'primevue/listbox'
import Menu from 'primevue/menu'
import KnListBox from './KnListBox.vue'
import PrimeVue from 'primevue/config'

const mockedOptions = [
    {
        id: 544,
        name: '/albnale/admin',
        description: '/albnale/admin',
        roleTypeCD: 'ADMIN',
        code: null,
        roleTypeID: 32,
        organization: 'DEMO',
        isPublic: false
    },
    {
        id: 450,
        name: '/demo/admin',
        description: '/demo/admin',
        roleTypeCD: 'ADMIN',
        code: null,
        roleTypeID: 32,
        organization: 'DEMO',
        isPublic: false
    },
    {
        id: 455,
        name: '/demo/user',
        description: '/demo/user',
        roleTypeCD: 'USER',
        code: '/demo/user',
        roleTypeID: 31,
        organization: 'DEMO',
        isPublic: false
    }
]

const mockedSettings = {
    buttons: [
        {
            emits: 'delete',
            icon: 'fas fa-trash-alt',
            label: 'common.delete'
        }
    ],
    defaultSortField: 'name',
    filterFields: ['name'],
    interaction: {
        type: 'event'
    },
    sortFields: ['name'],
    textField: 'roleTypeCD',
    titleField: 'name'
}

const $confirm = {
    require: jest.fn()
}

const $store = {
    commit: jest.fn()
}

const factory = () => {
    return mount(KnListBox, {
        props: {
            options: mockedOptions,
            settings: mockedSettings
        },
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue],
            stubs: {
                Avatar,
                Badge,
                Button,
                KnListButtonRenderer,
                Listbox,
                Menu
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm
            }
        }
    })
}

describe('KnListBox loading', () => {
    it('the list shows an hint component when loaded empty', async () => {
        const wrapper = factory()

        await wrapper.setProps({ options: [] })

        expect(wrapper.vm.options.length).toBe(0)
        expect(wrapper.find('[data-test="list"]').html()).toContain('No available options')
    })
})
describe('KnListBox', () => {
    it('shows list of items', async () => {
        const wrapper = factory()

        await wrapper.find('[data-test="list-item"]').trigger('click')

        expect(wrapper.html()).toContain('/albnale/admin')
        expect(wrapper.html()).toContain('/demo/admin')
        expect(wrapper.html()).toContain('/demo/user')
    })
    it('emits event with the clicked item on click', async () => {
        const wrapper = factory()

        await wrapper.find('[data-test="list-item"]').trigger('click')

        expect(wrapper.emitted()['click'][0][0].item).toStrictEqual(mockedOptions[0])
    })

    it('filters the list', async () => {
        const wrapper = factory()
        const searchInput = wrapper.find('.p-inputtext')

        expect(wrapper.html()).toContain('/albnale/admin')
        expect(wrapper.html()).toContain('/demo/admin')
        expect(wrapper.html()).toContain('/demo/user')

        await searchInput.setValue('/albnale/admin')

        expect(wrapper.html()).toContain('/albnale/admin')
        expect(wrapper.html()).not.toContain('/demo/admin')
        expect(wrapper.html()).not.toContain('/demo/user')
    })

    it('emitts event with item to delete', async () => {
        const wrapper = factory()
        const searchInput = wrapper.find('.p-inputtext')

        await searchInput.setValue('/demo/admin')
        await wrapper.find('[data-test="delete-button-0"]').trigger('click')

        expect(wrapper.emitted().delete[0][0].item).toStrictEqual(mockedOptions[1])
    })
})
