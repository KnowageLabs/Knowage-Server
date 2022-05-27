import { mount } from '@vue/test-utils'
import Calendar from 'primevue/calendar'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import InputText from 'primevue/inputtext'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import NewsDetailCard from './NewsDetailCard.vue'
import PrimeVue from 'primevue/config'
import Textarea from 'primevue/textarea'
import Toolbar from 'primevue/toolbar'

const mockedNews = {
    id: '1',
    title: 'First news',
    description: 'Description',
    type: {
        id: 1,
        value: 'News'
    },
    html: '<p>Test</p>',
    expirationDate: new Date('2019-10-02 00:00:00.0'),
    active: true
}

const factory = () => {
    return mount(NewsDetailCard, {
        global: {
            plugins: [PrimeVue],
            stubs: {
                Calendar,
                Card,
                Editor: true,
                Dropdown,
                InputSwitch,
                KnValidationMessages,
                InputText,
                Textarea,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Role Detail Tab', () => {
    it('shows filled input fields when role is passed', async () => {
        const wrapper = factory()
        await wrapper.setProps({ selectedNews: mockedNews })
        const activeInput = wrapper.find('[data-test="active-input"]')
        const titleInput = wrapper.find('[data-test="title-input"]')
        const descriptionInput = wrapper.find('[data-test="description-input"]')

        expect(wrapper.vm.news).toStrictEqual(mockedNews)

        expect(activeInput.html()).toContain('aria-checked="true"')
        expect(titleInput.wrapperElement._value).toBe('First news')
        expect(descriptionInput.wrapperElement._value).toBe('Description')
    })

    it('emits correct value on input field change', async () => {
        const wrapper = factory()

        const titleInput = wrapper.find('[data-test="title-input"]')
        const descriptionInput = wrapper.find('[data-test="description-input"]')

        await titleInput.setValue('test title')
        expect(wrapper.emitted().fieldChanged[0][0].value).toBe('test title')

        await descriptionInput.setValue('test description')
        expect(wrapper.emitted().fieldChanged[2][0].value).toBe('test description')
    })

    it('emits correct value on field and active change', async () => {
        const wrapper = factory()
        await wrapper.setProps({ selectedNews: mockedNews })

        wrapper.vm.onFieldChange('test', 'test field')
        expect(wrapper.emitted().fieldChanged[0][0].value).toBe('test field')

        wrapper.vm.news.active = false
        wrapper.vm.onActiveChange()
        expect(wrapper.emitted().fieldChanged[1][0].value).toBe(false)
    })
})
