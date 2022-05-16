import { mount, shallowMount } from '@vue/test-utils'
import MainMenu from './MainMenu.vue'
import { createStore } from 'vuex'
import flushPromises from 'flush-promises'
import Tooltip from 'primevue/tooltip'
import axios from 'axios'

const mockedEnduserData = {
    technicalUserFunctionalities: [
        {
            label: 'Profile Management',
            iconCls: 'fa fa-2x fa-id-card-o',
            items: [
                {
                    label: 'Profile Attributes',
                    to: '/profile-attributes-management'
                },
                {
                    label: 'Roles',
                    to: '/roles-management'
                },
                {
                    label: 'Users',
                    to: '/users-management'
                },
                {
                    label: 'Menu configuration',
                    to: '/menu-management'
                },
                {
                    label: 'Functionalities',
                    to: '/functionalities-management'
                }
            ]
        }
    ],
    commonUserFunctionalities: [
        {
            label: 'Home',
            iconCls: 'pi pi-fw pi-home',
            to: '/',
            visible: true
        }
    ],
    allowedUserFunctionalities: [
        {
            label: 'Documents browser',
            to: '/document-browser',
            iconCls: 'far fa-folder-open',
            visible: true
        }
    ],
    dynamicUserFunctionalities: [
        {
            iconCls: 'far fa-hospital',
            label: 'Cockpit',
            descr: 'Cockpit',
            prog: 1,
            items: [
                {
                    label: 'Sales analysis',
                    descr: 'Sales analysis',
                    to: '/knowage/servlet/AdapterHTTP?ACTION_NAME=MENU_BEFORE_EXEC&MENU_ID=158',
                    prog: 1,
                    roles: ['/demo/admin', '/demo/user']
                },
                {
                    label: 'Inventory analysis',
                    descr: 'Inventory analysis',
                    to: '/knowage/servlet/AdapterHTTP?ACTION_NAME=MENU_BEFORE_EXEC&MENU_ID=167',
                    prog: 2,
                    roles: ['/demo/admin', '/demo/user']
                },
                {
                    label: 'Solr',
                    descr: 'Solr',
                    to: '/knowage/servlet/AdapterHTTP?ACTION_NAME=MENU_BEFORE_EXEC&MENU_ID=166',
                    prog: 3,
                    roles: ['/demo/admin', '/demo/user']
                }
            ],
            roles: ['/demo/admin', '/demo/user']
        }
    ]
}

const store = createStore({
    state() {
        return {
            locale: 'en_US',
            licenses: {
                hosts: [],
                licenses: {},
                cpuNumber: -1
            }
        }
    }
})

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/menu/enduser?locale=en-US`:
                return Promise.resolve({ data: mockedEnduserData })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

jest.mock('axios', () => ({
    get: jest.fn(() => Promise.resolve({ data: { technicalUserFunctionalities: [{ items: [{ label: 'Data source' }] }] } }))
}))

const factory = () => {
    return shallowMount(MainMenu, {
        attachToDocument: true,
        global: {
            directives: {
                tooltip: Tooltip
            },
            plugins: [store],
            stubs: {
                'router-link': true
            },
            mocks: {
                $t: (msg) => msg,
                $i18n: {
                    fallbackLocale: 'en_US'
                },
                localObject: { locale: 'en_US' },
                $http
            }
        }
    })
}

describe('Main Menu', () => {
    test('is administrator', async () => {
        const wrapper = factory()
        wrapper.vm.localObject.locale = wrapper.vm.localObject.locale.replaceAll('_', '-')

        expect(axios.get).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '3.0/menu/enduser?locale=' + wrapper.vm.localObject.locale)
        await flushPromises()
        console.log('TEEEEEEEST: ', wrapper.vm.technicalUserFunctionalities.length)
        expect(wrapper.vm.technicalUserFunctionalities.length).not.toBe(0)
    })
})

describe('Main Menu', () => {
    test('is loaded empty', () => {
        const wrapper = factory()
        axios.get.mockReturnValueOnce(
            Promise.resolve({
                data: []
            })
        )
        expect(wrapper.vm.dynamicUserFunctionalities).toStrictEqual([])
        expect(wrapper.vm.commonUserFunctionalities).toStrictEqual([])
    })
})
