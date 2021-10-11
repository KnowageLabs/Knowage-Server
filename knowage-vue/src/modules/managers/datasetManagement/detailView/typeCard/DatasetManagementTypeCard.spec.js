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

// const mockedDataset = {
//     id: 140,
//     label: 'lolo',
//     name: 'lolo',
//     description: 'lolo',
//     usedByNDocs: 0,
//     catTypeVn: null,
//     catTypeId: null,
//     pars: [],
//     meta: {
//         dataset: [
//             {
//                 pname: 'resultNumber',
//                 pvalue: '25'
//             }
//         ],
//         columns: [
//             {
//                 column: 'city',
//                 pname: 'Type',
//                 pvalue: 'java.lang.String'
//             },
//             {
//                 column: 'city',
//                 pname: 'fieldType',
//                 pvalue: 'ATTRIBUTE'
//             },
//             {
//                 column: 'city',
//                 pname: 'fieldAlias',
//                 pvalue: 'city'
//             }
//         ]
//     },
//     dsVersions: [],
//     dsTypeCd: 'File',
//     userIn: 'demo_admin',
//     versNum: 39,
//     dateIn: '2021-10-08T16:09:39.000+02:00',
//     fileName: 'lolo_38.csv',
//     fileType: 'CSV',
//     csvDelimiter: ',',
//     csvQuote: '"',
//     dateFormat: 'dd/MM/yyyy',
//     timestampFormat: 'dd/MM/yyyy HH:mm:ss',
//     csvEncoding: 'UTF-8',
//     skipRows: '0',
//     limitRows: '',
//     xslSheetNumber: '1',
//     trasfTypeCd: null,
//     pivotColName: null,
//     pivotColValue: null,
//     pivotRowName: null,
//     pivotIsNumRows: false,
//     isPersisted: true,
//     isPersistedHDFS: false,
//     persistTableName: 'test_schedule',
//     isScheduled: false,
//     startDate: null,
//     endDate: null,
//     schedulingCronLine: null,
//     isRealtime: false,
//     isIterable: false,
//     owner: 'demo_admin',
//     scopeCd: 'USER',
//     scopeId: 186,
//     tags: [],
//     canLoadData: true,
//     actions: [
//         {
//             name: 'detaildataset',
//             description: 'Dataset detail'
//         },
//         {
//             name: 'delete',
//             description: 'Delete dataset'
//         },
//         {
//             name: 'qbe',
//             description: 'Show Qbe'
//         }
//     ]
// }

// const mockedDatasetTypes = [
//     {
//         VALUE_NM: 'SbiFileDataSet',
//         VALUE_DS: 'SbiFileDataSet',
//         VALUE_ID: 142,
//         VALUE_CD: 'File'
//     },
//     {
//         VALUE_NM: 'SbiQueryDataSet',
//         VALUE_DS: 'SbiQueryDataSet',
//         VALUE_ID: 143,
//         VALUE_CD: 'Query'
//     },
//     {
//         VALUE_NM: 'SbiJClassDataSet',
//         VALUE_DS: 'SbiJClassDataSet',
//         VALUE_ID: 144,
//         VALUE_CD: 'Java Class'
//     },
//     {
//         VALUE_NM: 'SbiWSDataSet',
//         VALUE_DS: 'SbiWSDataSet',
//         VALUE_ID: 145,
//         VALUE_CD: 'Web Service'
//     },
//     {
//         VALUE_NM: 'SbiScriptDataSet',
//         VALUE_DS: 'SbiScriptDataSet',
//         VALUE_ID: 146,
//         VALUE_CD: 'Script'
//     },
//     {
//         VALUE_NM: 'SbiQbeDataSet',
//         VALUE_DS: 'SbiQbeDataSet',
//         VALUE_ID: 147,
//         VALUE_CD: 'Qbe'
//     },
//     {
//         VALUE_NM: 'SbiCustomDataSet',
//         VALUE_DS: 'SbiCustomDataSet',
//         VALUE_ID: 148,
//         VALUE_CD: 'Custom'
//     },
//     {
//         VALUE_NM: 'SbiFlatDataSet',
//         VALUE_DS: 'SbiFlatDataSet',
//         VALUE_ID: 149,
//         VALUE_CD: 'Flat'
//     },
//     {
//         VALUE_NM: 'SbiCkanDataSet',
//         VALUE_DS: 'SbiCkanDataSet',
//         VALUE_ID: 150,
//         VALUE_CD: 'Ckan'
//     },
//     {
//         VALUE_NM: 'SbiFederatedDataSet',
//         VALUE_DS: 'SbiFederatedDataSet',
//         VALUE_ID: 220,
//         VALUE_CD: 'Federated'
//     },
//     {
//         VALUE_NM: 'SbiRESTDataSet',
//         VALUE_DS: 'SbiRESTDataSet',
//         VALUE_ID: 224,
//         VALUE_CD: 'REST'
//     },
//     {
//         VALUE_NM: 'SbiSPARQLDataSet',
//         VALUE_DS: 'SbiSPARQLDataSet',
//         VALUE_ID: 431,
//         VALUE_CD: 'SPARQL'
//     },
//     {
//         VALUE_NM: 'SbiSolrDataSet',
//         VALUE_DS: 'SbiSolrDataSet',
//         VALUE_ID: 432,
//         VALUE_CD: 'Solr'
//     },
//     {
//         VALUE_NM: 'SbiPythonDataSet',
//         VALUE_DS: 'SbiPythonDataSet',
//         VALUE_ID: 444,
//         VALUE_CD: 'Python/R'
//     }
// ]

