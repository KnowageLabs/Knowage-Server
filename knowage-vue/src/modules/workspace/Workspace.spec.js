import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { nextTick } from 'vue'
import Button from 'primevue/button'
import Sidebar from 'primevue/sidebar'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Listbox from 'primevue/listbox'
import InputText from 'primevue/inputtext'
import ProgressBar from 'primevue/progressbar'
import Workspace from './Workspace.vue'
import Toolbar from 'primevue/toolbar'

vi.mock('axios')

const crypto = require('crypto')

Object.defineProperty(global.self, 'crypto', {
    value: {
        getRandomValues: (arr) => crypto.randomBytes(arr.length)
    }
})

const $http = { get: vi.fn().mockImplementation(() => Promise.resolve({ data: [] })) }

const $router = {
    currentRoute: {
        _rawValue: {
            fullPath: '/workspace/'
        }
    },
    push: vi.fn()
}

const factory = () => {
    return mount(Workspace, {
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [
                createTestingPinia({
                    initialState: {
                        store: {
                            user: { isSuperadmin: true, functionalities: ['SaveIntoFolderFunctionality', 'CreateDocument', 'BuildQbeQueriesFunctionality'] }
                        }
                    }
                })
            ],
            stubs: {
                Button,
                Accordion,
                AccordionTab,
                InputText,
                Listbox,
                ProgressBar,
                Sidebar,
                Toolbar,
                'router-view': true
            },
            mocks: {
                $t: (msg) => msg,

                $http,
                $router
            }
        }
    })
}

describe('Workspace', () => {
    it('should show progress bar when loading', async () => {
        const wrapper = factory()

        wrapper.vm.loading = true

        await nextTick()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })

    it('the entries list shows correct elements', async () => {
        const wrapper = factory()

        wrapper.vm.displayMenu = true

        await nextTick()

        expect(wrapper.find('[data-test="menu-list"]').html()).toContain('workspace.menuLabels.recent')
        expect(wrapper.find('[data-test="menu-list"]').html()).toContain('workspace.menuLabels.myRepository')
        expect(wrapper.find('[data-test="menu-list"]').html()).not.toContain('workspace.menuLabels.myData')
        expect(wrapper.find('[data-test="menu-list"]').html()).toContain('workspace.menuLabels.myModels')
        expect(wrapper.find('[data-test="menu-list"]').html()).toContain('workspace.menuLabels.myAnalysis')
        expect(wrapper.find('[data-test="menu-list"]').html()).not.toContain('workspace.menuLabels.schedulation')
    })

    it("should show a folder tree if 'documents' is selected", async () => {
        const wrapper = factory()

        wrapper.vm.displayMenu = true

        await nextTick()

        await wrapper.find('[data-test="document-accordion"]').trigger('click')

        expect(wrapper.find('[data-test="document-tree"]').exists()).toBe(true)
    })
})
