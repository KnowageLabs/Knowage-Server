import { flushPromises, mount } from '@vue/test-utils'
import Button from 'primevue/button'
import { createTestingPinia } from '@pinia/testing'
import MenuConfiguration from './MenuManagement.vue'
import InputText from 'primevue/inputtext'
import ProgressBar from 'primevue/progressbar'
import Card from 'primevue/card'
import Toolbar from 'primevue/toolbar'
import KnHint from '@/components/UI/KnHint.vue'
import Tree from 'primevue/tree'
import axios from 'axios'

const $confirm = {
    require: vi.fn()
}

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() =>
        Promise.resolve({
            data: []
        })
    )
}

const factory = () => {
    return mount(MenuConfiguration, {
        attachToDocument: true,
        global: {
            plugins: [createTestingPinia()],
            stubs: { Button, InputText, MenuManagementDocumentBrowserTree: true, ProgressBar, Toolbar, Card, KnHint, Tree },
            mocks: {
                $t: (msg) => msg,

                $confirm,
                $http
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('menu configuration management loading', () => {
    it('show progress bar when loading', async () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
})

it('when loaded a tree with just the root is shown if no child are present', async () => {
    $http.get.mockReturnValueOnce(
        Promise.resolve({
            data: [
                {
                    menuId: 33,
                    objId: null,
                    objParameters: null,
                    subObjName: null,
                    snapshotName: null,
                    snapshotHistory: null,
                    functionality: 'WorkspaceManagement',
                    initialPath: 'analysis',
                    name: 'test',
                    descr: 'description',
                    parentId: null,
                    level: 1,
                    depth: null,
                    prog: 1,
                    hasChildren: false,
                    lstChildren: [],
                    roles: [],
                    viewIcons: false,
                    hideToolbar: false,
                    hideSliders: false,
                    staticPage: '',
                    code: null,
                    url: null,
                    iconPath: null,
                    icon: {
                        label: '',
                        className: 'fas fa-business-time',
                        unicode: 'null',
                        visible: 'true',
                        id: 90,
                        category: 'solid',
                        src: null
                    },
                    custIcon: null,
                    iconCls: null,
                    groupingMenu: null,
                    linkType: null,
                    adminsMenu: false,
                    externalApplicationUrl: null,
                    clickable: true
                }
            ]
        })
    )
    const wrapper = factory()
    const tree = wrapper.find('[data-test="menu-nodes-tree"]')

    await flushPromises()

    expect(wrapper.vm.menuNodes.length).toBe(1)
    expect(wrapper.vm.menuNodes.length).toBe(1)
    expect(tree.html()).toContain('test')
    expect(tree.html()).not.toContain('subnode')
    expect(tree.html()).not.toContain('subsubnode')
})

describe('menu configuration management', () => {
    it("opens empty form when the '+' button is clicked", async () => {
        const wrapper = factory()
        const openButton = wrapper.find('[data-test="open-form-button"]')
        await openButton.trigger('click')
        expect(wrapper.vm.hideForm).toBe(false)
    })
})
