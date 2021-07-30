import { mount } from '@vue/test-utils'
import { VCodeMirror } from 'vue3-code-mirror'
import axios from 'axios'
import AutoComplete from 'primevue/autocomplete'
import Chip from 'primevue/chip'
import CodeMirror from 'codemirror'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'
import Listbox from 'primevue/listbox'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import MeasureDefinitionTabView from './MeasureDefinitionTabView.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

import 'codemirror/lib/codemirror.css'
import 'codemirror/theme/monokai.css'
import 'codemirror/theme/eclipse.css'
import 'codemirror/addon/hint/show-hint.css'
import 'codemirror/addon/hint/show-hint.js'
import 'codemirror/addon/hint/sql-hint.js'
import 'codemirror/mode/htmlmixed/htmlmixed.js'
import 'codemirror/mode/javascript/javascript.js'
import 'codemirror/mode/python/python.js'
import 'codemirror/mode/xml/xml.js'
import 'codemirror/mode/sql/sql.js'

const mockedRule = {
    id: 1,
    ruleOutputs: [
        {
            alias: 'asdasda',
            aliasId: 491,
            author: 'demo_admin',
            category: {
                valueCd: 'Category one'
            },
            hierarchy: null,
            rule: 'Bojan Test 123154',
            type: {
                domainCode: 'KPI_RULEOUTPUT_TYPE',
                domainName: 'KPI Rule Output types',
                translatedValueDescription: 'Measure',
                translatedValueName: 'Measure',
                valueCd: 'MEASURE',
                valueDescription: 'sbidomains.ds.measure',
                valueId: 234,
                valueName: 'sbidomains.nm.measure'
            }
        },
        {
            alias: 'Demo alias 2',
            aliasIcon: 'fa fa-exclamation-triangle icon-used',
            category: {
                valueCd: 'Category two'
            },
            hierarchy: null,
            rule: 'Bojan Test 123154',
            type: {
                domainCode: 'KPI_RULEOUTPUT_TYPE',
                domainName: 'KPI Rule Output types',
                translatedValueDescription: 'Measure',
                translatedValueName: 'Measure',
                valueCd: 'MEASURE',
                valueDescription: 'sbidomains.ds.measure',
                valueId: 234,
                valueName: 'sbidomains.nm.measure'
            }
        }
    ]
}

const factory = () => {
    return mount(MeasureDefinitionTabView, {
        props: {
            id: 1,
            ruleVersion: 1,
            clone: false
        },

        global: {
            directives: {
                tooltip() {}
            },
            stubs: {
                AutoComplete,
                Chip,
                Column,
                DataTable,
                Dialog,
                Dropdown,
                MetadataCard: true,
                Listbox,
                QueryCard: true,
                VCodeMirror,
                CodeMirror,
                TabView,
                TabPanel
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

jest.mock('axios')

axios.get.mockImplementation(() => Promise.resolve({ data: mockedRule }))

afterEach(() => {
    jest.clearAllMocks()
})

describe('Metadata Definition Tab View', () => {
    it('test', async () => {
        const wrapper = factory()

        console.log(wrapper.html())
    })
})
