import { mount } from '@vue/test-utils'
import AlertDetail from './AlertDetail.vue'
import Toolbar from 'primevue/toolbar'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import Menu from 'primevue/menu'
import InputText from 'primevue/inputtext'
const factory = () => {
    return mount(AlertDetail, {
        global: {
            plugins: [],
            stubs: {
                Button,
                Card,
                Toolbar,
                Dropdown,
                Menu,
                InputText
            },
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
describe('Alert Definition kpi action', () => {
    it('shows a dialog when kpi action is clicked', async () => {
        // const wrapper = factory()
        // expect(wrapper.vm.isListenerSelected).toBe(true)
        // expect(wrapper.vm.actionList.length).toBe(1)
        // console.log(wrapper.html())
        // await wrapper.find('[data-test="add-action-button"]').trigger('click')
        // expect(wrapper.vm.dialogVisiable).toBe(true)
    })
    it('shows a wysiwyg editor if send mail is selected', () => {})
    it('shows a form if context broker is selected', () => {})
    it('shows a selectable table if etl document is selected', () => {})
})
