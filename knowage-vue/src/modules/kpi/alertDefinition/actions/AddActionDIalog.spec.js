import { mount } from '@vue/test-utils'
import axios from 'axios'
import AddActionDialog from './AlertDefinitionActionDialog.vue'
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
import PrimeVue from 'primevue/config'

const mocedKpi = {
    threshold: {
        id: 49,
        description: 'Threshold_1',
        name: 'Threshold_1 (Clone)',
        typeId: 75,
        type: 'Range',
        thresholdValues: [
            {
                id: 153,
                position: 1,
                label: 'OK',
                color: '#007AFF',
                severityId: 78,
                severityCd: 'URGENT',
                minValue: 0.0,
                includeMin: false,
                maxValue: 30.0,
                includeMax: true
            },
            {
                id: 154,
                position: 2,
                label: 'Medium',
                color: '#FFFF00',
                severityId: 80,
                severityCd: 'MEDIUM',
                minValue: 30.0,
                includeMin: false,
                maxValue: 80.0,
                includeMax: true
            },
            {
                id: 155,
                position: 3,
                label: 'KO',
                color: '#FF0000',
                severityId: 81,
                severityCd: 'LOW',
                minValue: 80.0,
                includeMin: false,
                maxValue: 100.0,
                includeMax: true
            }
        ]
    }
}

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() =>
        Promise.resolve({
            data: []
        })
    ),
    delete: axios.delete.mockImplementation(() => Promise.resolve())
}

const factory = () => {
    return mount(AddActionDialog, {
        props: {
            selectedAction: { idAction: 62, thresholdValues: [] },
            dialogVisible: true,
            kpi: mocedKpi
        },
        global: {
            plugins: [PrimeVue],
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
                Chip,
                ExectuteEtlCard: true,
                ContextBrokerCard: true,
                SendMailCard: true
            },
            mocks: {
                $t: (msg) => msg,
                $http
            }
        }
    })
}
describe('Alert Definition kpi action', () => {
    it('shows a wysiwyg editor if send mail is selected', async () => {
        const wrapper = factory()
        await wrapper.setProps({ selectedAction: { className: 'it.eng.knowage.enterprise.tools.alert.action.SendMail', thresholdValues: [] } })
        expect(wrapper.vm.componentToShow).toBe('SendMailCard')
    })
    it('shows a form if context broker is selected', async () => {
        const wrapper = factory()
        await wrapper.setProps({ selectedAction: { className: 'it.eng.spagobi.tools.alert.action.NotifyContextBroker', thresholdValues: [] } })
        expect(wrapper.vm.componentToShow).toBe('ContextBrokerCard')
    })
    it('shows a selectable table if etl document is selected', async () => {
        const wrapper = factory()
        await wrapper.setProps({ selectedAction: { className: 'it.eng.knowage.enterprise.tools.alert.action.ExecuteETLDocument', thresholdValues: [] } })
        expect(wrapper.vm.componentToShow).toBe('ExectuteEtlCard')
    })
})
