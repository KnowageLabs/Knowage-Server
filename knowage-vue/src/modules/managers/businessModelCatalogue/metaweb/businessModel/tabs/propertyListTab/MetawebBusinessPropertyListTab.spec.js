import { mount } from '@vue/test-utils'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import MetawebBusinessPropertyListTab from './MetawebBusinessPropertyListTab.vue'

const mockedBusinessModel = {
    name: 'Product class',
    uniqueName: 'product_class',
    properties: [
        {
            'structural.visible': {
                propertyType: {
                    id: 'structural.visible',
                    name: 'Visible',
                    description: 'Specify if this column is visible in the query editor',
                    admissibleValues: ['true', 'false'],
                    defaultValue: 'true'
                },
                value: 'true'
            }
        },
        {
            'structural.tabletype': {
                propertyType: {
                    id: 'structural.tabletype',
                    name: 'Type',
                    description: 'The role played by this table in the data model (generic, cube or dimension)',
                    admissibleValues: ['generic', 'cube', 'dimension', 'geographic dimension'],
                    defaultValue: 'generic'
                },
                value: 'dimension'
            }
        }
    ],
    columns: [],
    calculatedBusinessColumns: [],
    relationships: [],
    simpleBusinessColumns: [],
    physicalTable: {
        physicalTableIndex: 13
    }
}

const factory = () => {
    return mount(MetawebBusinessPropertyListTab, {
        props: { selectedBusinessModel: mockedBusinessModel },
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Accordion,
                AccordionTab,
                Button,
                InputText,
                'router-view': true
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Metaweb Business Property List Tab', () => {
    it('the detail field of a business model should show the name of the selected business model in read-only mode', async () => {
        const wrapper = factory()

        expect(wrapper.vm.businessModel).toStrictEqual(mockedBusinessModel)

        expect(wrapper.find('[data-test="input-name"]').wrapperElement._value).toBe('Product class')
        expect(wrapper.find('[data-test="input-name"]').element.disabled).toBe(true)
    })
})
