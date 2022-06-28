import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import Card from 'primevue/card'
import InputText from 'primevue/inputtext'
import PrimeVue from 'primevue/config'
import ScorecardsTargetItem from './ScorecardsTargetItem.vue'
import ScorecardsTableHint from './ScorecardsTableHint.vue'
import Toolbar from 'primevue/toolbar'

const mockedTarget = {
    id: 1,
    name: 'Test',
    criterion: {},
    options: {
        criterionPriority: []
    },
    status: 'GREY',
    kpis: [],
    groupedKpis: [],
    statusColor: '',
    updated: false
}

const $confirm = {
    require: jest.fn()
}

const $store = {
    commit: jest.fn()
}

const $router = {
    push: jest.fn()
}

const factory = () => {
    return mount(ScorecardsTargetItem, {
        props: {
            propTarget: mockedTarget,
            criterias: [],
            kpis: []
        },
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Button,
                Card,
                InputText,
                ScorecardsPerspectiveItem: true,
                ScorecardsTableHint,
                Toolbar,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $router
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Scorecards Perpsective Item', () => {
    it('should show an input field inline if MP or P rules are selected', async () => {
        const wrapper = factory()

        expect(wrapper.vm.selectedCriteria).toBe('M')
        expect(wrapper.find('[data-test="criteria-select-input"]').exists()).toBe(false)

        await wrapper.find('[data-test="select-button-MP"]').trigger('click')

        expect(wrapper.vm.selectedCriteria).toBe('MP')
        expect(wrapper.find('[data-test="criteria-select-input"]').exists()).toBe(true)

        await wrapper.find('[data-test="select-button-P"]').trigger('click')

        expect(wrapper.vm.selectedCriteria).toBe('P')
        expect(wrapper.find('[data-test="criteria-select-input"]').exists()).toBe(true)
    })
})
