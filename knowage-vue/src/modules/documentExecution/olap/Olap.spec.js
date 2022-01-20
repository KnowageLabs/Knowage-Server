// import { mount, flushPromises } from '@vue/test-utils'
// import axios from 'axios'
// import Button from 'primevue/button'
// import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
// import Olap from './Olap.vue'
// import ProgressBar from 'primevue/progressbar'
// import TabView from 'primevue/tabview'
// import TabPanel from 'primevue/tabpanel'
// import Toolbar from 'primevue/toolbar'
// import Tooltip from 'primevue/tooltip'

// jest.mock('axios')

// const mockedOlap = {
//     table: '',
//     rows: [],
//     columns: [],
//     filters: [],
//     formulas: [],
//     CALCULATED_FIELDS: [],
//     columnsAxisOrdinal: 0,
//     rowsAxisOrdinal: 1,
//     modelConfig: {
//         drillType: '',
//         showParentMembers: false,
//         hideSpans: false,
//         showProperties: false,
//         showCompactProperties: false,
//         suppressEmpty: false,
//         enableDrillThrough: false,
//         sortingEnabled: false,
//         axisToSort: 0,
//         axis: 0,
//         sortingPositionUniqueName: null,
//         sortMode: null,
//         topBottomCount: 0,
//         startRow: 0,
//         rowsSet: 50,
//         rowCount: 1,
//         startColumn: 0,
//         columnSet: 50,
//         columnCount: 1,
//         pageSize: 0,
//         pagination: false,
//         actualVersion: null,
//         artifactId: 25,
//         status: null,
//         locker: null,
//         toolbarVisibleButtons: [
//             'BUTTON_DRILL_THROUGH',
//             'BUTTON_MDX',
//             'BUTTON_EDIT_MDX',
//             'BUTTON_FATHER_MEMBERS',
//             'BUTTON_CC',
//             'BUTTON_HIDE_SPANS',
//             'BUTTON_SORTING_SETTINGS',
//             'BUTTON_SORTING',
//             'BUTTON_SHOW_PROPERTIES',
//             'BUTTON_HIDE_EMPTY',
//             'BUTTON_SAVE_SUBOBJECT',
//             'BUTTON_FLUSH_CACHE',
//             'BUTTON_SAVE_NEW',
//             'BUTTON_UNDO',
//             'BUTTON_VERSION_MANAGER',
//             'BUTTON_EXPORT_OUTPUT',
//             'BUTTON_EDITABLE_EXCEL_EXPORT',
//             'BUTTON_CROSS_NAVIGATION'
//         ],
//         toolbarMenuButtons: [],
//         toolbarClickedButtons: [],
//         dimensionHierarchyMap: {},
//         crossNavigation: {
//             parameters: [],
//             buttonClicked: false,
//             modelStatus: null
//         },
//         writeBackConf: null,
//         whatIfScenario: false
//     },
//     mdxFormatted: 'SELECT * FROM table',
//     MDXWITHOUTCF: 'SELECT * FROM table',
//     hasPendingTransformations: false
// }

// const mockedFilter = {
//     name: 'Customers',
//     uniqueName: '[Customers]',
//     caption: 'Customers',
//     hierarchies: [
//         {
//             name: 'Customers.All Customers',
//             uniqueName: '[Customers.All Customers]',
//             caption: 'All Customers',
//             position: 0,
//             slicers: [],
//             levelNames: ['(All)', 'Country', 'State Province', 'City', 'Name']
//         },
//         {
//             name: 'Customers.Customer by segment',
//             uniqueName: '[Customers.Customer by segment]',
//             caption: 'Customer by segment',
//             position: 0,
//             slicers: [],
//             levelNames: ['(All)', 'Occupation', 'Education', 'Yearly income']
//         }
//     ],
//     selectedHierarchyUniqueName: '[Customers.All Customers]',
//     selectedHierarchyPosition: 0,
//     axis: 1,
//     measure: 0,
//     positionInAxis: 0
// }

// const $http = {
//     get: axios.get.mockImplementation((url) => {
//         switch (url) {
//             case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `olap/designer/1`:
//                 return Promise.resolve({ data: [] })
//             default:
//                 return Promise.resolve({ data: [] })
//         }
//     }),

//     post: axios.post.mockImplementation((url) => {
//         switch (url) {
//             case process.env.VUE_APP_OLAP_PATH + `1.0/model/?SBI_EXECUTION_ID=1`:
//                 return Promise.resolve({ data: mockedOlap })
//             default:
//                 return Promise.resolve({ data: [] })
//         }
//     })
// }

// const $route = { name: '' }

// const factory = () => {
//     return mount(Olap, {
//         props: {
//             id: '1',
//             olapId: '1'
//         },
//         global: {
//             directives: {
//                 tooltip() {}
//             },
//             stubs: {
//                 Button,
//                 FilterPanel: true,
//                 FilterTopToolbar: true,
//                 FilterLeftToolbar: true,
//                 KnOverlaySpinnerPanel,
//                 MultiHierarchyDialog: true,
//                 OlapSidebar: true,
//                 OlapSortingDialog: true,
//                 OlapCustomViewTable: true,
//                 OlapCustomViewSaveDialog: true,
//                 OlapMDXQueryDialog: true,
//                 OlapCrossNavigationDefinitionDialog: true,
//                 OlapButtonWizardDialog: true,
//                 ProgressBar,
//                 TabView,
//                 TabPanel,
//                 Toolbar,
//                 Tooltip
//             },
//             mocks: {
//                 $t: (msg) => msg,
//                 $http,
//                 $route
//             }
//         }
//     })
// }

