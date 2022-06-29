<template>
    <div class="p-d-flex p-flex-column kn-flex kn-height-full">
        <ProgressSpinner class="kn-progress-spinner" v-if="loading" />

        <OlapCustomViewTable v-if="customViewVisible" class="olap-overlay-dialog" :olapCustomViews="olapCustomViews" @close="$emit('closeOlapCustomView')" @applyCustomView="$emit('applyCustomView', $event)" />

        <DrillTruDialog
            v-if="drillTruDialogVisible"
            :drillData="dtData"
            :tableColumns="formattedColumns"
            :dtLevels="dtAssociatedLevels"
            :menuTree="dtTree"
            :dtMaxRows="dtMaxRows"
            class="olap-overlay-dialog"
            @close="closeDrillTruDialog"
            @checkCheckboxes="checkCheckboxes"
            @clearLevels="dtAssociatedLevels.length = 0"
            @rowsChanged="dtMaxRows = $event"
            @drill="drillThrough"
        />

        <FilterPanel :olapProp="olap" :olapDesigner="olapDesigner" @putFilterOnAxis="putFilterOnAxis" @showMultiHierarchy="showMultiHierarchy" @openFilterDialog="openFilterDialog" />
        <FilterTopToolbar :olapProp="olap" @openSidebar="olapSidebarVisible = true" @putFilterOnAxis="putFilterOnAxis" @swapAxis="swapAxis" @switchPosition="moveHierarchies" @showMultiHierarchy="showMultiHierarchy" @openFilterDialog="openFilterDialog" />

        <div id="left-and-table-container" class="p-d-flex p-flex-row kn-flex">
            <FilterLeftToolbar :olapProp="olap" @openSidebar="olapSidebarVisible = true" @putFilterOnAxis="putFilterOnAxis" @switchPosition="moveHierarchies" @showMultiHierarchy="showMultiHierarchy" @openFilterDialog="openFilterDialog" />
            <div id="table-container" class="kn-flex" :style="olapDescriptor.style.tableContainer">
                <div id="olap-table" class="kn-flex kn-olap-table" ref="olap-table" v-html="olap.table" @click="handleTableClick" @dblclick="handleTableDoubleClick"></div>
            </div>
        </div>

        <!-- SELECT TOAST CONFIRM -->
        <div v-if="mode === 'From Cell' || mode === 'From Member'" id="custom-toast" :style="olapDescriptor.style.customToastContainer">
            <div id="custom-toast-content" :style="olapDescriptor.style.customToastContent">
                <div class="p-d-flex p-flex-column">
                    <div class="p-text-center p-d-flex p-flex-row p-ai-center">
                        <i class="pi pi-info-circle p-ml-2" :style="olapDescriptor.style.toastIcon"></i>
                        <h4 class="p-ml-2">{{ $t('documentExecution.olap.crossNavigationDefinition.finishSelection') }}</h4>
                        <Button class="p-jc-center" :style="olapDescriptor.style.toastButton" label="OK" @click="cellSelected" />
                    </div>
                </div>
            </div>
        </div>

        <!-- SIDEBAR -->
        <div v-if="olapSidebarVisible" id="olap-backdrop" @click="olapSidebarVisible = false" />
        <OlapSidebar
            v-if="olapSidebarVisible"
            class="olap-sidebar kn-overflow-y"
            :olap="olap"
            :olapDesignerMode="olapDesignerMode"
            :propButtons="buttons"
            :whatIfMode="whatIfMode"
            :olapHasScenario="olapHasScenario"
            @openCustomViewDialog="customViewSaveDialogVisible = true"
            @drillTypeChanged="onDrillTypeChanged"
            @showParentMemberChanged="onShowParentMemberChanged"
            @hideSpansChanged="onHideSpansChanged"
            @suppressEmptyChanged="onSuppressEmptyChanged"
            @drillThroughChanged="onDrillThroughChanged"
            @showPropertiesChanged="onShowPropertiesChanged"
            @openSortingDialog="sortingDialogVisible = true"
            @openMdxQueryDialog="mdxQueryDialogVisible = true"
            @reloadSchema="reloadOlap"
            @enableCrossNavigation="enableCrossNaivigation"
            @openCrossNavigationDefinitionDialog="crossNavigationDefinitionDialogVisible = true"
            @openButtonWizardDialog="buttonsWizardDialogVisible = true"
            @saveOlapDesigner="saveOlapDesigner"
            @showOutputWizard="outputWizardVisible = true"
            @showScenarioWizard="scenarioWizardVisible = true"
            @showSaveAsNewVersion="saveVersionDialogVisible = true"
            @showAlgorithmDialog="algorithmDialogVisible = true"
            @undo="undo"
            @showDeleteVersions="deleteVersionDialogVisible = true"
            @exportExcel="exportExcel"
            @loading="loading = $event"
        />
    </div>

    <div id="whatif-input" ref="whatifInput" class="p-inputgroup">
        <InputText v-model="whatifInputNewValue" @keyup.enter="onWhatifInput" />
        <InputText v-model="whatifInputOldValue" :disabled="true" />
        <Button icon="pi pi-times" class="kn-button--secondary" @click="closeWhatifInput" />
    </div>

    <!-- DIALOGS -->
    <OlapCustomViewSaveDialog :visible="customViewSaveDialogVisible" :sbiExecutionId="id" @close="customViewSaveDialogVisible = false"></OlapCustomViewSaveDialog>
    <OlapSortingDialog :visible="sortingDialogVisible" :olap="olap" @save="onSortingSelect"></OlapSortingDialog>
    <OlapMDXQueryDialog :visible="mdxQueryDialogVisible" :mdxQuery="olap?.MDXWITHOUTCF" @close="mdxQueryDialogVisible = false"></OlapMDXQueryDialog>
    <OlapCrossNavigationDefinitionDialog :visible="crossNavigationDefinitionDialogVisible" :propOlapDesigner="olapDesigner" :selectedCell="selectedCell" @close="crossNavigationDefinitionDialogVisible = false" @selectFromTable="enterSelectMode($event)"></OlapCrossNavigationDefinitionDialog>
    <OlapButtonWizardDialog :visible="buttonsWizardDialogVisible" :propButtons="buttons" :propOlapDesigner="olapDesigner" @close="buttonsWizardDialogVisible = false"></OlapButtonWizardDialog>
    <MultiHierarchyDialog :selectedFilter="multiHierFilter" :multiHierUN="selecetedMultiHierUN" :visible="multiHierarchyDialogVisible" @setMultiHierUN="setMultiHierUN" @updateHierarchy="updateHierarchy" @close="multiHierarchyDialogVisible = false" />
    <KnOverlaySpinnerPanel :visibility="loading" />
    <OutputWizard v-if="outputWizardVisible" :visible="outputWizardVisible" :olapVersionsProp="olapVersions" :sbiExecutionId="id" @close="outputWizardVisible = false" />
    <ScenarioWizard v-if="scenarioWizardVisible" :visible="scenarioWizardVisible" :hiddenFormDataProp="hiddenFormDataProp" :sbiExecutionId="id" :olapDesignerProp="olapDesigner" @saveScenario="saveScenario" @deleteScenario="deleteScenario" @close="scenarioWizardVisible = false" />
    <AlgorithmDialog v-if="algorithmDialogVisible" :visible="algorithmDialogVisible" :sbiExecutionId="id" @close="algorithmDialogVisible = false" />
    <OlapFilterDialog :visible="filterDialogVisible" :propFilter="selectedFilter" :id="id" :olapDesignerMode="olapDesignerMode" :parameters="parameters" :profileAttributes="profileAttributes" :olapDesigner="olapDesigner" @close="closeFilterDialog" @applyFilters="applyFilters"></OlapFilterDialog>
    <OlapSaveNewVersionDialog :visible="saveVersionDialogVisible" :id="id" @close="saveVersionDialogVisible = false" @newVersionSaved="onNewVersionSaved"></OlapSaveNewVersionDialog>
    <OlapDeleteVersionsDialog :visible="deleteVersionDialogVisible" :id="id" :propOlapVersions="olapVersions" :olap="olap" @close="deleteVersionDialogVisible = false"></OlapDeleteVersionsDialog>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import { iOlapCustomView, iButton, iOlapFilter, iOlap, iParameter, iProfileAttribute } from './Olap'
