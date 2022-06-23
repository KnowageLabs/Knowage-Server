import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import Column from 'primevue/column'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import MondrianSchemasManagementTabView from './MondrianSchemasManagementTabView.vue'
import ProgressBar from 'primevue/progressbar'
import TabPanel from 'primevue/tabpanel'
import TabView from 'primevue/tabview'
import Toolbar from 'primevue/toolbar'

vi.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() =>
        Promise.resolve({
            data: []
        })
    )
}

const $confirm = {
    require: vi.fn()
}

const $store = {
    commit: jest.fn()
}

const $router = {
    push: jest.fn()
}

const factory = () => {
    return mount(MondrianSchemasManagementTabView, {
        global: {
            stubs: {
                Button,
                Column,
                Card,
                DataTable,
                KnValidationMessages,
                InputText,
                ProgressBar,
                TabPanel,
                TabView,
                Toolbar,
                MondrianSchemasDetailTab: true,
                MondrianSchemasWorkflowTab: true
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $router,
                $http
            }
        }
    })
}

describe('Mondrian Schema Management Tab View', () => {
    it('switches to Workflow tab if Workflow  is clicked', async () => {
        const wrapper = factory()

        await flushPromises()
        await wrapper.find('.p-tabview-nav li:nth-child(1)').trigger('click')

        expect(wrapper.find('.p-tabview-nav li:nth-child(1)').html()).toContain('aria-selected="true"')
        expect(wrapper.find('.p-tabview-nav li:nth-child(2)').html()).toContain('aria-selected="false"')
    })

    it('save button is disabled if a mandatory input is empty', () => {
        const wrapper = factory()
        expect(wrapper.vm.selectedSchema).toStrictEqual({})
        expect(wrapper.vm.buttonDisabled).toBe(true)
    })
})
