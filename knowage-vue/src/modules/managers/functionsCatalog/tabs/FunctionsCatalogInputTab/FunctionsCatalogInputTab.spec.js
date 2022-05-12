import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import FunctionsCatalogInputTab from './FunctionsCatalogInputTab.vue'
import FunctionsCatalogInputColumnsCard from './FunctionsCatalogInputColumnsCard.vue'
import FunctionsCatalogInputVariablesCard from './FunctionsCatalogInputVariablesCard.vue'
import Toolbar from 'primevue/toolbar'

const mockedFunction = {
    id: '599b009b-707e-4033-ad1c-02f6bec275e7',
    name: 'Toy function',
    description: 'This is a test function. It computes the family size given number of children, pets, and a scale factor. It also takes the gender as a char and return the complete string.',
    benchmarks: '',
    language: 'Python',
    family: 'online',
    onlineScript: '# save copy of inputs (inputs are read only)\npets = ${pets}\ngender = ${gender}\n# business logic\nscaled_pets = pets.apply(lambda x: x*${scale_factor})\n# fill outputs\n${family_size} = ${children} + scaled_pets\n${gender_str} = gender.replace({"M": "Male", "F": "Female"})',
    offlineScriptTrain: '',
    offlineScriptUse: '',
    owner: 'demo_admin',
    label: 'toy_dataset_function',
    type: 'Machine Learning',
    keywords: [],
    inputVariables: [
        {
            name: 'scale_factor',
            type: 'NUMBER',
            value: '0.5'
        }
    ],
    inputColumns: [
        {
            name: 'gender',
            type: 'STRING'
        },
        {
            name: 'children',
            type: 'NUMBER'
        },
        {
            name: 'pets',
            type: 'NUMBER'
        }
    ],
    outputColumns: [
        {
            name: 'family_size',
            fieldType: 'MEASURE',
            type: 'NUMBER'
        },
        {
            name: 'gender_str',
            fieldType: 'ATTRIBUTE',
            type: 'STRING'
        }
    ]
}

const factory = () => {
    return mount(FunctionsCatalogInputTab, {
        props: {
            propFunction: mockedFunction,
            readonly: false
        },
        global: {
            directives: {
                tooltip() {}
            },
            stubs: {
                Button,
                Calendar,
                Dropdown,
                InputText,
                FunctionsCatalogInputColumnsCard,
                FunctionsCatalogInputVariablesCard,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Functions Catalog Input Tab', () => {
    it('should have two different sections in the input tab, one for columns and one for variables', () => {
        const wrapper = factory()

        expect(wrapper.vm.selectedFunction).toStrictEqual(mockedFunction)
        expect(wrapper.find('[data-test="input-columns-container"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="input-variables-container"]').exists()).toBe(true)

        expect(wrapper.find('[data-test="column-name-input"]').wrapperElement._value).toBe('gender')
        expect(wrapper.find('[data-test="variable-name-input"]').wrapperElement._value).toBe('scale_factor')
    })
})
