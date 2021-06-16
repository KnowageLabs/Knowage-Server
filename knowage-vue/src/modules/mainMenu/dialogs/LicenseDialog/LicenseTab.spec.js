import { mount } from '@vue/test-utils'
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

const factory = (licenses) => {
    return mount(LicenceTab, {
        props: {
            licenses
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
                $t: (msg) => msg
            }
        }
    })
}

describe('License management', () => {
    it("shows a 'no license available' if no license is returned", () => {
        const wrapper = factory([])

        expect(wrapper.html()).toContain('licenseDialog.noLicense')
    })
    it('shows a list of available license when loaded', () => {
        const wrapper = factory(mockedLicenses)

        expect(wrapper.vm.licensesList).toStrictEqual(mockedLicenses)
        expect(wrapper.html()).toContain('KnowageSI')
        expect(wrapper.html()).toContain('KnowagePA')
    })
    xit('shows the technical informations of the used machine (hostName, hardwareId,cores)', () => {})
    it("shows the 'invalid license' label id a license is outdated", () => {
        const wrapper = factory(mockedLicenses)

        expect(wrapper.vm.licensesList).toStrictEqual(mockedLicenses)
        expect(wrapper.html()).toContain('KnowagePA')
        expect(wrapper.html()).toContain('licenseDialog.invalidLicense')
    })
    xit('clicking on the + button a file input dialog appears', () => {})
    xit('clicking on the edit button a file input dialog appears', () => {})
    xit('clicking on the download button a file download dialog appears', () => {})
})
