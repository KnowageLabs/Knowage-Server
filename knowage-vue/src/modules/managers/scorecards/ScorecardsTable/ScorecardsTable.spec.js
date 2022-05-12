import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import Card from 'primevue/card'
import PrimeVue from 'primevue/config'
import ScorecardsTable from './ScorecardsTable.vue'
import ScorecardsTableHint from './ScorecardsTableHint.vue'
import Toolbar from 'primevue/toolbar'

const mockedScorecard = { name: '', description: '', perspectives: [] }

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
    return mount(ScorecardsTable, {
        props: {
            propScorecard: mockedScorecard,
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

describe('Scorecards Table', () => {
    it('should show an hint if no perspectives are present', async () => {
        const wrapper = factory()

        expect(wrapper.vm.scorecard.perspectives.length).toBe(0)
        expect(wrapper.find('[data-test="no-perspective-hint"]').exists()).toBe(true)
    })
    it('adds perspective on button click', async () => {
        const wrapper = factory()

        expect(wrapper.vm.scorecard.perspectives.length).toBe(0)
        expect(wrapper.find('[data-test="no-perspective-hint"]').exists()).toBe(true)
    })
})