// const mockedDataSources = [
//     {
//         dsId: 1,
//         descr: 'Foodmart',
//         label: 'Foodmart',
//         jndi: 'java:comp/env/jdbc/foodmart',
//         urlConnection: '',
//         user: '',
//         driver: '',
//         dialectName: 'org.hibernate.dialect.MySQLInnoDBDialect',
//         hibDialectClass: 'org.hibernate.dialect.MySQLInnoDBDialect',
//         schemaAttribute: '',
//         multiSchema: false,
//         readOnly: false,
//         writeDefault: false,
//         owner: 'biadmin'
//     }
// ]

// const mockedbusinessModels = [
//     {
//         id: 140,
//         name: 'Sales',
//         description: 'Sales cube',
//         category: 280,
//         dataSourceLabel: 'Foodmart',
//         dataSourceId: 1,
//         modelLocked: false,
//         modelLocker: null,
//         smartView: true,
//         tablePrefixLike: null,
//         tablePrefixNotLike: null,
//         drivers: [],
//         metamodelDrivers: null
//     },
//     {
//         id: 142,
//         name: 'Inventory',
//         description: 'Inventory cube',
//         category: 168,
//         dataSourceLabel: 'Foodmart',
//         dataSourceId: 1,
//         modelLocked: false,
//         modelLocker: null,
//         smartView: true,
//         tablePrefixLike: null,
//         tablePrefixNotLike: null,
//         drivers: [],
//         metamodelDrivers: null
//     },
//     {
//         id: 153,
//         name: 'Expenses',
//         description: 'Expenses cube',
//         category: 169,
//         dataSourceLabel: 'Foodmart',
//         dataSourceId: 1,
//         modelLocked: false,
//         modelLocker: null,
//         smartView: true,
//         tablePrefixLike: null,
//         tablePrefixNotLike: null,
//         drivers: [],
//         metamodelDrivers: null
//     }
// ]

// const mockedScriptTypes = [
//     {
//         VALUE_NM: 'Groovy',
//         VALUE_DS: 'Script Type',
//         VALUE_ID: 140,
//         VALUE_CD: 'groovy'
//     },
//     {
//         VALUE_NM: 'Javascript',
//         VALUE_DS: 'Script Type',
//         VALUE_ID: 141,
//         VALUE_CD: 'ECMAScript'
//     }
// ]

// const mockedPython = [
//     {
//         id: 354,
//         label: 'python.virtualenv.0.backend.url',
//         name: 'Default python address',
//         description: 'Default IP address and port for python engine.',
//         valueCheck: 'localhost:5000',
//         valueTypeId: 30,
//         category: 'PYTHON_CONFIGURATION',
//         active: true
//     }
// ]

// const mockedR = [
//     {
//         id: 356,
//         label: 'r.env.0',
//         name: 'Default R address',
//         description: 'Default IP address and port for R engine.',
//         valueCheck: 'localhost:5001',
//         valueTypeId: 30,
//         category: 'R_CONFIGURATION',
//         active: true
//     }
// ]

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