import { downloadDirect } from '@/helpers/commons/fileHelper'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
import olapDescriptor from './OlapDescriptor.json'
import OlapSidebar from './olapSidebar/OlapSidebar.vue'
import OlapSortingDialog from './sortingDialog/OlapSortingDialog.vue'
import OlapCustomViewTable from './customView/OlapCustomViewTable.vue'
import OlapCustomViewSaveDialog from './customViewSaveDialog/OlapCustomViewSaveDialog.vue'
import OlapMDXQueryDialog from './mdxQueryDialog/OlapMDXQueryDialog.vue'
import FilterPanel from './filterPanel/OlapFilterPanel.vue'
import FilterTopToolbar from './filterToolbar/OlapTopFilterToolbar.vue'
import FilterLeftToolbar from './filterToolbar/OlapLeftFilterToolbar.vue'
import OlapCrossNavigationDefinitionDialog from './crossNavigationDefinition/OlapCrossNavigationDefinitionDialog.vue'
import OlapButtonWizardDialog from './buttonWizard/OlapButtonWizardDialog.vue'
import MultiHierarchyDialog from './multiHierarchyDialog/OlapMultiHierarchyDialog.vue'
import DrillTruDialog from './drillThroughDialog/OlapDrillThroughDialog.vue'
import OutputWizard from './outputWizard/OlapOutputWizard.vue'
import ScenarioWizard from './scenarioWizard/OlapScenarioWizard.vue'
import OlapFilterDialog from './filterDialog/OlapFilterDialog.vue'
import OlapSaveNewVersionDialog from './newVersionDialog/OlapSaveNewVersionDialog.vue'
import AlgorithmDialog from './algorithmDialog/OlapAlgorithmDialog.vue'
import OlapDeleteVersionsDialog from './deleteVersionsDialog/OlapDeleteVersionsDialog.vue'

