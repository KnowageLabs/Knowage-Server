import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { createRouter, createWebHistory } from 'vue-router'
import TargetDefinitionHint from './TargetDefinitionHint.vue'
import Button from 'primevue/button'
import Card from 'primevue/card'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import Listbox from 'primevue/listbox'
import ProgressBar from 'primevue/progressbar'
import TargetDefinition from './TargetDefinition.vue'
import Toolbar from 'primevue/toolbar'
import KnHint from '@/components/UI/KnHint.vue'
import mainStore from '../../../App.store'

const mockedTarget = [
    {
        id: 88,
        name: 'Test_target1',
        startValidity: 1625781600000,
        endValidity: 1625868000000,
        author: 'demo_admin',
        values: [],
        category: {
            valueId: 421,
            valueCd: 'SALES',
            valueName: 'SALES',
            valueDescription: 'SALES',
            domainCode: 'KPI_TARGET_CATEGORY',
            domainName: 'KPI_TARGET_CATEGORY',
            translatedValueName: 'SALES',
            translatedValueDescription: 'SALES'
        }
    },
    {
        id: 89,
        name: 'Test_target2',
        startValidity: 1625781600000,
        endValidity: 1625868000000,
        author: 'demo_admin',
        values: [],
        category: {
            valueId: 421,
            valueCd: 'SALES',
            valueName: 'SALES',
            valueDescription: 'SALES',
            domainCode: 'KPI_TARGET_CATEGORY',
            domainName: 'KPI_TARGET_CATEGORY',
            translatedValueName: 'SALES',
            translatedValueDescription: 'SALES'
        }
    },
    {
        id: 90,
        name: 'Test_target3',
        startValidity: 1625781600000,
        endValidity: 1625868000000,
        author: 'demo_admin',
        values: [],
        category: {
            valueId: 421,
            valueCd: 'SALES',
            valueName: 'SALES',
            valueDescription: 'SALES',
            domainCode: 'KPI_TARGET_CATEGORY',
            domainName: 'KPI_TARGET_CATEGORY',
            translatedValueName: 'SALES',
            translatedValueDescription: 'SALES'
        }
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() =>
        Promise.resolve({
            data: mockedTarget
        })
    ),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}

const $confirm = {
    require: vi.fn()
}

const $route = { path: '/target-definition' }

const $router = {
    push: vi.fn(),
    replace: vi.fn()
}
const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: TargetDefinitionHint
        },
        {
            path: '/target-definition',
            component: TargetDefinitionHint
        },
        {
            path: '/new-target-definition',
            component: null
        },
        {
            path: '/target-definition/edit',
            props: (route) => ({ id: route.query.id, clone: route.query.clone }),
            component: null
        }
    ]
})
const factory = () => {
    return mount(TargetDefinition, {
        global: {
            plugins: [router, createTestingPinia()],
            stubs: {
                Button,
                Card,
                InputText,
                Listbox,
                ProgressBar,
                Toolbar,
                KnHint
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

afterEach(() => {
    vi.clearAllMocks()
})

describe('Target Definition loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('the list shows "no data" label when loaded empty', async () => {
        $http.get.mockReturnValueOnce(
            Promise.resolve({
                data: []
            })
        )
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.targetList.length).toBe(0)
        expect(wrapper.find('[data-test="target-list"]').html()).toContain('common.info.noDataFound')
    })
})
describe('Target Definition List', () => {
    it('shows an hint when no item is selected', async () => {
        router.push('/target-definition')

        await router.isReady()

        await flushPromises()

        const wrapper = factory()

        expect(wrapper.html()).toContain('kpi.targetDefinition.hint')
    })
    it('deletes target when clicking on delete icon', async () => {
        const wrapper = factory()
        const store = mainStore()

        await flushPromises()

        expect(wrapper.vm.targetList.length).toBe(3)

        await wrapper.find('[data-test="delete-button"]').trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)

        await wrapper.vm.deleteTarget(88)
        expect($http.delete).toHaveBeenCalledTimes(1)
        expect($http.delete).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/kpiee/' + 88 + '/deleteTarget')
        expect(store.setInfo).toHaveBeenCalledTimes(1)
        expect($router.replace).toHaveBeenCalledWith('/target-definition')
    })
    it("opens empty detail form when the '+' button is clicked", async () => {
        const wrapper = factory()

        await wrapper.find('[data-test="open-form-button"]').trigger('click')

        expect($router.push).toHaveBeenCalledWith('/target-definition/new-target-definition')
    })
    it('open filled detail when an item is clicked', async () => {
        const wrapper = factory()

        await flushPromises()
        await wrapper.find('[data-test="list-item"]').trigger('click')
        expect($router.push).toHaveBeenCalledWith(`/target-definition/edit?id=88&clone=false`)
    })
})
