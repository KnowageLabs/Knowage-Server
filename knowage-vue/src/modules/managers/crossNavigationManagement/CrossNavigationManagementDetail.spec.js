import { mount } from '@vue/test-utils'
import axios from 'axios'
import flushPromises from 'flush-promises'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import CrossNavigationManagementDetail from './CrossNavigationManagementDetail.vue'
import Toolbar from 'primevue/toolbar'

const mockedInput={results:[
    {
        id: 7593,
        label: "Utente di inserimento",
        url: "utente",
        parType: "STRING"
    },
    {
        id: 7597,
        label: "Nota della richiesta",
        url: "nota",
        parType: "STRING"
    }
]}
const mockedParams=[
    {
        id: 7593,
        name: "Utente di inserimento",
        type: 1,
        parType: "STRING"
    },
    {
        id: 7597,
        name: "Nota della richiesta",
        type: 1,
        parType: "STRING"
    }
]
const mockedDoc={
    DOCUMENT_ID: 2329,
    DOCUMENT_LABEL: "BestProductSingPar",
    DOCUMENT_NAME: "BestProductSingPar",
    DOCUMENT_DESCR: "BestProduct single pameter - Composite Document",
    DOCUMENT_AUTH: "demoadmin"
}

jest.mock('axios')

axios.get.mockImplementation((url) => {
    switch (url) {
        case process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/documents/BestProductSingPar/parameters':
            return Promise.resolve({ data: mockedInput })
        default:
            return Promise.resolve({ data: [] })
    }
})

const $store = {
    commit: jest.fn()
}

const factory = () => {
    return mount(CrossNavigationManagementDetail, {
        global: {
            stubs: {
                Button,
                Card,
                Dropdown,
                InputText,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $store
            }
        }
    })
}

describe("Cross-navigation Management Detail", () => {
    it("save button is disabled if one of the mandatory input is invalid",() => {
        const formWrapper = factory()
        expect(formWrapper.vm.navigation.name).toStrictEqual(undefined)
        expect(formWrapper.vm.buttonDisabled).toBe(true)
        }
    );
    it("changes the output parameters list if origin document changes",async() => {
        const formWrapper = factory()
        formWrapper.vm.docType= 'origin',
        await flushPromises()
        await formWrapper.vm.hadleDoc(mockedDoc)
        expect(formWrapper.vm.navigation.fromPars).toStrictEqual(mockedParams)
    }
    );
    it("changes the input parameters list if target document changes", async () => {
        const formWrapper = factory()
        formWrapper.vm.docType= 'target',
        await flushPromises()
        await formWrapper.vm.hadleDoc(mockedDoc)
        expect(formWrapper.vm.navigation.toPars).toStrictEqual(mockedParams)
    }
    );
});