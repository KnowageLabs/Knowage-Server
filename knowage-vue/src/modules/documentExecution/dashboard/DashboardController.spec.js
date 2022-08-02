import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { v4 as uuidv4 } from 'uuid'
import { nextTick } from 'vue'
import { emitter } from './DashboardHelpers'
import Button from 'primevue/button'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import PrimeVue from 'primevue/config'
import DashboardController from './DashboardController.vue'
import Toolbar from 'primevue/toolbar'
import VueGridLayout from 'vue-grid-layout'

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/?asPagedList=true&seeTechnical=true`:
                return Promise.resolve({ data: [] })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

const $confirm = {
    require: vi.fn()
}

const $router = {
    push: vi.fn()
}

const factory = () => {
    return mount(DashboardController, {
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            provide: { dHash: uuidv4() },
            plugins: [createTestingPinia(), VueGridLayout],
            stubs: {
                Button,
                Column,
                DatasetEditor: true,
                WidgetPickerDialog: true,
                WidgetEditor: true,
                InputText,
                Toolbar,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
                $http,
                $confirm,
                $router
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('DashboardController', () => {
    it('should open up when clicking on new widget inside widget picker dialog', async () => {
        const wrapper = factory()
        wrapper.vm.openNewWidgetEditor({ name: 'Table', type: 'table' })

        console.log('WRAPPER: ', wrapper.html())

        await nextTick()

        expect(wrapper.vm.widgetEditorVisible).toBe(true)
        expect(wrapper.vm.selectedWidget.type).toBe('table')
        expect(wrapper.find('[data-test="widget-editor"]').exists()).toBe(true)
    })

    it('should open up when clicking on edit widget button', async () => {
        const wrapper = factory()
        emitter.emit('openWidgetEditor', { name: 'Table', type: 'table' })

        console.log('WRAPPER: ', wrapper.html())

        await nextTick()

        expect(wrapper.vm.widgetEditorVisible).toBe(true)
        expect(wrapper.vm.selectedWidget).toStrictEqual({ name: 'Table', type: 'table' })
        expect(wrapper.find('[data-test="widget-editor"]').exists()).toBe(true)
    })
})