export default defineComponent({
    name: 'olap',
    components: {
        OutputWizard,
        OlapSidebar,
        DrillTruDialog,
        OlapCustomViewTable,
        OlapCustomViewSaveDialog,
        KnOverlaySpinnerPanel,
        OlapSortingDialog,
        FilterPanel,
        FilterTopToolbar,
        FilterLeftToolbar,
        OlapMDXQueryDialog,
        OlapCrossNavigationDefinitionDialog,
        OlapButtonWizardDialog,
        MultiHierarchyDialog,
        OlapFilterDialog,
        ScenarioWizard,
        OlapSaveNewVersionDialog,
        AlgorithmDialog,
        OlapDeleteVersionsDialog
    },
    props: { id: { type: String }, olapId: { type: String }, olapName: { type: String }, reloadTrigger: { type: Boolean }, olapCustomViewVisible: { type: Boolean }, hiddenFormDataProp: { type: Object, required: true } },
    emits: ['closeOlapCustomView', 'applyCustomView', 'executeCrossNavigation'],
    data() {
        return {
            olapDescriptor,
            documentId: null as any,
            documentName: null as any,
            olap: {} as iOlap,
            olapSidebarVisible: false,
            customViewVisible: false,
            olapCustomViews: [] as iOlapCustomView[],
            customViewSaveDialogVisible: false,
            sortingDialogVisible: false,
            mdxQueryDialogVisible: false,
            crossNavigationDefinitionDialogVisible: false,
            buttonsWizardDialogVisible: false,
            multiHierarchyDialogVisible: false,
            drillTruDialogVisible: false,
            outputWizardVisible: false,
            scenarioWizardVisible: false,
            algorithmDialogVisible: false,
            multiHierFilter: {} as iOlapFilter,
            selecetedMultiHierUN: '',
            sort: null as any,
            mode: 'view',
            selectedCell: null as any,
            buttons: [] as iButton[],
            olapDesigner: null as any,
            olapDesignerMode: false,
            loading: false,
            dtData: [] as any,
            dtColumns: [] as any,
            formattedColumns: [] as any,
            dtAssociatedLevels: [] as any,
            dtTree: [] as any,
            olapVersions: [] as any,
            dtMaxRows: 0,
            usedOrdinal: 0 as Number,
            selectedFilter: null as any,
            filterDialogVisible: false,
            parameters: [] as iParameter[],
            profileAttributes: [] as iProfileAttribute[],
            saveVersionDialogVisible: false,
            deleteVersionDialogVisible: false,
            whatIfMode: false,
            whatifInputNewValue: 0 as Number,
            whatifInputOldValue: 0 as Number,
            whatifInputOrdinal: 0 as Number,
            noTemplate: '' as string,
            reference: '' as string,
            documentLabel: null as any
        }
    },
    async created() {
        this.documentId = this.olapId
        this.documentName = this.olapName

        if (this.$route.name === 'olap-designer') {
            this.olapDesignerMode = true
        }
        await this.loadPage()
    },
    computed: {
        olapHasScenario() {
            if (this.olapDesigner?.template?.wrappedObject?.olap?.SCENARIO) {
                return true
            } else return false
        }
    },
    watch: {
        async id() {
            await this.loadPage()
        },
        async reloadTrigger() {
            await this.loadPage()
        },
        olapCustomViewVisible() {
            this.loadCustomView()
        }
    },
    methods: {
        async loadPage() {
            this.loading = true
            if (this.$route.name === 'olap-designer') {
                this.documentId = this.$route.query.olapId
                this.documentName = this.$route.query.olapName
                this.documentLabel = this.$route.query.olapLabel
            }
            await this.loadOlapModel()
            this.loadCustomView()
            this.loading = false
        },
        async loadOlapDesigner() {
            console.log(' >>> CALLED OLAP DESIGNER: ')
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `olap/designer/${this.documentId}`, { headers: { Accept: 'application/json, text/plain, */*' } })
                .then(async (response: AxiosResponse<any>) => {
                    this.olapDesigner = response.data
                    this.whatIfMode = this.olapDesigner?.ENGINE === 'knowagewhatifengine'
                })
                .catch(() => {})
        },
        async loadCustomView() {
            this.customViewVisible = this.olapCustomViewVisible

            if (this.customViewVisible) {
                await this.loadOlapCustomViews()
            }
        },
        async loadOlapCustomViews() {
            this.loading = true
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `/1.0/olapsubobjects/getSubObjects?idObj=${this.documentId}`)
                .then(async (response: AxiosResponse<any>) => (this.olapCustomViews = response.data.results))
                .catch(() => {})
            this.loading = false
        },
        async loadOlapButtons() {
            this.loading = true
            await this.$http
                .get(process.env.VUE_APP_OLAP_PATH + `1.0/buttons`)
                .then(async (response: AxiosResponse<any>) => {
                    this.buttons = response.data
                    this.buttons.splice(
                        this.buttons.findIndex((item) => item.name === 'BUTTON_CC'),
                        1
                    )
                })
                .catch(() => {})
            this.loading = false
        },
        async loadOlapModel() {
            console.log('CAAAAAAAAAAAAAAAAAAAALING loadOlapModel()')
            this.noTemplate = this.$route.query.noTemplate as string
            this.reference = this.$route.query.reference as string

            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/model/?SBI_EXECUTION_ID=${this.id}`, null, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then(async (response: AxiosResponse<any>) => {
                    this.olap = response.data
                    // TODO
                    // await this.loadOlapDesigner()
                    console.log('ROUTE: ', this.$route)
                    console.log('noTemplate ', this.noTemplate)
                    console.log('reference: ', this.reference)
                    if (this.noTemplate === 'true') {
                        this.olapDesigner = {
                            template: {
                                wrappedObject: {
                                    olap: {
                                        cube: {
                                            reference: this.reference
                                        },
                                        MDXMondrianQuery: {
                                            XML_TAG_TEXT_CONTENT: this.olap.MDXWITHOUTCF
                                        },
                                        MDXQUERY: {
                                            XML_TAG_TEXT_CONTENT: this.olap.MDXWITHOUTCF,
                                            parameter: []
                                        },
                                        JSONTEMPLATE: {
                                            XML_TAG_TEXT_CONTENT: ''
                                        },
                                        calculated_fields: {
                                            calculated_field: []
                                        }
                                    }
                                }
                            }
                        }
                        this.olapDesigner.template.wrappedObject.olap.JSONTEMPLATE.XML_TAG_TEXT_CONTENT = JSON.stringify(this.olapDesigner.template.wrappedObject)
                    }

                    console.log('CREATED OLAP DESINGER: ', this.olapDesigner)

                    if (this.olapDesigner) {
                        await this.loadParameters()
                        await this.loadProfileAttributes()
                    }
                    await this.loadOlapButtons()
                    this.setClickedButtons()
                    await this.loadModelConfig()
                    await this.loadVersions()
                })
                .catch((error: any) => {
                    console.log('EEEEEEEEEEEEEEEEEEEEEEEEROR: ', error)
                })
            this.loading = false
        },
        setClickedButtons() {
            if (this.olapDesigner && this.olapDesigner.template?.wrappedObject?.olap?.TOOLBAR) {
                const toolbarButtonKeys = Object.keys(this.olapDesigner.template?.wrappedObject?.olap?.TOOLBAR)
                this.buttons.forEach((tempButton: iButton) => {
                    const index = toolbarButtonKeys.indexOf(tempButton.name)
                    if (index >= 0) {
                        tempButton.visible = this.olapDesigner.template.wrappedObject.olap.TOOLBAR[toolbarButtonKeys[index]].visible

                        tempButton.clicked = this.olapDesigner.template.wrappedObject.olap.TOOLBAR[toolbarButtonKeys[index]].clicked
                    }
                })

                this.olap.modelConfig.toolbarClickedButtons?.forEach((button: string) => {
                    switch (button) {
                        case 'BUTTON_DRILL_THROUGH':
                            this.olap.modelConfig.enableDrillThrough = true
                            break
                        case 'BUTTON_FATHER_MEMBERS':
                            this.olap.modelConfig.showParentMembers = true
                            break
                        case 'BUTTON_HIDE_SPANS':
                            this.olap.modelConfig.hideSpans = true
                            break
                        case 'BUTTON_SHOW_PROPERTIES':
                            this.olap.modelConfig.showProperties = true
                            break
                        case 'BUTTON_HIDE_EMPTY':
                            this.olap.modelConfig.suppressEmpty = true
                            break
                    }
                })
            }
        },
        async loadModelConfig() {
            console.log('loadModelConfig() CALLLLLLLLLLLLLED ')
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/modelconfig?SBI_EXECUTION_ID=${this.id}&NOLOADING=undefined`, this.olap.modelConfig, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then((response: AxiosResponse<any>) => (this.olap = response.data))
                .catch(() => {})

            this.formatOlapTable()
            this.loading = false
        },
        async loadVersions() {
            this.loading = true
            if (this.olapHasScenario) {
                await this.$http
                    .get(process.env.VUE_APP_OLAP_PATH + `1.0/version?SBI_EXECUTION_ID=${this.id}`)
                    .then((response: AxiosResponse<any>) => (this.olapVersions = response.data))
                    .catch(() => {})
            }

            this.loading = false
        },
        formatOlapTable() {
            if (this.olap?.table) {
                this.olap.table = this.olap.table.replaceAll('</drillup>', ' <div class="drill-up"></div></drillup> ')
                this.olap.table = this.olap.table.replaceAll('</drilldown>', '<div class="drill-down"></div> </drilldown> ')
                this.olap.table = this.olap.table.replaceAll('../../../../knowage/themes/commons/img/olap/nodrill.png', '')
                this.olap.table = this.olap.table.replaceAll('src="../../../../knowage/themes/commons/img/olap/arrow-up.png"', ' <div class="drill-up-replace"></div ')
                this.olap.table = this.olap.table.replaceAll('src="../../../../knowage/themes/commons/img/olap/noSortRows.png"', ' <div class="sort-basic"></div ')
                this.olap.table = this.olap.table.replaceAll('src="../../../../knowage/themes/commons/img/olap/ASC-rows.png"', ' <div class="sort-asc"></div ')
                this.olap.table = this.olap.table.replaceAll('src="../../../../knowage/themes/commons/img/olap/DESC-rows.png"', ' <div class="sort-desc"></div ')
                this.olap.table = this.olap.table.replaceAll('<a href="#" onClick="parent.execExternal', '<a href="#" class="external-cross-navigation" crossParams="parent.execExternal')
                this.olap.table = this.olap.table.replaceAll('src="../../../../knowage/themes/commons/img/olap/cross-navigation.png"', ' <div class="cell-cross-navigation"></div ')
                this.olap.table = this.olap.table.replaceAll('src="../../../../knowage/themes/commons/img/olap/ico_search.gif"', ' <div class="drillthrough"></div ')
            }
        },
        async drillDown(event: any) {
            this.loading = true
            const axis = event.target.parentNode.getAttribute('axis')
            const position = event.target.parentNode.getAttribute('position')
            const member = event.target.parentNode.getAttribute('memberordinal')

            const postData = JSON.stringify({
                memberUniqueName: event.target.parentNode.getAttribute('uniquename'),
                positionUniqueName: event.target.parentNode.getAttribute('positionuniquename')
            })
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/member/drilldown/${axis}/${position}/${member}/?SBI_EXECUTION_ID=${this.id}`, postData, {
                    headers: {
                        Accept: 'application/json, text/plain, */*',
                        'Content-Type': 'application/json;charset=UTF-8'
                    }
                })
                .then((response: AxiosResponse<any>) => (this.olap = response.data))
                .catch(() => {})

            this.formatOlapTable()

            this.loading = false
        },
        async drillUp(event: any, replace: boolean) {
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/member/drillup?SBI_EXECUTION_ID=${this.id}`, this.formatDrillUpPostData(event, replace), {
                    headers: {
                        Accept: 'application/json, text/plain, */*',
                        'Content-Type': 'application/json;charset=UTF-8'
                    }
                })
                .then((response: AxiosResponse<any>) => (this.olap = response.data))
                .catch(() => {})

            this.formatOlapTable()

            this.loading = false
        },
        formatDrillUpPostData(event: any, replace: boolean) {
            let tempArray = [] as any[]
            if (replace) {
                const temp = event.target.attributes[0].textContent
                const tempString = temp.substring(temp.indexOf('(') + 1, temp.lastIndexOf(')'))
                tempArray = tempString?.split(',')
            }

            const postData = JSON.stringify({
                axis: replace ? +tempArray[0] : event.target.parentNode.getAttribute('axis'),
                memberPosition: replace ? +tempArray[2] : event.target.parentNode.getAttribute('memberordinal'),
                memberUniqueName: replace ? tempArray[3].trim().substring(1, tempArray[3].length - 1) : event.target.parentNode.getAttribute('uniquename'),
                position: replace ? +tempArray[1] : event.target.parentNode.getAttribute('position'),
                positionUniqueName: replace ? tempArray[4].trim().substring(1, tempArray[4].length - 2) : event.target.parentNode.getAttribute('positionuniquename')
            })

            return postData
        },
        async onDrillTypeChanged(newDrillType: string) {
            this.olap.modelConfig.drillType = newDrillType
            await this.loadModelConfig()
        },
        async onShowParentMemberChanged(showParentMembers: boolean) {
            this.olap.modelConfig.showParentMembers = showParentMembers
            await this.loadModelConfig()
        },
        async onHideSpansChanged(hideSpans: boolean) {
            this.olap.modelConfig.hideSpans = hideSpans
            await this.loadModelConfig()
        },
        async onSuppressEmptyChanged(suppressEmpty: boolean) {
            this.olap.modelConfig.suppressEmpty = suppressEmpty
            await this.loadModelConfig()
        },
        async onDrillThroughChanged(enableDrillThrough: boolean) {
            this.olap.modelConfig.enableDrillThrough = enableDrillThrough
            await this.loadModelConfig()
        },
        async onShowPropertiesChanged(showProperties: boolean) {
            this.olap.modelConfig.showProperties = showProperties
            await this.loadModelConfig()
        },
        onSortingSelect(payload: { sortingMode: string; sortingCount: number }) {
            this.sort = payload

            if ((this.sort.sortingMode === 'no sorting' && this.olap?.modelConfig.sortingEnabled) || (this.sort.sortingMode !== 'no sorting' && !this.olap.modelConfig.sortingEnabled)) {
                this.enableSorting()
            }

            this.sortingDialogVisible = false
        },
        async enableSorting() {
            this.loading = true
            await this.$http
                .get(process.env.VUE_APP_OLAP_PATH + `1.0/member/sort/disable?SBI_EXECUTION_ID=${this.id}`, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then((response: AxiosResponse<any>) => (this.olap = response.data))
                .catch(() => {})

            this.formatOlapTable()
            this.loading = false
        },
        async sortOlap(event: any) {
            this.loading = true
            const temp = event.target.attributes[0].textContent
            const tempString = temp.substring(temp.indexOf('(') + 1, temp.indexOf(')'))
            const tempArray = tempString?.split(',')
            const tempPositionUniqueName = tempArray.splice(2).join(',')

            const postData = {
                axisToSort: +tempArray[0],
                axis: +tempArray[1],
                positionUniqueName: tempPositionUniqueName.substring(tempPositionUniqueName.indexOf("'") + 1, tempPositionUniqueName.lastIndexOf("'")),
                sortMode: this.sort.sortingMode,
                topBottomCount: this.sort.sortingCount
            }

            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/member/sort/?SBI_EXECUTION_ID=${this.id}`, postData)
                .then((response: AxiosResponse<any>) => (this.olap = response.data))
                .catch(() => {})
            this.formatOlapTable()
            this.loading = false
        },
        async reloadOlap() {
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/cache/?SBI_EXECUTION_ID=${this.id}`, null, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then((response: AxiosResponse<any>) => (this.olap = response.data))
                .catch(() => {})
            this.formatOlapTable()
            this.loading = false
        },
        async putFilterOnAxis(fromAxis, filter) {
            if (fromAxis === -1) this.removeFilterLevels(filter)
            var toSend = { fromAxis: fromAxis, hierarchy: filter.selectedHierarchyUniqueName, toAxis: filter.axis }
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/axis/moveDimensionToOtherAxis?SBI_EXECUTION_ID=${this.id}`, toSend, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then((response: AxiosResponse<any>) => {
                    this.olap = response.data
                    console.log(this.olapDesigner)
                    if (this.olapDesigner && this.olapDesigner.template) {
                        this.olapDesigner.template.wrappedObject.olap.MDXMondrianQuery.XML_TAG_TEXT_CONTENT = this.olap.MDXWITHOUTCF
                        this.olapDesigner.template.wrappedObject.olap.MDXQUERY.XML_TAG_TEXT_CONTENT = this.olap.MDXWITHOUTCF
                    }
                })
                .catch(() => this.$store.commit('setError', { title: this.$t('common.toast.error'), msg: this.$t('documentExecution.olap.filterToolbar.putFilterOnAxisError') }))
            this.formatOlapTable()
            this.loading = false
        },
        async swapAxis() {
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/axis/swap?SBI_EXECUTION_ID=${this.id}`, null, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then((response: AxiosResponse<any>) => (this.olap = response.data))
                .catch(() => this.$store.commit('setError', { title: this.$t('common.toast.error'), msg: this.$t('documentExecution.olap.filterToolbar.swapAxisError') }))
            this.formatOlapTable()
            this.loading = false
        },
        async moveHierarchies(data) {
            var toSend = { axis: data.axis, hierarchy: data.selectedHierarchyUniqueName, newPosition: data.positionInAxis + 1, direction: 1 }
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/axis/moveHierarchy?SBI_EXECUTION_ID=${this.id}`, toSend, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then((response: AxiosResponse<any>) => {
                    this.olap = response.data
                })
                .catch(() => this.$store.commit('setError', { title: this.$t('common.toast.error'), msg: this.$t('documentExecution.olap.filterToolbar.hierarchyMoveError') }))
            this.formatOlapTable()
            this.loading = false
        },
        setMultiHierUN(un) {
            this.selecetedMultiHierUN = un
        },
        showMultiHierarchy(filter) {
            this.multiHierFilter = filter
            this.selecetedMultiHierUN = this.multiHierFilter.hierarchies[this.multiHierFilter.selectedHierarchyPosition].uniqueName
            this.multiHierarchyDialogVisible = true
        },
        async updateHierarchy() {
            var oldHier = this.multiHierFilter.hierarchies[this.multiHierFilter.selectedHierarchyPosition].uniqueName
            var newHier = this.selecetedMultiHierUN
            if (oldHier != newHier) {
                var toSend = { axis: this.multiHierFilter.axis, oldHierarchyUniqueName: oldHier, newHierarchyUniqueName: newHier, hierarchyPosition: this.multiHierFilter.positionInAxis }
                this.loading = true
                await this.$http
                    .post(process.env.VUE_APP_OLAP_PATH + `1.0/axis/updateHierarchyOnDimension?SBI_EXECUTION_ID=${this.id}`, toSend, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                    .then((response: AxiosResponse<any>) => (this.olap = response.data))
                    .catch(() => this.$store.commit('setError', { title: this.$t('common.toast.error'), msg: this.$t('documentExecution.olap.filterToolbar.hierarchyUpdateError') }))
                    .finally(() => {
                        this.formatOlapTable()
                        this.loading = false
                        this.multiHierarchyDialogVisible = false
                    })
            }
        },
        execExternalCrossNavigation(event: any) {
            if (this.olapDesignerMode) {
                return
            }

            const tempCrossNavigationParams = event.target.attributes[2].value
            const tempFormatted = tempCrossNavigationParams.substring(tempCrossNavigationParams.indexOf('(') + 2, tempCrossNavigationParams.lastIndexOf(')') - 1)

            const tempArray = tempFormatted.split(',')
            const object = {}
            tempArray?.forEach((el: string) => {
                object[el.substring(0, el.indexOf(':'))] = el.substring(el.indexOf(':') + 2, el.length - 1)
            })
            this.$emit('executeCrossNavigation', object)
        },
        async enableCrossNaivigation(crossNavigation: boolean) {
            this.loading = true
            this.olap.modelConfig.crossNavigation.buttonClicked = crossNavigation
            await this.$http
                .get(process.env.VUE_APP_OLAP_PATH + `1.0/crossnavigation/initialize/?SBI_EXECUTION_ID=${this.id}`, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then((response: AxiosResponse<any>) => (this.olap = response.data))
                .catch(() => {})
            this.formatOlapTable()
            this.loading = false
        },
        async getCrossNavigationURL(event: any) {
            if (this.olapDesignerMode) {
                return
            }

            this.loading = true

            const tempString = event.target.attributes[1].textContent
            const tempParametersString = tempString.substring(tempString.indexOf('(') + 1, tempString.indexOf(')'))
            const temp = tempParametersString?.substring(1, tempParametersString.length - 1)?.split(',')

            let tempResponse = null
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/crossnavigation/getCrossNavigationUrl/${temp[0]},${temp[1]}?SBI_EXECUTION_ID=${this.id}`, null, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then((response: AxiosResponse<any>) => (tempResponse = response.data))
                .catch(() => {})
            await this.executeCrossnavigationFromCell(tempResponse)

            this.loading = false
        },
        async executeCrossnavigationFromCell(crossNavigationString: string | null) {
            const tempString = crossNavigationString?.substring(crossNavigationString.indexOf('{') + 1, crossNavigationString.indexOf('}'))
            const tempArray = tempString?.split(',')

            const object = {}
            tempArray?.forEach((el: string) => {
                object[el.substring(0, el.indexOf(':'))] = el.substring(el.indexOf(':') + 2, el.length - 1)
            })

            this.$emit('executeCrossNavigation', object)
        },
        enterSelectMode(mode: string) {
            this.mode = mode
            this.olapSidebarVisible = false
            this.crossNavigationDefinitionDialogVisible = false
        },
        selectCell(event: any) {
            const attributes = event.target.attributes

            if (attributes[0].localName !== 'axisordinal') {
                return
            }

            const cell = {}
            for (let i = 0; i < attributes.length; i++) {
                cell[attributes[i].nodeName] = attributes[i].value
            }

            if (this.selectedCell) {
                this.selectedCell.event.target.style.border = 'none'
            }

            this.selectedCell = { cell: cell, event: event }
            event.target.style.border = '2px solid red'
        },
        cellSelected() {
            this.olapSidebarVisible = true
            this.crossNavigationDefinitionDialogVisible = true
            this.mode = 'view'
            this.selectedCell.event.target.style.border = 'none'
        },
        async saveOlapDesigner() {
            this.loading = true

            console.log('OLAP DESINGER ON SAVE: ', this.olapDesigner)

            await this.$http
                .post(
                    process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documents/${this.documentName}/saveOlapTemplate`,
                    { olap: { ...this.olapDesigner.template.wrappedObject.olap, JSONTEMPLATE: { XML_TAG_TEXT_CONTENT: JSON.stringify(this.olapDesigner.template.wrappedObject) } } },
                    { headers: { Accept: 'application/json, text/plain, */*' } }
                )
                .then(async () => {
                    this.$store.commit('setInfo', { title: this.$t('common.toast.updateTitle'), msg: this.$t('common.toast.updateSuccess') })
                    await this.loadOlapDesigner()
                })
                .catch(() => {})
            this.loading = false
        },
        closeDrillTruDialog() {
            this.drillTruDialogVisible = false
            this.dtData = []
            this.dtColumns = []
            this.dtTree = []
            this.dtAssociatedLevels = []
        },
        async drillThrough(ordinal?: any) {
            ordinal ? (this.usedOrdinal = this.getOrdinalFromEvent(ordinal)) : ''
            this.$store.commit('setInfo', { title: this.$t('documentExecution.olap.drillTru.loadingTitle'), msg: this.$t('documentExecution.olap.drillTru.loadingMsg') })
            this.loading = true
            if (this.dtAssociatedLevels.length == 0 && this.dtMaxRows == 0) {
                let toSend = {} as any
                toSend.ordinal = this.usedOrdinal
                await this.$http
                    .post(process.env.VUE_APP_OLAP_PATH + `1.0/member/drilltrough?SBI_EXECUTION_ID=${this.id}`, toSend, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                    .then((response: AxiosResponse<any>) => {
                        this.dtData = []
                        this.dtColumns = []

                        this.dtData = response.data
                        for (var key in response.data[0]) {
                            this.dtColumns.push(key)
                        }
                        this.formattedColumns = this.formatColumns(this.dtColumns)
                        this.getCollections()
                        this.drillTruDialogVisible = true
                    })
                    .catch(() => {
                        this.$store.commit('setError', { title: this.$t('common.toast.error'), msg: this.$t('documentExecution.olap.drillTru.drillTruError') })
                        this.loading = false
                    })
                    .finally(() => (this.loading = false))
            } else {
                let toSend = {} as any
                toSend.ordinal = this.usedOrdinal
                toSend.levels = JSON.stringify(this.dtAssociatedLevels)
                toSend.max = this.dtMaxRows
                await this.$http
                    .post(process.env.VUE_APP_OLAP_PATH + `1.0/member/drilltrough/full?SBI_EXECUTION_ID=${this.id}`, toSend, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                    .then((response: AxiosResponse<any>) => {
                        this.dtData = []
                        this.dtColumns = []
                        this.dtData = response.data
                        for (var key in response.data[0]) {
                            this.dtColumns.push(key)
                        }
                        this.formattedColumns = this.formatColumns(this.dtColumns)
                    })
                    .catch(() => {
                        this.$store.commit('setError', { title: this.$t('common.toast.error'), msg: this.$t('documentExecution.olap.drillTru.drillTruError') })
                        this.loading = false
                    })
                    .finally(() => (this.loading = false))
            }
        },
        getOrdinalFromEvent(event: any) {
            const drillThroughAttribute = event.target.attributes[1].textContent

            const ordinal = parseInt(drillThroughAttribute.substring(drillThroughAttribute.indexOf('(') + 1, drillThroughAttribute.indexOf(')')))
            return ordinal
        },
        formatColumns(array) {
            let arr = [] as any
            for (var i = 0; i < array.length; i++) {
                var obj = {} as any
                obj.label = array[i].toUpperCase()
                obj.name = array[i]
                obj.size = '100px'
                arr.push(obj)
            }
            return arr
        },
        async getCollections() {
            var toSend = {} as any
            toSend.filters = JSON.stringify(this.olap.filters)

            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/member/drilltrough/levels/?SBI_EXECUTION_ID=${this.id}`, toSend, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then((response: AxiosResponse<any>) => {
                    this.dtTree = response.data
                    setTimeout(() => {
                        this.checkDtLevels()
                    }, 500)
                })
                .catch(() => {
                    this.$store.commit('setError', { title: this.$t('common.toast.error'), msg: this.$t('documentExecution.olap.drillTru.drillLevelsError') })
                })
        },

        checkDtLevels() {
            var tempArr = [] as any
            if (this.dtTree != null && this.formattedColumns != null) {
                for (var i = 0; i < this.dtTree.length; i++) {
                    for (var j = 0; j < this.dtTree[i].children.length; j++) {
                        for (var k = 0; k < this.formattedColumns.length; k++) {
                            if (this.formattedColumns[k].label == this.dtTree[i].children[j].caption.toUpperCase()) {
                                this.dtTree[i].children[j].checked = true
                                tempArr.push(this.dtTree[i].children[j])
                            } else {
                                this.dtTree[i].children[j].checked = false
                            }
                        }
                    }
                }
            } else {
                this.$store.commit('setError', { title: this.$t('common.toast.error'), msg: this.$t('documentExecution.olap.drillTru.checkingLevelsError') })
            }

            for (i = 0; i < tempArr.length; i++) {
                this.checkCheckboxes(tempArr[i], this.dtAssociatedLevels)
            }
        },

        checkCheckboxes(item, list) {
            // eslint-disable-next-line no-prototype-builtins
            if (item.hasOwnProperty('caption')) {
                var index = this.indexInList(item, list)

                if (index != -1) {
                    this.dtAssociatedLevels.splice(index, 1)
                } else {
                    this.dtAssociatedLevels.push(item)
                }
            }
        },

        indexInList(item, list) {
            // eslint-disable-next-line no-prototype-builtins
            if (item.hasOwnProperty('caption')) {
                for (var i = 0; i < list.length; i++) {
                    var object = list[i]
                    if (object.caption == item.caption) {
                        return i
                    }
                }
            }
            return -1
        },

        async handleTableClick(event: any) {
            const eventTarget = event.target as any

            if (this.mode !== 'view' && eventTarget.tagName === 'TH') {
                this.selectCell(event)
            }

            if (eventTarget) {
                switch (eventTarget.className) {
                    case 'drill-up':
                        await this.drillUp(event, false)
                        break
                    case 'drill-down':
                        await this.drillDown(event)
                        break
                    case 'drill-up-replace':
                        await this.drillUp(event, true)
                        break
                    case 'sort-basic':
                    case 'sort-asc':
                    case 'sort-desc':
                        await this.sortOlap(event)
                        break
                    case 'external-cross-navigation':
                        this.execExternalCrossNavigation(event)
                        break
                    case 'cell-cross-navigation':
                        await this.getCrossNavigationURL(event)
                        break
                    case 'drillthrough':
                        await this.drillThrough(event)
                        break
                }
            }
        },
        openFilterDialog(filter: any) {
            this.selectedFilter = filter
            this.filterDialogVisible = true
        },
        closeFilterDialog() {
            this.filterDialogVisible = false
            this.selectedFilter = null
        },
        async applyFilters(payload: any) {
            console.log(' --- applyFilters() - payload: ', payload)
            this.filterDialogVisible = false
            this.loading = true
            if (payload.type === 'slicer') {
                delete payload.type
                if (this.olapDesignerMode) this.updateDynamicSlicer(payload)
                await this.sliceOLAP(payload)
            } else {
                await this.placeMembersOnAxis(payload)
            }

            this.formatOlapTable()
            this.loading = false
        },
        async sliceOLAP(payload) {
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/hierarchy/slice?SBI_EXECUTION_ID=${this.id}`, payload, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then((response: AxiosResponse<any>) => (this.olap = response.data))
                .catch(() => {})
        },
        async placeMembersOnAxis(payload) {
            const members = payload.members?.map((member: any) => {
                return { id: member.id, leaf: member.leaf, name: member.name, uniqueName: member.uniqueName, visible: member.visible }
            })
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/axis/${payload.axis}/placeMembersOnAxis?SBI_EXECUTION_ID=${this.id}`, members, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then((response: AxiosResponse<any>) => (this.olap = response.data))
                .catch(() => {})
        },
        async loadParameters() {
            const documentLabel = this.olapDesigner.DOCUMENT_LABEL ?? this.documentLabel
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documents/${documentLabel}/parameters`)
                .then((response: AxiosResponse<any>) => (this.parameters = response.data ? response.data.results : []))
                .catch(() => {})
            console.log(' --- LOADED PARAMETERS: ', this.parameters)
        },
        async loadProfileAttributes() {
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/attributes`)
                .then((response: AxiosResponse<any>) => (this.profileAttributes = response.data))
                .catch(() => {})
        },
        async undo() {
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/model/undo/?SBI_EXECUTION_ID=${this.id}`, null, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8', 'X-Disable-Errors': 'true' } })
                .then((response: AxiosResponse<any>) => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.olap = response.data
                    this.formatOlapTable()
                })
                .catch((error: any) =>
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: error?.localizedMessage
                    })
                )
            this.loading = false
        },
        saveScenario(scenario) {
            this.olapDesigner.template.wrappedObject.olap.SCENARIO = scenario
            this.scenarioWizardVisible = false
            this.$store.commit('setInfo', { title: this.$t('common.toast.updateTitle'), msg: this.$t('documentExecution.olap.scenarioWizard.scenarioUpdated') })
        },
        deleteScenario() {
            delete this.olapDesigner.template.wrappedObject.olap.SCENARIO
            this.scenarioWizardVisible = false
        },
        updateDynamicSlicer(payload: any) {
            this.olapDesigner.template.wrappedObject.olap.DYNAMIC_SLICER = payload.DYNAMIC_SLICER?.filter((level: any) => level.DRIVER || level.PROFILE_ATTRIBUTE).map((level: any) => {
                return {
                    HIERARCHY: level.HIERARCHY,
                    LEVEL: level.LEVEL,
                    DRIVER: level.DRIVER,
                    PROFILE_ATTRIBUTE: level.PROFILE_ATTRIBUTE
                }
            })
            if (!this.olapDesigner.template.wrappedObject.olap.DYNAMIC_SLICER || this.olapDesigner.template.wrappedObject.olap.DYNAMIC_SLICER.length === 0) delete this.olapDesigner.template.wrappedObject.olap.DYNAMIC_SLICER
        },
        exportExcel() {
            if (this.checkIfVersionIsSet()) {
                this.loading = true
                this.$http
                    .get(process.env.VUE_APP_OLAP_PATH + `1.0/model/exceledit?SBI_EXECUTION_ID=${this.id}`, { headers: { Accept: 'application/json, text/plain, */*' }, responseType: 'blob' })
                    .then((response: AxiosResponse<any>) => {
                        let fileName = response.headers['content-disposition'].split('filename="')[1].split('"')[0]
                        downloadDirect(response.data, fileName, response.headers['content-type'])
                    })
                    .catch(() => {})
                    .finally(() => (this.loading = false))
            } else {
                return this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.olap.sliceVersionError') })
            }
        },
        removeFilterLevels(filter: any) {
            if (this.olapDesigner && this.olapDesigner.template && this.olapDesigner.template.wrappedObject.olap.DYNAMIC_SLICER) {
                for (let i = this.olapDesigner.template.wrappedObject.olap.DYNAMIC_SLICER.length - 1; i >= 0; i--) {
                    if (this.olapDesigner.template.wrappedObject.olap.DYNAMIC_SLICER[i].HIERARCHY === filter.uniqueName) {
                        this.olapDesigner.template.wrappedObject.olap.DYNAMIC_SLICER.splice(i, 1)
                    }
                }
            }
        },
        handleTableDoubleClick(event: any) {
            if (!this.olapHasScenario) return
            if (!event.target.attributes.cell) return
            let clickLocation = event.target.getBoundingClientRect()

            if (!this.checkIfVersionIsSet()) {
                return this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.olap.sliceVersionError') })
            } else if (this.checkIfModelIsLocked()) {
                return this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.olap.editErrorLocked') })
            } else if (!this.checkIfMeasureIsEditable(event.target.getAttribute('measurename'))) {
                return this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.olap.notEditable') })
            } else {
                // @ts-ignore
                this.$refs.whatifInput.style.top = `${clickLocation.top}px`
                // @ts-ignore
                this.$refs.whatifInput.style.left = `${clickLocation.left}px`
                // @ts-ignore
                this.$refs.whatifInput.style.display = 'flex'

                let locale = localStorage.getItem('locale') as any
                let cutLocalString = locale.split('_')

                this.whatifInputNewValue = this.parseLocaleNumber(event.target.attributes.value.value, cutLocalString[0])
                this.whatifInputOldValue = event.target.attributes.value.value
                this.whatifInputOrdinal = event.target.attributes.ordinal.value
            }
        },
        parseLocaleNumber(stringNumber, locale) {
            let num = 123456.789,
                fmt_local = new Intl.NumberFormat(locale),
                parts_local = fmt_local.formatToParts(num),
                group = ''

            // separators
            parts_local.forEach(function(i) {
                switch (i.type) {
                    case 'group':
                        group = i.value
                        break
                    default:
                        break
                }
            })

            return stringNumber.replace(new RegExp('\\' + group, 'g'), '')
        },
        checkIfVersionIsSet() {
            let versionIsSet = false
            for (let i = 0; i < this.olap.filters.length; i++) {
                if (this.olap.filters[i].uniqueName === '[Version]') {
                    versionIsSet = this.olap.filters[i].hierarchies[0].slicers.length > 0
                }
            }
            return versionIsSet
        },
        checkIfModelIsLocked() {
            if (this.olap.modelConfig.status == 'locked_by_other' || this.olap.modelConfig.status == 'unlocked') {
                return true
            } else return false
        },
        checkIfMeasureIsEditable(measureName) {
            if (this.olap.modelConfig && this.olap.modelConfig.writeBackConf) {
                if (this.olap.modelConfig.writeBackConf.editableMeasures == null || this.olap.modelConfig.writeBackConf.editableMeasures.length == 0) {
                    return true
                } else {
                    var measures = this.olap.modelConfig.writeBackConf.editableMeasures
                    for (var i = 0; i < measures.length; i++) {
                        if (measures[i] === measureName) {
                            return true
                        }
                    }
                }
                return false
            }
        },
        closeWhatifInput() {
            // @ts-ignore
            this.$refs.whatifInput.style.display = 'none'
        },
        async onWhatifInput() {
            if (this.whatifInputNewValue != this.whatifInputOldValue) {
                let postData = { expression: this.whatifInputNewValue }
                this.loading = true
                await this.$http
                    .post(process.env.VUE_APP_OLAP_PATH + `1.0/model/setValue/${this.whatifInputOrdinal}?SBI_EXECUTION_ID=${this.id}`, postData, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                    .then((response: AxiosResponse<any>) => {
                        this.olap = response.data
                        this.closeWhatifInput()
                        this.formatOlapTable()
                    })
                    .catch(() => {})
                    .finally(() => (this.loading = false))
            }
            this.closeWhatifInput()
        },
        onNewVersionSaved(olap: iOlap) {
            this.olap = olap
            this.formatOlapTable()
            this.saveVersionDialogVisible = false
        }
    }
})
</script>

<style lang="scss">
.olap-overlay-dialog {
    position: absolute;
    z-index: 300;
    background-color: white;
    height: 100%;
    width: 100%;
}
.olap-page-container {
    display: flex;
    flex-direction: column;
}

#olap-backdrop {
    background-color: rgba(33, 33, 33, 1);
    opacity: 0.48;
    z-index: 50;
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
}

#olap-select-message {
    flex: 0.7;
}

#olap-select-button {
    max-width: 200px;
    max-height: 40%;
    flex: 0.3;
}

.olap-sidebar {
    margin-left: auto;
}

.drill-up {
    background-image: url('../../../assets/images/olap/minus.gif');
    background-position: center;
    background-repeat: no-repeat;
    height: 0.8rem;
    width: 0.8rem;
    float: left;
}

.drill-down {
    background-image: url('../../../assets/images/olap/plus.gif');
    background-position: center;
    background-repeat: no-repeat;
    height: 0.8rem;
    width: 0.8rem;
    float: left;
}

.drill-up-replace {
    background-image: url('../../../assets/images/olap/arrow-up.png');
    background-position: center;
    background-repeat: no-repeat;
    height: 0.8rem;
    width: 0.8rem;
    float: left;
}

.sort-basic {
    background-image: url('../../../assets/images/olap/noSortRows.png');
    background-position: center;
    background-repeat: no-repeat;
    height: 0.8rem;
    width: 0.8rem;
    float: left;
}

.sort-asc {
    background-image: url('../../../assets/images/olap/ASC-rows.png');
    background-position: center;
    background-repeat: no-repeat;
    height: 0.8rem;
    width: 0.8rem;
    float: left;
}

.sort-desc {
    background-image: url('../../../assets/images/olap/DESC-rows.png');
    background-position: center;
    background-repeat: no-repeat;
    height: 0.8rem;
    width: 0.8rem;
    float: left;
}

.cell-cross-navigation {
    background-image: url('../../../assets/images/olap/cross-navigation.png');
    background-position: center;
    background-repeat: no-repeat;
    height: 0.8rem;
    width: 0.8rem;
    float: left;
}

.drillthrough {
    background-image: url('../../../assets/images/olap/ico_search.gif');
    background-position: center;
    background-repeat: no-repeat;
    height: 0.8rem;
    width: 0.8rem;
    float: left;
}

.kn-olap-table {
    position: absolute;
    overflow: auto;
    width: 100%;
    height: 100%;
    text-align: left;
    table-layout: fixed;
    color: rgba(0, 0, 0, 0.54);
    font-size: 12px;
    table {
        border-collapse: collapse;
        thead {
            border-bottom: 1px solid #ccc;
            overflow: auto;
            th {
                position: relative !important;
                border-right: 1px solid #ccc;
                border-left: 1px solid #ccc;
                padding: 5px;
                background: #f5f5f5;
                white-space: nowrap;
                text-align: left;
            }
            td {
                text-align: right;
                vertical-align: middle;
                border-bottom: 1px solid #3b678c;
                border-right: 1px solid #3b678c;
                max-height: 43px !important;
            }
        }
        tbody {
            th {
                border-right: 1px solid #ccc;
                padding-right: 5px;
            }
            td[measurename] {
                text-align: right;
            }
        }
        tr {
            &:nth-child(even) {
                background-color: #eceff1;
            }
            &:nth-child(odd) {
                background-color: white;
            }
        }
    }
}

#whatif-input {
    width: 358px;
    height: 22px;
    position: absolute;
    display: none;
    z-index: 99999;
}
</style>
