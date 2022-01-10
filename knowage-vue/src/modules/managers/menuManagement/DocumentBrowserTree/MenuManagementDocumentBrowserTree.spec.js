import { flushPromises, mount } from '@vue/test-utils'
import Button from 'primevue/button'
import DocumentBrowserTree from './MenuManagementDocumentBrowserTree.vue'
import InputText from 'primevue/inputtext'
import ProgressBar from 'primevue/progressbar'
import Card from 'primevue/card'
import Tree from 'primevue/tree'
import axios from 'axios'

const mockedElements = {
    functionality: [
        {
            id: 1,
            path: '/Functionalities',
            name: 'Functionalities',
            description: 'Functionalities',
            childs: [
                {
                    id: 2,
                    path: '/Functionalities/Marco',
                    name: 'Marco',
                    description: 'workspace',
                    childs: [
                        {
                            id: 15,
                            path: '/Functionalities/Marco/Demos',
                            name: 'Demos',
                            description: 'Demos'
                        },
                        {
                            id: 13,
                            path: '/Functionalities/Marco/CrossTab',
                            name: 'CrossTab',
                            description: ''
                        }
                    ]
                }
            ]
        }
    ]
}

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() =>
        Promise.resolve({
            data: mockedElements
        })
    )
}

const factory = () => {
    return mount(DocumentBrowserTree, {
        attachToDocument: true,
        global: {
            plugins: [],
            stubs: { Button, InputText, ProgressBar, Tree, Card },
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

describe('Document browser tree', () => {
    it('emits selectedDocumentNode when list item is clicked', async () => {
        const wrapper = factory()

        const selectedNode = {
            id: 38,
            path: '/Functionalities/Marco/Other',
            name: 'Other',
            description: 'Other stuff'
        }

        wrapper.vm.onNodeSelect(selectedNode)

        expect(wrapper.emitted()).toHaveProperty('selectedDocumentNode')
    })

    it('load functionalities on created hook', async () => {
        const wrapper = factory()

        wrapper.vm.loadFunctionalities()

        await flushPromises()

        expect(axios.get).toHaveBeenCalled
        expect(axios.get).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/menu/functionalities')

        expect(wrapper.vm.nodes).toStrictEqual([
            {
                children: [
                    {
                        children: [
                            {
                                children: [],
                                icon: 'pi pi-fw pi-folder',
                                key: 15,
                                label: 'Demos',
                                name: 'Demos',
                                path: '/Functionalities/Marco/Demos'
                            },
                            {
                                children: [],
                                icon: 'pi pi-fw pi-folder',
                                key: 13,
                                label: 'CrossTab',
                                name: 'CrossTab',
                                path: '/Functionalities/Marco/CrossTab'
                            }
                        ],
                        icon: 'pi pi-fw pi-folder',
                        key: 2,
                        label: 'Marco',
                        name: 'Marco',
                        path: '/Functionalities/Marco'
                    }
                ],
                icon: 'pi pi-fw pi-folder',
                key: 1,
                label: 'Functionalities',
                name: 'Functionalities',
                path: '/Functionalities'
            }
        ])
    })
})
