import { mount } from '@vue/test-utils'
import AddActionDialog from './AddActionDialog.vue'
import Toolbar from 'primevue/toolbar'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import Menu from 'primevue/menu'
import InputText from 'primevue/inputtext'
import Dialog from 'primevue/dialog'
import MultiSelect from 'primevue/multiselect'
import ColorPicker from 'primevue/colorpicker'
import Chip from 'primevue/chip'

const mockedAction = {
    jsonActionParameters: {},
    idAction: null,
    thresholdData: null
}

const factory = (selectedAction, dialogVisible) => {
    return mount(AddActionDialog, {
        props: {
            selectedAction,
            dialogVisible
        },
        global: {
            plugins: [],
            stubs: {
                Button,
                Card,
                Toolbar,
                Dropdown,
                Menu,
                InputText,
                Dialog,
                MultiSelect,
                ColorPicker,
                Chip
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}
describe('Alert Definition kpi action', () => {
    it('shows a wysiwyg editor if send mail is selected', () => {
        const wrapper = factory(mockedAction, true)
        console.log(wrapper.html())
    })
    it('shows a form if context broker is selected', () => {})
    it('shows a selectable table if etl document is selected', () => {})
})
