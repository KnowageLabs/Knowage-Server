import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import Card from 'primevue/card'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import DatasetManagementTypeCard from './DatasetManagementTypeCard.vue'
import ProgressBar from 'primevue/progressbar'
import Dropdown from 'primevue/dropdown'
import ParamTable from './tables/DatasetManagementParamTable.vue'
import FileDataset from './fileDataset/DatasetManagementFileDataset.vue'
import JavaDataset from './javaDataset/DatasetManagementJavaDataset.vue'
import QbeDataset from './qbeDataset/DatasetManagementQbeDataset.vue'
import FlatDataset from './flatDataset/DatasetManagementFlatDataset.vue'
import CkanDataset from './ckanDataset/DatasetManagementCkanDataset.vue'
import RestDataset from './restDataset/DatasetManagementRestDataset.vue'
import SolrDataset from './solrDataset/DatasetManagementSolrDataset.vue'

const $store = {
    commit: jest.fn()
}

const $router = {
    replace: jest.fn()
}

const factory = (parentValid, selectedDataset, datasetTypes, dataSources, businessModels, scriptTypes, pythonEnvironments, rEnvironments) => {
    return mount(DatasetManagementTypeCard, {
        props: { parentValid, selectedDataset, datasetTypes, dataSources, businessModels, scriptTypes, pythonEnvironments, rEnvironments },
        global: {
            stubs: { Button, Card, KnValidationMessages, ProgressBar, Dropdown, ParamTable, CkanDataset, QbeDataset, RestDataset, JavaDataset, FlatDataset, SolrDataset, FileDataset },
            mocks: {
                $t: (msg) => msg,
                $store,
                $router
            }
        }
    })
}

describe('can not mount because of Code Mirror', () => {
    it('should change the type tab wizard if type changes', () => {
        const wrapper = factory()

        console.log(wrapper.html())
    })
})
