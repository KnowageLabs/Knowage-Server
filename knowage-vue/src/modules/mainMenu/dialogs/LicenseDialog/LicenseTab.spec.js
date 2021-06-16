import { mount } from '@vue/test-utils'
import axios from 'axios'
import Avatar from 'primevue/avatar'
import Button from 'primevue/button'
import FabButton from '@/components/UI/KnFabButton.vue'
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

jest.mock('axios', () => ({
    get: jest.fn(() => Promise.resolve({ data: [] }))
}))

const $store = {
    commit: jest.fn()
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
                FabButton,
                LicenceTab,
                Listbox,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $store
            }
        }
    })
}

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
    xit('clicking on the + button a file input dialog appears', () => {})
    xit('clicking on the edit button a file input dialog appears', () => {})
    it('clicking on the download button a file download dialog appears', async () => {
        const wrapper = factory(mockedLicenses, mockedHost)

        await wrapper.find('[data-test="download-button"]').trigger('click')

        expect(axios.get).toHaveBeenCalledWith('/knowage/restful-services/1.0/license/download/DESKTOP-TEST12/KnowageSI', { headers: { Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9' } })
    })
})
