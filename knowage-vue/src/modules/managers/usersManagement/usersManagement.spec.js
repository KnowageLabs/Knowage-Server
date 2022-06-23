import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import flushPromises from 'flush-promises'
import UsersManagement from './UsersManagement.vue'
import InputText from 'primevue/inputtext'
import ProgressBar from 'primevue/progressbar'
import Card from 'primevue/card'
import Toolbar from 'primevue/toolbar'

const mockedUsers = [
    {
        id: 1,
        userId: 'biadmin',
        fullName: 'Knowage Administrator',
        dtPwdBegin: null,
        dtPwdEnd: null,
        flgPwdBlocked: true,
        dtLastAccess: 1621249789000,
        isSuperadmin: true,
        defaultRoleId: null,
        failedLoginAttempts: 3,
        blockedByFailedLoginAttempts: false,
        sbiExtUserRoleses: [4],
        sbiUserAttributeses: {
            1: {
                name: 'Knowage Administrator'
            },
            5: {
                email: 'admin@eng.it'
            },
            7: {
                pr_ruolo: '%'
            },
            8: {
                pr_userid: '%'
            }
        }
    },
    {
        id: 3,
        userId: 'bidemo',
        fullName: 'Knowage Demo User',
        dtPwdBegin: null,
        dtPwdEnd: null,
        flgPwdBlocked: null,
        dtLastAccess: null,
        isSuperadmin: false,
        defaultRoleId: null,
        failedLoginAttempts: 0,
        blockedByFailedLoginAttempts: false,
        sbiExtUserRoleses: [6],
        sbiUserAttributeses: {
            1: {
                name: 'Knowage Demo User'
            },
            7: {
                pr_ruolo: '%'
            },
            8: {
                pr_userid: '%'
            }
        }
    },
    {
        id: 5,
        userId: 'bidev',
        fullName: 'Knowage Developer',
        dtPwdBegin: null,
        dtPwdEnd: null,
        flgPwdBlocked: null,
        dtLastAccess: null,
        isSuperadmin: false,
        defaultRoleId: 2,
        failedLoginAttempts: 0,
        blockedByFailedLoginAttempts: false,
        sbiExtUserRoleses: [1, 2, 3, 4, 20, 5, 6, 25, 26, 28, 13, 14],
        sbiUserAttributeses: {
            1: {
                name: 'Knowage Developer'
            },
            7: {
                pr_ruolo: '%'
            },
            8: {
                pr_userid: '%'
            }
        }
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() =>
        Promise.resolve({
            data: mockedUsers
        })
    ),
    post: vi.fn().mockImplementation(() => Promise.resolve()),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}

const $confirm = {
    require: vi.fn()
}
const $store = {
    commit: jest.fn()
}

const factory = () => {
    return mount(UsersManagement, {
        attachToDocument: true,
        global: {
            plugins: [],
            stubs: { Button, InputText, ProgressBar, Toolbar, Card },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $http
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Users Management loading', () => {
    it('show progress bar when loading', async () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })

    it('shows "no data" label when loaded empty', async () => {
        $http.get.mockReturnValueOnce(
            Promise.resolve({
                data: []
            })
        )
        const wrapper = factory()
        await flushPromises()
        expect(wrapper.vm.users.length).toBe(0)
        expect(wrapper.find('[data-test="users-list"]').html()).toContain('common.info.noDataFound')
    })
})

describe('Users Management', () => {
    it('deletes user clicking on delete icon', async () => {
        const wrapper = factory()
        await flushPromises()
        const deleteButton = wrapper.find('[data-test="deleteBtn"]')
        await deleteButton.trigger('click')
        expect($confirm.require).toHaveBeenCalledTimes(1)
        await wrapper.vm.onUserDelete(1)
        await wrapper.vm.loadAllUsers()
    })

    it("opens empty form when the '+' button is clicked", async () => {
        const wrapper = factory()
        const openButton = wrapper.find('[data-test="open-form-button"]')
        await openButton.trigger('click')
        expect(wrapper.vm.hiddenForm).toBe(false)
    })

    it('shows form when a row is clicked', async () => {
        const wrapper = factory()
        await flushPromises()
        const datalist = wrapper.find('[data-test="users-list"]')

        await datalist.find('ul li').trigger('click')

        expect(wrapper.vm.hiddenForm).toBe(false)
        expect(wrapper.vm.userDetailsForm).toStrictEqual({
            id: 1,
            userId: 'biadmin',
            fullName: 'Knowage Administrator',
            dtPwdBegin: null,
            dtPwdEnd: null,
            flgPwdBlocked: true,
            dtLastAccess: 1621249789000,
            isSuperadmin: true,
            defaultRoleId: null,
            failedLoginAttempts: 3,
            blockedByFailedLoginAttempts: false,
            sbiExtUserRoleses: [4],
            sbiUserAttributeses: {
                1: {
                    name: 'Knowage Administrator'
                },
                5: {
                    email: 'admin@eng.it'
                },
                7: {
                    pr_ruolo: '%'
                },
                8: {
                    pr_userid: '%'
                }
            }
        })
    })
})

describe('Users Management Search', () => {
    it('filters the list if a label or name is provided', async () => {
        const wrapper = factory()
        await flushPromises()
        const usersList = wrapper.find('[data-test="users-list"]')
        const searchInput = usersList.find('input')

        expect(usersList.html()).toContain('biadmin')
        expect(usersList.html()).toContain('bidemo')
        expect(usersList.html()).toContain('bidev')

        // User Id
        await searchInput.setValue('biadmin')
        await usersList.trigger('filter')
        expect(usersList.html()).toContain('biadmin')
        expect(usersList.html()).not.toContain('bidemo')
        expect(usersList.html()).not.toContain('bidev')

        // Full Name
        await searchInput.setValue('Knowage Administrator')
        await usersList.trigger('filter')
        expect(usersList.html()).not.toContain('Knowage Demo User')
        expect(usersList.html()).not.toContain('Knowage Developer')
        expect(usersList.html()).toContain('Knowage Administrator')
    })
    it('returns no data if the label is not present', async () => {
        const wrapper = factory()
        await flushPromises()
        const usersList = wrapper.find('[data-test="users-list"]')
        const searchInput = usersList.find('input')

        expect(usersList.html()).toContain('biadmin')
        expect(usersList.html()).toContain('bidemo')
        expect(usersList.html()).toContain('bidev')

        await searchInput.setValue('not present value')
        await usersList.trigger('filter')

        expect(usersList.html()).not.toContain('biadmin')
        expect(usersList.html()).not.toContain('bidemo')
        expect(usersList.html()).not.toContain('bidev')
    })
})
