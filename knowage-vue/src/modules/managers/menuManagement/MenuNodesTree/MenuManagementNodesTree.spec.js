import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import MenuNodesTree from './MenuManagementNodesTree.vue'
import InputText from 'primevue/inputtext'
import ProgressBar from 'primevue/progressbar'
import Card from 'primevue/card'
import Tree from 'primevue/tree'

const mockedElements = [
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
        parentId: 38,
        level: 1,
        depth: null,
        prog: 1,
        hasChildren: false,
        lstChildren: [],
        roles: [
            {
                id: 4,
                name: 'admin',
                description: 'admin',
                roleTypeCD: 'ADMIN',
                code: null,
                roleTypeID: 28,
                organization: 'DEFAULT_TENANT',
                isPublic: false,
                ableToCreateSelfServiceCockpit: false,
                ableToCreateSelfServiceGeoreport: false,
                ableToCreateSelfServiceKpi: false,
                defaultRole: false,
                roleMetaModelCategories: null,
                ableToManageInternationalization: true,
                ableToSendMail: false,
                ableToSeeNotes: true,
                ableToManageGlossaryTechnical: false,
                ableToManageKpiValue: false,
                ableToManageCalendar: false,
                ableToEnableCopyAndEmbed: false,
                ableToEnableDatasetPersistence: false,
                ableToManageGlossaryBusiness: false,
                ableToUseFunctionsCatalog: false,
                ableToEnableFederatedDataset: false,
                ableToEnablePrint: false,
                ableToSaveSubobjects: false,
                ableToEnableRate: false,
                ableToCreateCustomChart: true,
                ableToEditPythonScripts: true,
                ableToSeeSubobjects: false,
                ableToSaveIntoPersonalFolder: false,
                ableToDeleteKpiComm: false,
                ableToSeeToDoList: false,
                ableToHierarchiesManagement: false,
                ableToSeeFavourites: false,
                ableToSaveMetadata: true,
                ableToSeeMetadata: true,
                ableToDoMassiveExport: false,
                ableToCreateSocialAnalysis: false,
                ableToEditMyKpiComm: false,
                ableToSeeDocumentBrowser: true,
                ableToSeeViewpoints: false,
                ableToSeeSnapshots: false,
                ableToBuildQbeQuery: false,
                ableToEditAllKpiComm: false,
                ableToSaveRememberMe: false,
                ableToSeeMyData: true,
                ableToCreateDocuments: false,
                ableToSeeMyWorkspace: true,
                ableToSeeSubscriptions: false,
                ableToViewSocialAnalysis: false,
                ableToManageUsers: false,
                ableToRunSnapshots: false
            },
            {
                id: 25,
                name: 'demo admin',
                description: 'demo admin',
                roleTypeCD: 'ADMIN',
                code: 'demo_admin',
                roleTypeID: 28,
                organization: 'DEFAULT_TENANT',
                isPublic: false,
                ableToCreateSelfServiceCockpit: false,
                ableToCreateSelfServiceGeoreport: false,
                ableToCreateSelfServiceKpi: false,
                defaultRole: false,
                roleMetaModelCategories: null,
                ableToManageInternationalization: false,
                ableToSendMail: false,
                ableToSeeNotes: false,
                ableToManageGlossaryTechnical: false,
                ableToManageKpiValue: false,
                ableToManageCalendar: false,
                ableToEnableCopyAndEmbed: false,
                ableToEnableDatasetPersistence: false,
                ableToManageGlossaryBusiness: false,
                ableToUseFunctionsCatalog: false,
                ableToEnableFederatedDataset: false,
                ableToEnablePrint: false,
                ableToSaveSubobjects: false,
                ableToEnableRate: false,
                ableToCreateCustomChart: false,
                ableToEditPythonScripts: false,
                ableToSeeSubobjects: false,
                ableToSaveIntoPersonalFolder: false,
                ableToDeleteKpiComm: false,
                ableToSeeToDoList: false,
                ableToHierarchiesManagement: false,
                ableToSeeFavourites: false,
                ableToSaveMetadata: false,
                ableToSeeMetadata: false,
                ableToDoMassiveExport: false,
                ableToCreateSocialAnalysis: false,
                ableToEditMyKpiComm: false,
                ableToSeeDocumentBrowser: false,
                ableToSeeViewpoints: false,
                ableToSeeSnapshots: false,
                ableToBuildQbeQuery: false,
                ableToEditAllKpiComm: false,
                ableToSaveRememberMe: false,
                ableToSeeMyData: false,
                ableToCreateDocuments: false,
                ableToSeeMyWorkspace: false,
                ableToSeeSubscriptions: false,
                ableToViewSocialAnalysis: false,
                ableToManageUsers: false,
                ableToRunSnapshots: false
            },
            {
                id: 13,
                name: '/kte/admin',
                description: '/kte/admin',
                roleTypeCD: 'ADMIN',
                code: null,
                roleTypeID: 28,
                organization: 'DEFAULT_TENANT',
                isPublic: false,
                ableToCreateSelfServiceCockpit: false,
                ableToCreateSelfServiceGeoreport: false,
                ableToCreateSelfServiceKpi: false,
                defaultRole: false,
                roleMetaModelCategories: null,
                ableToManageInternationalization: false,
                ableToSendMail: false,
                ableToSeeNotes: false,
                ableToManageGlossaryTechnical: false,
                ableToManageKpiValue: false,
                ableToManageCalendar: false,
                ableToEnableCopyAndEmbed: false,
                ableToEnableDatasetPersistence: false,
                ableToManageGlossaryBusiness: false,
                ableToUseFunctionsCatalog: false,
                ableToEnableFederatedDataset: false,
                ableToEnablePrint: false,
                ableToSaveSubobjects: false,
                ableToEnableRate: false,
                ableToCreateCustomChart: false,
                ableToEditPythonScripts: false,
                ableToSeeSubobjects: false,
                ableToSaveIntoPersonalFolder: false,
                ableToDeleteKpiComm: false,
                ableToSeeToDoList: false,
                ableToHierarchiesManagement: false,
                ableToSeeFavourites: false,
                ableToSaveMetadata: false,
                ableToSeeMetadata: false,
                ableToDoMassiveExport: false,
                ableToCreateSocialAnalysis: false,
                ableToEditMyKpiComm: false,
                ableToSeeDocumentBrowser: false,
                ableToSeeViewpoints: false,
                ableToSeeSnapshots: false,
                ableToBuildQbeQuery: false,
                ableToEditAllKpiComm: false,
                ableToSaveRememberMe: false,
                ableToSeeMyData: false,
                ableToCreateDocuments: false,
                ableToSeeMyWorkspace: false,
                ableToSeeSubscriptions: false,
                ableToViewSocialAnalysis: false,
                ableToManageUsers: false,
                ableToRunSnapshots: false
            }
        ],
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
            unicode: null,
            visible: true,
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
    },
    {
        menuId: 35,
        objId: null,
        objParameters: null,
        subObjName: null,
        snapshotName: null,
        snapshotHistory: null,
        functionality: 'DocumentUserBrowser',
        initialPath: '/Functionalities/Marco/CrossTab',
        name: 'subsubnode',
        descr: 'desc',
        parentId: 33,
        level: 2,
        depth: null,
        prog: 1,
        hasChildren: false,
        lstChildren: [],
        roles: [],
        viewIcons: false,
        hideToolbar: false,
        hideSliders: false,
        staticPage: 'homePageWithLinks.html',
        code: null,
        url: null,
        iconPath: null,
        icon: null,
        custIcon: null,
        iconCls: null,
        groupingMenu: null,
        linkType: null,
        adminsMenu: false,
        externalApplicationUrl: null,
        clickable: true
    },
    {
        menuId: 32,
        objId: null,
        objParameters: null,
        subObjName: null,
        snapshotName: null,
        snapshotHistory: null,
        functionality: 'WorkspaceManagement',
        initialPath: 'datasets',
        name: 'subnode',
        descr: 'desc',
        parentId: 35,
        level: 2,
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
        icon: null,
        custIcon: null,
        iconCls: null,
        groupingMenu: null,
        linkType: null,
        adminsMenu: false,
        externalApplicationUrl: null,
        clickable: true
    },
    {
        menuId: 38,
        objId: 129,
        objParameters: 'test param',
        subObjName: null,
        snapshotName: null,
        snapshotHistory: null,
        functionality: null,
        initialPath: null,
        name: 'moveDownTest',
        descr: 'test',
        parentId: null,
        level: 1,
        depth: null,
        prog: 2,
        hasChildren: false,
        lstChildren: [],
        roles: [],
        viewIcons: false,
        hideToolbar: false,
        hideSliders: false,
        staticPage: null,
        code: null,
        url: null,
        iconPath: null,
        icon: {
            label: '',
            className: 'fas fa-blender',
            unicode: null,
            visible: true,
            id: 61,
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
    },
    {
        menuId: 41,
        objId: null,
        objParameters: null,
        subObjName: null,
        snapshotName: null,
        snapshotHistory: null,
        functionality: null,
        initialPath: null,
        name: 'Moveup test',
        descr: 'test',
        parentId: 38,
        level: 2,
        depth: null,
        prog: 5,
        hasChildren: false,
        lstChildren: [],
        roles: [],
        viewIcons: false,
        hideToolbar: false,
        hideSliders: false,
        staticPage: null,
        code: null,
        url: null,
        iconPath: null,
        icon: null,
        custIcon: null,
        iconCls: null,
        groupingMenu: null,
        linkType: null,
        adminsMenu: false,
        externalApplicationUrl: null,
        clickable: true
    },
    {
        menuId: 50,
        objId: 130,
        objParameters: 'test param',
        subObjName: null,
        snapshotName: null,
        snapshotHistory: null,
        functionality: null,
        initialPath: null,
        name: 'moveDownTest',
        descr: 'test',
        parentId: null,
        level: 1,
        depth: null,
        prog: 9,
        hasChildren: false,
        lstChildren: [],
        roles: [],
        viewIcons: false,
        hideToolbar: false,
        hideSliders: false,
        staticPage: null,
        code: null,
        url: null,
        iconPath: null,
        icon: {
            label: '',
            className: 'fas fa-blender',
            unicode: null,
            visible: true,
            id: 61,
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

const factory = () => {
    return mount(MenuNodesTree, {
        props: {
            elements: [],
            loading: false
        },
        attachToDocument: true,
        global: {
            plugins: [],
            stubs: { Button, InputText, ProgressBar, Tree, Card },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Menu Nodes Tree', () => {
    it('shows expanded tree', async () => {
        const wrapper = factory()

        await wrapper.setProps({ elements: mockedElements })

        expect(wrapper.vm.expandedKeys[33]).toBe(true)
        expect(wrapper.vm.expandedKeys[35]).toBe(true)
        expect(wrapper.vm.expandedKeys[38]).toBe(true)
    })
    it('emits selectedMenuNode when list item is clicked', async () => {
        const wrapper = factory()

        await wrapper.setProps({ elements: mockedElements })
        await wrapper.find('[data-test="menu-nodes-tree-item-33"]').trigger('click')

        expect(wrapper.emitted()).toHaveProperty('selectedMenuNode')
    }),
        it('emits deleteMenuNode when delete button is clicked', async () => {
            const wrapper = factory()

            await wrapper.setProps({ elements: mockedElements })
            await wrapper.find('[data-test="delete-button-32"]').trigger('click')

            expect(wrapper.emitted()).toHaveProperty('deleteMenuNode')
            expect(wrapper.emitted().deleteMenuNode[0][0]).toEqual(32)
        })
    it('emits changeWithFather when button is clicked', async () => {
        const wrapper = factory()

        await wrapper.setProps({ elements: mockedElements })
        await wrapper.find('[data-test="change-with-father-button-32"]').trigger('click')

        expect(wrapper.emitted()).toHaveProperty('changeWithFather')
        expect(wrapper.emitted().changeWithFather[0][0]).toEqual(32)
    })

    it('emits moveUp when button is clicked', async () => {
        const wrapper = factory()

        await wrapper.setProps({ elements: mockedElements })
        await wrapper.find('[data-test="move-up-button-41"]').trigger('click')

        expect(wrapper.emitted()).toHaveProperty('moveUp')
        expect(wrapper.emitted().moveUp[0][0]).toEqual(41)
    })

    it('emits moveDown when button is clicked', async () => {
        const wrapper = factory()

        await wrapper.setProps({ elements: mockedElements })
        await wrapper.find('[data-test="move-down-button-50"]').trigger('click')

        expect(wrapper.emitted()).toHaveProperty('moveDown')
        expect(wrapper.emitted().moveDown[0][0]).toEqual(50)
    })
})
