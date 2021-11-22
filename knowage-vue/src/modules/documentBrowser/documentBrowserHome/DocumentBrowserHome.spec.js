import { flushPromises, mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import InputText from 'primevue/inputtext'
import DocumentBrowserHint from './DocumentBrowserHint.vue'
import ProgressBar from 'primevue/progressbar'
import DocumentBrowserHome from './DocumentBrowserHome.vue'
import Toolbar from 'primevue/toolbar'

const mockedFolder = {
    id: 725,
    key: 'Registry',
    name: 'Registry',
    parentId: 724,
    parentFolder: {
        data: {
            id: 724,
            key: 'Analytical Engines',
            name: 'Analytical Engines',
            parentId: 538,
            parentFolder: {
                data: {
                    id: 538,
                    key: 'Functionalities',
                    name: 'Functionalities',
                    parentId: null
                }
            }
        }
    }
}

const mockedBreadcrumb = {
    label: 'Analytical Documents',
    node: {
        chidlren: [],
        data: {
            id: 601,
            parentId: 538,
            name: 'Analytical Documents',
            description: 'Demo Analytical Documents',
            codType: 'LOW_FUNCT',
            path: '/Functionalities/Demo',
            parentFolder: { key: 'Functionalities', id: 538, parentID: null }
        },
        icon: 'pi pi-folder-open',
        id: 601,
        key: 'Analytical Documents',
        label: 'Analytical Documents'
    }
}

jest.mock('axios')

const $http = { get: axios.get.mockImplementation(() => Promise.resolve({ data: [] })) }

const $store = {
    state: {
        user: {}
    }
}

const factory = () => {
    return mount(DocumentBrowserHome, {
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Button,
                Card,
                DocumentBrowserHint,
                InputText,
                ProgressBar,
                DocumentBrowserTree: true,
                DocumentBrowserDetail: true,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $http
            }
        }
    })
}

describe('Document Browser Home', () => {
    it('should show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('should show a hint when no folder is selected', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.selectedFolder).toBe(null)
        expect(wrapper.find('[data-test="document-browser-hint"]').exists()).toBe(true)
    })
    it('creates breadcrumb from selected folder', () => {
        const wrapper = factory()

        wrapper.vm.selectedFolder = mockedFolder

        wrapper.vm.createBreadcrumbs()

        expect(wrapper.vm.breadcrumbs[0].label).toBe('Functionalities')
        expect(wrapper.vm.breadcrumbs[1].label).toBe('Analytical Engines')
        expect(wrapper.vm.breadcrumbs[2].label).toBe('Registry')
    })
    it('should move the current folder to the parent if the parent folder is selected the breadcrumbs', async () => {
        const wrapper = factory()

        expect(wrapper.vm.selectedFolder).toBe(null)

        await wrapper.vm.setSelectedBreadcrumb(mockedBreadcrumb)

        expect(wrapper.vm.selectedFolder).toStrictEqual(mockedBreadcrumb.node.data)
    })
})
