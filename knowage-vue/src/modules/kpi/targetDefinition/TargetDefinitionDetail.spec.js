import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import TargetDefinitionDetail from './TargetDefinitionDetail.vue'
import Button from 'primevue/button'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import Listbox from 'primevue/listbox'
import PrimeVue from 'primevue/config'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const $confirm = {
    require: vi.fn()
}

const $route = { path: '/target-definition' }

const $router = {
    push: vi.fn(),
    replace: vi.fn()
}

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() =>
        Promise.resolve({
            data: []
        })
    ),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}

const factory = () => {
    return mount(TargetDefinitionDetail, {
        global: {
            plugins: [PrimeVue, createTestingPinia()],
            stubs: {
                Button,
                Card,
                InputText,
                Listbox,
                ProgressBar,
                Toolbar,
                DataTable,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,

                $confirm,
                $route,
                $router,
                $http
            }
        }
    })
}
describe('Target Definition Detail', () => {
    it('disables the save button if no name is provided', () => {
        const formWrapper = factory()
        expect(formWrapper.vm.target.name).toStrictEqual(undefined)
        expect(formWrapper.vm.buttonDisabled).toBe(true)
    })
    it('disables the save button if no kpi is selected', () => {
        const formWrapper = factory()
        expect(formWrapper.vm.kpi).toStrictEqual([])
        expect(formWrapper.vm.buttonDisabled).toBe(true)
    })
    it('shows an hint if no kpi is selected', () => {
        const wrapper = factory()
        expect(wrapper.vm.kpi).toStrictEqual([])
        expect(wrapper.find('[data-test="selected-kpi-table"]').html()).toContain('noElementSelected')
    })
})