// afterEach(() => {
//     jest.clearAllMocks()
// })

// describe('Olap', () => {
//     it('should set drillup/drilldown/drillthrough on different member settings', async () => {
//         const wrapper = factory()

//         await flushPromises()
//         wrapper.vm.onDrillTypeChanged('position')
//         expect(axios.post).toBeCalledTimes(2)
//         expect(axios.post).toHaveBeenNthCalledWith(
//             2,
//             '/knowagewhatifengine/restful-services/1.0/modelconfig?SBI_EXECUTION_ID=1&NOLOADING=undefined',
//             { ...mockedOlap.modelConfig, drillType: 'position' },
//             { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } }
//         )

//         wrapper.vm.onDrillTypeChanged('member')
//         expect(axios.post).toBeCalledTimes(3)
//         expect(axios.post).toHaveBeenNthCalledWith(
//             3,
//             '/knowagewhatifengine/restful-services/1.0/modelconfig?SBI_EXECUTION_ID=1&NOLOADING=undefined',
//             { ...mockedOlap.modelConfig, drillType: 'member' },
//             { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } }
//         )

//         wrapper.vm.onDrillTypeChanged('replace')
//         expect(axios.post).toBeCalledTimes(4)
//         expect(axios.post).toHaveBeenNthCalledWith(
//             4,
//             '/knowagewhatifengine/restful-services/1.0/modelconfig?SBI_EXECUTION_ID=1&NOLOADING=undefined',
//             { ...mockedOlap.modelConfig, drillType: 'replace' },
//             { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } }
//         )
//     })

//     it('should sort the table if sort is enabled and a sorting column is clicked', async () => {
//         const wrapper = factory()

//         await flushPromises()
//         wrapper.vm.sorting = 'no sorting'
//         wrapper.vm.onSortingSelect({ sortingMode: 'breaking', sortingCOunt: 10 })
//         expect(axios.get).toBeCalledTimes(3)
//         expect(axios.get).toHaveBeenNthCalledWith(3, '/knowagewhatifengine/restful-services/1.0/member/sort/disable?SBI_EXECUTION_ID=1', { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
//     })

//     it('should swap axis content if swap button is clicked', async () => {
//         const wrapper = factory()

//         await flushPromises()
//         wrapper.vm.moveHierarchies(mockedFilter)
//         expect(axios.post).toBeCalledTimes(2)
//         expect(axios.post).toHaveBeenNthCalledWith(
//             2,
//             '/knowagewhatifengine/restful-services/1.0/axis/moveHierarchy?SBI_EXECUTION_ID=1',
//             {
//                 axis: mockedFilter.axis,
//                 hierarchy: mockedFilter.selectedHierarchyUniqueName,
//                 newPosition: mockedFilter.positionInAxis + 1,
//                 direction: 1
//             },
//             { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } }
//         )
//     })

//     it('should invert axis dimensions if invert button is clicked', async () => {
//         const wrapper = factory()

//         await flushPromises()

//         wrapper.vm.swapAxis()
//         expect(axios.post).toBeCalledTimes(2)
//         expect(axios.post).toHaveBeenNthCalledWith(2, '/knowagewhatifengine/restful-services/1.0/axis/swap?SBI_EXECUTION_ID=1', null, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
//     })

//     it('should insert/remove a dimension from the table if a dimension is dragged in the axis', async () => {
//         const wrapper = factory()

//         await flushPromises()

//         wrapper.vm.putFilterOnAxis(0, mockedFilter)
//         expect(axios.post).toBeCalledTimes(2)
//         expect(axios.post).toHaveBeenNthCalledWith(
//             2,
//             '/knowagewhatifengine/restful-services/1.0/axis/moveDimensionToOtherAxis?SBI_EXECUTION_ID=1',
//             { fromAxis: 0, hierarchy: mockedFilter.selectedHierarchyUniqueName, toAxis: mockedFilter.axis },
//             { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } }
//         )
//     })
// })

// describe('OLAP Designer', () => {
//     it('should show empty rows if the button empty rows is not set', async () => {
//         const wrapper = factory()

//         await flushPromises()
//         expect(wrapper.vm.olap.modelConfig.showParentMembers).toBe(false)
//         wrapper.vm.onShowParentMemberChanged(true)
//         expect(axios.post).toBeCalledTimes(2)
//         expect(axios.post).toHaveBeenNthCalledWith(
//             2,
//             '/knowagewhatifengine/restful-services/1.0/modelconfig?SBI_EXECUTION_ID=1&NOLOADING=undefined',
//             { ...mockedOlap.modelConfig, showParentMembers: true },
//             { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } }
//         )
//     })

//     it('should hide empty rows if the button empty rows is set', async () => {
//         mockedOlap.modelConfig.showParentMembers = true

//         const wrapper = factory()

//         await flushPromises()
//         expect(wrapper.vm.olap.modelConfig.showParentMembers).toBe(true)
//         wrapper.vm.onShowParentMemberChanged(false)
//         expect(axios.post).toBeCalledTimes(2)
//         expect(axios.post).toHaveBeenNthCalledWith(
//             2,
//             '/knowagewhatifengine/restful-services/1.0/modelconfig?SBI_EXECUTION_ID=1&NOLOADING=undefined',
//             { ...mockedOlap.modelConfig, showParentMembers: false },
//             { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } }
//         )
//     })
// })
