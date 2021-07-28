import { mount } from '@vue/test-utils'
import AlertDetail from './AlertDetail.vue'

const factory = () => {
    return mount(AlertDetail, {
        global: {
            plugins: [],
            stubs: {},
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}
describe('Alert Definition Detail', () => {
    it('disables the save button if one required input is empty', () => {
        const formWrapper = factory()
        expect(formWrapper.vm.selectedAlert.name).toStrictEqual(undefined)
        expect(formWrapper.vm.selectedAlert.alertListener).toStrictEqual(undefined)
        expect(formWrapper.vm.buttonDisabled).toBe(true)
    })
    // it('disables the save button if no kpi is selected', () => {})
})
