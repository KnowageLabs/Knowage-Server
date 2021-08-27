import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import axios from 'axios'
import GlossaryUsageDetail from './GlossaryUsageDetail.vue'
import flushPromises from 'flush-promises'
import ProgressBar from 'primevue/progressbar'

const mockedGlossaryList = [
    { GLOSSARY_ID: 45, GLOSSARY_NM: 'MARKET ANALYSIS' },
    { GLOSSARY_ID: 46, GLOSSARY_NM: 'MOCK GLOSSARY' }
]

const mockedContent = [
    { CONTENT_ID: 199, CONTENT_NM: 'PRODUCTS', HAVE_WORD_CHILD: true, HAVE_CONTENTS_CHILD: false },
    { CONTENT_ID: 198, CONTENT_NM: 'STORE', HAVE_WORD_CHILD: true, HAVE_CONTENTS_CHILD: false },
    { CONTENT_ID: 200, CONTENT_NM: 'UNITS', HAVE_WORD_CHILD: true, HAVE_CONTENTS_CHILD: false }
]

const mockedSearchContent = {
    GlossSearch: {
        GLOSSARY_ID: 45,
        GLOSSARY_NM: 'MARKET ANALYSIS',
        SBI_GL_CONTENTS: [{ CONTENT_ID: 199, CONTENT_NM: 'PRODUCTS', HAVE_WORD_CHILD: true, HAVE_CONTENTS_CHILD: false, CHILD: [{ WORD_ID: 262, WORD: 'Customer' }] }]
    },
    Status: 'OK'
}

jest.mock('axios')

axios.get.mockImplementation((url) => {
    switch (url) {
        case process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/documents/mockedDocument':
            return Promise.resolve({ data: mockedGlossaryList })
        case process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/documents/1/roles':
            return Promise.resolve({ data: mockedContent })
        default:
            return Promise.resolve({ data: [] })
    }
})
axios.post.mockImplementation(() => Promise.resolve({ data: [] }))

afterEach(() => {
    jest.clearAllMocks()
})

const factory = () => {
    return mount(GlossaryUsageDetail, {
        global: {
            stubs: { GlossaryUsageNavigationCard: true, GlossaryUsageLinkCard: true, ProgressBar },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Glossary Usage Detail', () => {
    it('placeholder', () => {
        const wrapper = factory()

        console.log(wrapper.html())
    })
})
