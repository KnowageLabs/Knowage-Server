import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Avatar from 'primevue/avatar'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import FabButton from '@/components/UI/KnFabButton.vue'
import flushPromises from 'flush-promises'
import KnInputFile from '@/components/UI/KnInputFile.vue'
import Listbox from 'primevue/listbox'
import LicenceTab from './LicenseTab.vue'
import Toolbar from 'primevue/toolbar'

const mockedLicenses = [
    {
        licenseId: 1,
        product: 'KnowageSI',
        status: 'LICENSE_VALID',
        expiration_date: 'Sat Jun 04 16:46:21 CEST 2022',
        expiration_date_format: '2022-06-04'
    },
    {
        licenseId: 2,
        product: 'KnowagePA',
        status: 'LICENSE_INVALID',
        expiration_date: 'Sat Jun 04 16:50:23 CEST 2020',
        expiration_date_format: '2020-06-04'
    }
]

const mockedHost = {
    hostName: 'DESKTOP-TEST12',
    hardwareId: '123456789qwertyuiopasdfghhjklzxcvbnm123456789qwertyuiopasdfghjkl'
}

vi.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() => Promise.resolve({ data: [] })),
    post: vi.fn().mockImplementation(() => Promise.resolve({ data: [] }))
}

const $store = {
    commit: jest.fn(),
    dispatch: jest.fn()
}

const $confirm = {
    require: vi.fn()
}

const factory = (licenses, host) => {
    return mount(LicenceTab, {
        props: {
            licenses,
            host
        },
        global: {
            stubs: {
                Avatar,
                Button,
                Dialog,
                FabButton,
                KnInputFile,
                LicenceTab,
                Listbox,
                Toolbar
            },
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

describe('License management', () => {
    it("shows a 'no license available' if no license is returned", () => {
        const wrapper = factory([], {})

        expect(wrapper.html()).toContain('licenseDialog.noLicense')
    })
    it('shows a list of available license when loaded', () => {
        const wrapper = factory(mockedLicenses, mockedHost)

        expect(wrapper.vm.licensesList).toStrictEqual(mockedLicenses)
        expect(wrapper.html()).toContain('KnowageSI')
        expect(wrapper.html()).toContain('KnowagePA')
    })
    it('shows the technical informations of the used machine (hostName, hardwareId, cores)', () => {
        const wrapper = factory(mockedLicenses, mockedHost)
        const hostInfo = wrapper.find('[data-test="host-info"]')

        expect(wrapper.vm.host).toStrictEqual(mockedHost)
        expect(hostInfo.html()).toContain('DESKTOP-TEST12')
        expect(hostInfo.html()).toContain('123456789qwertyuiopasdfghhjklzxcvbnm123456789qwertyuiopasdfghjkl')
    })
    it("shows the 'invalid license' label id a license is outdated", () => {
        const wrapper = factory(mockedLicenses, mockedHost)

        expect(wrapper.vm.licensesList).toStrictEqual(mockedLicenses)
        expect(wrapper.html()).toContain('KnowagePA')
        expect(wrapper.html()).toContain('licenseDialog.invalidLicense')
    })
    it('clicking on the + button a file input dialog appears', async () => {
        const wrapper = factory(mockedLicenses, mockedHost)
        const formData = new FormData()
        formData.append('file', 'KnowageSI')

        await wrapper.find('[data-test="new-button"]').trigger('click')

        wrapper.vm.startUpload('KnowageSI')
        await flushPromises()

        expect($http.post).toHaveBeenCalledWith('/knowage/restful-services/1.0/license/upload/DESKTOP-TEST12?isForUpdate=false', formData, { headers: { 'Content-Type': 'multipart/form-data', 'X-Disable-Errors': 'true' } })
        expect(wrapper.emitted().reloadList).toBeTruthy()
        expect($store.commit).toHaveBeenCalledTimes(1)
    })
    it('clicking on the edit button a file input dialog appears', async () => {
        const wrapper = factory(mockedLicenses, mockedHost)
        const formData = new FormData()
        formData.append('file', 'KnowageSI')

        await wrapper.find('[data-test="edit-button"]').trigger('click')

        wrapper.vm.startUpload('KnowageSI')
        await flushPromises()

        expect($http.post).toHaveBeenCalledWith('/knowage/restful-services/1.0/license/upload/DESKTOP-TEST12?isForUpdate=true', formData, { headers: { 'Content-Type': 'multipart/form-data', 'X-Disable-Errors': 'true' } })
        expect(wrapper.emitted().reloadList).toBeTruthy()
        expect($store.commit).toHaveBeenCalledTimes(1)
    })
    it('clicking on the download button a file download dialog appears', async () => {
        const wrapper = factory(mockedLicenses, mockedHost)

        await wrapper.find('[data-test="download-button"]').trigger('click')

        expect($http.get).toHaveBeenCalledWith('/knowage/restful-services/1.0/license/download/DESKTOP-TEST12/KnowageSI', { headers: { Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9' } })
    })
    it('clicking on the delete button a delete request is sent', async () => {
        const wrapper = factory(mockedLicenses, mockedHost)

        await wrapper.find('[data-test="delete-button"]').trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)

        await wrapper.vm.deleteLicense('KnowageSI')

        expect($http.get).toHaveBeenCalledWith('/knowage/restful-services/1.0/license/delete/DESKTOP-TEST12/KnowageSI', { headers: { Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9' } })
    })
})
