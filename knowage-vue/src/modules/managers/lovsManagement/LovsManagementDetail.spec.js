import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import flushPromises from 'flush-promises'
import LovsManagementDetail from './LovsManagementDetail.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedLov = {
    id: 601,
    name: 'DEMO_BRAND_NAME',
    description: '',
    label: 'DEMO_BRAND_NAME',
    lovProvider:
        '<QUERY><CONNECTION>Foodmart</CONNECTION><STMT>select distinct brand_name from product\nwhere brand_name in (&apos;Colony&apos;,&apos;Fantastic&apos;,&apos;Great&apos;,&apos;Model&apos;,&apos;Sphinx&apos;)</STMT><VISIBLE-COLUMNS>brand_name</VISIBLE-COLUMNS><INVISIBLE-COLUMNS></INVISIBLE-COLUMNS><LOVTYPE>simple</LOVTYPE><VALUE-COLUMN>brand_name</VALUE-COLUMN><DESCRIPTION-COLUMN>brand_name</DESCRIPTION-COLUMN></QUERY>',
    itypeCd: 'QUERY',
    itypeId: '1',
    lovProviderJSON:
        '{"QUERY" : {"CONNECTION" : "Foodmart", "STMT" : "c2VsZWN0IGRpc3RpbmN0IGJyYW5kX25hbWUgZnJvbSBwcm9kdWN0CndoZXJlIGJyYW5kX25hbWUgaW4gKCZhcG9zO0NvbG9ueSZhcG9zOywmYXBvcztGYW50YXN0aWMmYXBvczssJmFwb3M7R3JlYXQmYXBvczssJmFwb3M7TW9kZWwmYXBvczssJmFwb3M7U3BoaW54JmFwb3M7KQ==", "VISIBLE-COLUMNS" : "brand_name", "INVISIBLE-COLUMNS" : "", "LOVTYPE" : "simple", "VALUE-COLUMN" : "brand_name", "DESCRIPTION-COLUMN" : "brand_name"}}'
}

jest.mock('axios')

axios.get.mockImplementation(() => Promise.resolve({ data: mockedLov }))

const $confirm = {
    require: jest.fn()
}

const $store = {
    commit: jest.fn()
}

const $router = {
    push: jest.fn()
}

const factory = () => {
    return mount(LovsManagementDetail, {
        global: {
            stubs: {
                Button,
                Card,
                LovsManagementDetailCard: true,
                LovsManagementWizardCard: true,
                LovsManagementQuery: true,
                ProgressBar,
                Toolbar,
                VCodeMirror: true
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $router
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Lovs Management loading', () => {
    it('can not mount because of Code Mirror', () => {
        const wrapper = factory()

        console.log(wrapper.html())
    })
})
