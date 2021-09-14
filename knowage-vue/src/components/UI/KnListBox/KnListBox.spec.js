import { mount } from '@vue/test-utils'
import Avatar from 'primevue/avatar'
import Badge from 'primevue/badge'
import Listbox from 'primevue/listbox'
import Menu from 'primevue/menu'
import KnListBox from './KnListBox.vue'
import PrimeVue from 'primevue/config'

const mockedOptions = [
    { id: 1, label: 'label1', name: 'Name1', description: 'Desc1' },
    { id: 2, label: 'label2', name: 'Name2', description: 'Desc2' },
    { id: 3, label: 'label3', name: 'Name3', description: 'Desc3' }
]

const mockedsettings = {
    defaultSortField: 'name',
    interaction: {
        parameterLabel: 'id',
        parameterValue: 'id',
        path: 'navigation-detail',
        type: 'router'
    },
    buttons: [
        {
            emits: 'delete',
            icon: 'fas fa-trash-alt',
            label: 'common.delete'
        }
    ]
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
            settings: mockedsettings
        },
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue],
            stubs: {
                Avatar,
                Badge,
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

describe('Cross-navigation Management loading', () => {
    it('the list shows an hint component when loaded empty', async () => {
        const wrapper = factory()

        await wrapper.setProps({ options: [] })

        expect(wrapper.vm.options.length).toBe(0)
        expect(wrapper.find('[data-test="list"]').html()).toContain('No available options')
    })
})
describe('Cross-navigation Management', () => {
    it('shows a prompt when user click on a list item delete button to delete it', async () => {
        const wrapper = factory()
        console.log(wrapper.html())
        const deleteButton = wrapper.find('[data-test="delete-button-0"]')
        await deleteButton.trigger('click')
    })
    it('shows the detail when clicking on a item', () => {})
})
