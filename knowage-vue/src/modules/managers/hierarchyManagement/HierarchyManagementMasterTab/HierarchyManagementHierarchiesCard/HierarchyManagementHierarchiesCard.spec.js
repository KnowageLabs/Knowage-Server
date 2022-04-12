import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import HierarchyManagementHierarchiesCard from './HierarchyManagementHierarchiesCard.vue'
import ProgressSpinner from 'primevue/progressspinner'
import PrimeVue from 'primevue/config'
import Toolbar from 'primevue/toolbar'

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() => Promise.resolve({ data: [] }))
}

const factory = () => {
    return mount(HierarchyManagementHierarchiesCard, {
        global: {
            plugins: [PrimeVue],
            stubs: { Button, Calendar, Checkbox, Dropdown, ProgressSpinner, Toolbar },
            mocks: {
                $t: (msg) => msg,
                $http
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Hierarchy Management Hierarchies ard', () => {
    it('Should show a warning popup when saving an empty hierarchy', async () => {
        const wrapper = factory()

        console.log(wrapper)
    })
})
