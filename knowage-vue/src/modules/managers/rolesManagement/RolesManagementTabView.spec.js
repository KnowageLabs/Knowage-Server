import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Column from 'primevue/column'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import RolesManagementTabView from './RolesManagementTabView.vue'
import ProgressBar from 'primevue/progressbar'
import TabPanel from 'primevue/tabpanel'
import TabView from 'primevue/tabview'
import Toolbar from 'primevue/toolbar'

const mockedBuissnesModelList = [
    {
        VALUE_NM: 'Default Model Category',
        VALUE_DS: 'Default Model Category',
        VALUE_ID: 172,
        VALUE_CD: 'Default Model Category'
    },
    {
        VALUE_NM: 'Examples',
        VALUE_DS: 'Category Meta Model',
        VALUE_ID: 263,
        VALUE_CD: 'Example'
    }
]
const mockedDataSetList = [
    {
        VALUE_NM: 'Default Dataset Category',
        VALUE_DS: 'Default Dataset Category',
        VALUE_ID: 152,
        VALUE_CD: 'Default Dataset Category'
    },
    {
        VALUE_NM: 'Sales',
        VALUE_DS: 'Sales',
        VALUE_ID: 250,
        VALUE_CD: 'Sales'
    }
]
const mockedKpiCategoriesList = [
    {
        VALUE_NM: 'PRODUCT',
        VALUE_DS: 'PRODUCT',
        VALUE_ID: 256,
        VALUE_CD: 'PRODUCT'
    },
    {
        VALUE_NM: 'RICAVI',
        VALUE_DS: 'RICAVI',
        VALUE_ID: 257,
        VALUE_CD: 'RICAVI'
    }
]
const mockedRoleCategories = [
    {
        roleId: 1,
        categoryId: 172
    },
    {
        roleId: 1,
        categoryId: 152
    },
    {
        roleId: 1,
        categoryId: 256
    }
]
const mockedRoleTypes = [
    {
        VALUE_NM: 'Administrative role',
        VALUE_DS: 'Administrative role for developer users',
        VALUE_ID: 28,
        VALUE_CD: 'ADMIN'
    },
    {
        VALUE_NM: 'Developer role',
        VALUE_DS: 'Developer role for developer users',
        VALUE_ID: 29,
        VALUE_CD: 'DEV_ROLE'
    }
]
const mockedAuthorizations = [
    {
        name: 'CREATE_DOCUMENTS'
    },
    {
        name: 'SEE_DOCUMENT_BROWSER'
    }
]
const mockedRole = {
    id: 1,
    name: 'dev',
    description: 'dev',
    roleTypeCD: 'DEV_ROLE',
    code: '1234',
    roleTypeID: 29,
    organization: 'DEFAULT_TENANT',
    isPublic: true,
    ableToSendMail: false,
    ableToManageUsers: true
}

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=BM_CATEGORY`:
                return Promise.resolve({ data: mockedBuissnesModelList })
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=DATASET_CATEGORY`:
                return Promise.resolve({ data: mockedDataSetList })
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=KPI_KPI_CATEGORY`:
                return Promise.resolve({ data: mockedKpiCategoriesList })
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=ROLE_TYPE`:
                return Promise.resolve({ data: mockedRoleTypes })
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/roles/categories/1`:
                return Promise.resolve({ data: mockedRoleCategories })
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + 'authorizations':
                return Promise.resolve({ data: { root: mockedAuthorizations } })
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/roles/1`:
                return Promise.resolve({ data: mockedRole })
        }
    }),
    post: axios.post.mockImplementation(() => Promise.resolve())
}

const $store = {
    commit: jest.fn()
}

const $router = {
    replace: jest.fn()
}

const factory = () => {
    return mount(RolesManagementTabView, {
        global: {
            stubs: {
                Button,
                Column,
                Card,
                Checkbox,
                DataTable,
                Dropdown,
                KnValidationMessages,
                InputText,
                ProgressBar,
                TabPanel,
                TabView,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $router,
                $http
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Roles Management Tab View', () => {
    it('switches to Authorization tab if authorization is clicked', async () => {
        const wrapper = factory()

        await flushPromises()
        await wrapper.find('.p-tabview-nav li:nth-child(2)').trigger('click')

        expect(wrapper.find('[role="tabpanel"]:nth-child(2)').html()).toContain('managers.rolesManagement.authorizations.createDocuments')
        expect(wrapper.find('[role="tabpanel"]:nth-child(2)').html()).toContain('managers.rolesManagement.authorizations.viewDocBrowser')
        expect(wrapper.vm.authorizationList).toStrictEqual(mockedAuthorizations)
    })
    it('switches to Business Models tab if Business Models is clicked', async () => {
        const expectedBusinessModels = [
            { categoryId: 172, categoryName: 'Default Model Category' },
            { categoryId: 263, categoryName: 'Examples' }
        ]
        const wrapper = factory()

        expect(wrapper.find('[role="tabpanel"]:nth-child(3)').html()).toContain('common.info.noDataFound')

        await flushPromises()
        await wrapper.find('.p-tabview-nav li:nth-child(3)').trigger('click')

        expect(wrapper.find('[role="tabpanel"]:nth-child(3)').html()).toContain('Default Model Category')
        expect(wrapper.vm.businessModelList).toStrictEqual(expectedBusinessModels)
    })
    it('switches to Datasets tab if Datasets is clicked', async () => {
        const expectedDatasets = [
            { categoryId: 152, categoryName: 'Default Dataset Category' },
            { categoryId: 250, categoryName: 'Sales' }
        ]
        const wrapper = factory()

        expect(wrapper.find('[role="tabpanel"]:nth-child(4)').html()).toContain('common.info.noDataFound')

        await flushPromises()
        await wrapper.find('.p-tabview-nav li:nth-child(4)').trigger('click')

        expect(wrapper.find('[role="tabpanel"]:nth-child(4)').html()).toContain('Default Dataset Category')
        expect(wrapper.vm.dataSetList).toStrictEqual(expectedDatasets)
    })
    it('switches to KPI Categories tab if KPI Categories is clicked', async () => {
        const expectedKpiCategories = [
            { categoryId: 256, categoryName: 'PRODUCT' },
            { categoryId: 257, categoryName: 'RICAVI' }
        ]
        const wrapper = factory()

        expect(wrapper.find('[role="tabpanel"]:nth-child(5)').html()).toContain('common.info.noDataFound')

        await flushPromises()
        await wrapper.find('.p-tabview-nav li:nth-child(5)').trigger('click')

        expect(wrapper.find('[role="tabpanel"]:nth-child(5)').html()).toContain('PRODUCT')
        expect(wrapper.vm.kpiCategoriesList).toStrictEqual(expectedKpiCategories)
    })

    it('save button is disabled if a mandatory input is empty', () => {
        const wrapper = factory()
        expect(wrapper.vm.selectedRole).toStrictEqual({})
        expect(wrapper.vm.buttonDisabled).toBe(true)
    })

    it('loads correct role and shows succes info if it is saved', async () => {
        const wrapper = factory()
        wrapper.setProps({ id: '1' })

        await flushPromises()

        expect(wrapper.vm.selectedRole).toStrictEqual(mockedRole)
        expect(wrapper.vm.selectedBusinessModels).toStrictEqual([{ categoryId: 172, categoryName: 'Default Model Category' }])
        expect(wrapper.vm.selectedDataSets).toStrictEqual([{ categoryId: 152, categoryName: 'Default Dataset Category' }])
        expect(wrapper.vm.selectedKPICategories).toStrictEqual([{ categoryId: 256, categoryName: 'PRODUCT' }])

        wrapper.vm.v$.$invalid = false
        wrapper.vm.handleSubmit()

        await flushPromises()

        expect(axios.post).toHaveBeenCalledTimes(1)
        expect(axios.post).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/roles/1', { ...mockedRole, roleMetaModelCategories: [{ categoryId: 172 }, { categoryId: 152 }, { categoryId: 256 }] })
        expect($store.commit).toHaveBeenCalledTimes(1)
        expect(wrapper.emitted()).toHaveProperty('inserted')
        expect($router.replace).toHaveBeenCalledWith('/roles-management')
    })

    it('shows success info if new data is saved', async () => {
        const wrapper = factory()
        wrapper.vm.selectedRole = mockedRole
        wrapper.vm.selectedBusinessModels = [{ categoryId: 172 }]
        wrapper.vm.selectedDataSets = [{ categoryId: 152 }]
        wrapper.vm.selectedKPICategories = [{ categoryId: 256 }]
        delete wrapper.vm.selectedRole.id
        wrapper.vm.v$.$invalid = false
        wrapper.vm.handleSubmit()

        await flushPromises()

        expect(axios.post).toHaveBeenCalledTimes(1)
        expect(axios.post).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/roles/', { ...mockedRole, roleMetaModelCategories: [{ categoryId: 172 }, { categoryId: 152 }, { categoryId: 256 }] })
        expect($store.commit).toHaveBeenCalledTimes(1)
        expect(wrapper.emitted()).toHaveProperty('inserted')
        expect($router.replace).toHaveBeenCalledWith('/roles-management')
    })
})
