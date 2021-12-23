<template>
    <div class="p-d-flex p-flex-column kn-flex">
        <FilterPanel :olapProp="olap" @putFilterOnAxis="putFilterOnAxis" />
        <FilterTopToolbar :olapProp="olap" @openSidebar="olapSidebarVisible = true" @putFilterOnAxis="putFilterOnAxis" />

        <div id="left-and-table-container" class="p-d-flex p-flex-row kn-flex">
            <FilterLeftToolbar :olapProp="olap" @openSidebar="olapSidebarVisible = true" @putFilterOnAxis="putFilterOnAxis" />
            <div id="olap-table" class="kn-flex" ref="olap-table" v-if="olap && olap.table && !customViewVisible" v-html="olap.table" @click="handleTableClick"></div>
        </div>

        <!-- SELECT TOAST CONFIRM  TODO:Premestiti stilove u deskriptor -------------------------------------->
        <div v-if="mode === 'member' || mode === 'cell'" id="custom-toast" style="position:fixed;width:25rem;top:20px;right:20px;z-index:2000">
            <div id="custom-toast-content" style="background: #B3E5FC; border:solid#B3E5FC; border-width:1px; color:#01579B;padding:1rem;box-shadow: 0 0.25rem 0.75rem rgb(0 0 0 / 10%);border-radius: 4px;">
                <div class="p-d-flex p-flex-column">
                    <div class="p-text-center p-d-flex p-flex-row p-ai-center">
                        <i class="pi pi-info-circle" style="font-size: 2rem"></i>
                        <h4 class="p-ml-2">{{ $t('documentExecution.olap.crossNavigationDefinition.finishSelection') }}</h4>
                        <Button class="p-jc-center" style="background-color:transparent;color:#01579B;width:5px;margin-left:auto" label="OK" @click="cellSelected" />
                    </div>
                </div>
            </div>
        </div>

        <!-- SIDEBAR -------------------------------------->
        <div v-if="olapSidebarVisible" id="olap-backdrop" @click="olapSidebarVisible = false" />
        <OlapSidebar
            v-if="olapSidebarVisible"
            class="olap-sidebar kn-overflow-y"
            :olap="olap"
            @openCustomViewDialog="customViewSaveDialogVisible = true"
            @drillTypeChanged="onDrillTypeChanged"
            @showParentMemberChanged="onShowParentMemberChanged"
            @hideSpansChanged="onHideSpansChanged"
            @suppressEmptyChanged="onSuppressEmptyChanged"
            @showPropertiesChanged="onShowPropertiesChanged"
            @openSortingDialog="sortingDialogVisible = true"
            @openMdxQueryDialog="mdxQueryDialogVisible = true"
            @reloadSchema="reloadOlap"
            @enableCrossNavigation="enableCrossNaivigation"
            @openCrossNavigationDefinitionDialog="crossNavigationDefinitionDialogVisible = true"
            @openButtonWizardDialog="buttonsWizardDialogVisible = true"
        />

        <OlapCustomViewTable v-if="customViewVisible" class="p-m-2" :olapCustomViews="olapCustomViews" @close="$emit('closeOlapCustomView')" @applyCustomView="$emit('applyCustomView', $event)"></OlapCustomViewTable>
    </div>

    <!-- DIALOGS ------------------------------------------->
    <OlapCustomViewSaveDialog :visible="customViewSaveDialogVisible" :sbiExecutionId="id" @close="customViewSaveDialogVisible = false"></OlapCustomViewSaveDialog>
    <OlapSortingDialog :visible="sortingDialogVisible" :olap="olap" @save="onSortingSelect"></OlapSortingDialog>
    <OlapMDXQueryDialog :visible="mdxQueryDialogVisible" :mdxQuery="olap?.MDXWITHOUTCF" @close="mdxQueryDialogVisible = false"></OlapMDXQueryDialog>
    <OlapCrossNavigationDefinitionDialog :visible="crossNavigationDefinitionDialogVisible" :selectedCell="selectedCell" @close="crossNavigationDefinitionDialogVisible = false" @selectFromTable="enterSelectMode($event)"></OlapCrossNavigationDefinitionDialog>
    <OlapButtonWizardDialog :visible="buttonsWizardDialogVisible" :propButtons="buttons" @close="buttonsWizardDialogVisible = false"></OlapButtonWizardDialog>
    <KnOverlaySpinnerPanel :visibility="loading" />
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import { iOlapCustomView, iButton } from './Olap'
import olapDescriptor from './OlapDescriptor.json'
import OlapSidebar from './olapSidebar/OlapSidebar.vue'
import OlapSortingDialog from './sortingDialog/OlapSortingDialog.vue'
import OlapCustomViewTable from './customView/OlapCustomViewTable.vue'
import OlapCustomViewSaveDialog from './customViewSaveDialog/OlapCustomViewSaveDialog.vue'
import OlapMDXQueryDialog from './mdxQueryDialog/OlapMDXQueryDialog.vue'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
import FilterPanel from './filterPanel/OlapFilterPanel.vue'
import FilterTopToolbar from './filterToolbar/OlapTopFilterToolbar.vue'
import FilterLeftToolbar from './filterToolbar/OlapLeftFilterToolbar.vue'
import OlapCrossNavigationDefinitionDialog from './crossNavigationDefinition/OlapCrossNavigationDefinitionDialog.vue'
import OlapButtonWizardDialog from './buttonWizard/OlapButtonWizardDialog.vue'

export default defineComponent({
    name: 'olap',
    components: { OlapSidebar, OlapCustomViewTable, OlapCustomViewSaveDialog, KnOverlaySpinnerPanel, OlapSortingDialog, FilterPanel, FilterTopToolbar, FilterLeftToolbar, OlapMDXQueryDialog, OlapCrossNavigationDefinitionDialog, OlapButtonWizardDialog },
    props: { id: { type: String }, olapId: { type: String }, reloadTrigger: { type: Boolean }, olapCustomViewVisible: { type: Boolean } },
    emits: ['closeOlapCustomView', 'applyCustomView', 'executeCrossNavigation'],
    data() {
        return {
            olapDescriptor,
            olap: null as any,
            olapSidebarVisible: false,
            customViewVisible: false,
            olapCustomViews: [] as iOlapCustomView[],
            customViewSaveDialogVisible: false,
            sortingDialogVisible: false,
            mdxQueryDialogVisible: false,
            crossNavigationDefinitionDialogVisible: false,
            buttonsWizardDialogVisible: false,
            sort: null as any,
            mode: 'view',
            selectedCell: null as any,
            buttons: [] as iButton[],
            loading: false
        }
    },
    async created() {
        await this.loadPage()
    },
    computed: {},
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
            await this.loadOlapModel()
            this.loadCustomView()
            this.loading = false
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
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `/1.0/olapsubobjects/getSubObjects?idObj=${this.olapId}`)
                .then(async (response: AxiosResponse<any>) => (this.olapCustomViews = response.data.results))
                .catch(() => {})
            this.loading = false
            // console.log('LOADED OLAP CUSTOM VIEWS: ', this.olapCustomViews)
        },
        async loadOlapButtons() {
            this.loading = true
            await this.$http
                .get(process.env.VUE_APP_OLAP_PATH + `1.0/buttons`)
                .then(async (response: AxiosResponse<any>) => (this.buttons = response.data))
                .catch(() => {})
            this.loading = false
            console.log('LOADED OLAP BUTTONS: ', this.buttons)
        },
        async loadOlapModel() {
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/model/?SBI_EXECUTION_ID=${this.id}`, null, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then(async (response: AxiosResponse<any>) => {
                    this.olap = response.data
                    await this.loadOlapButtons()
                    await this.loadModelConfig()
                })
                .catch(() => {})
            this.loading = false
        },
        async loadModelConfig() {
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/modelconfig?SBI_EXECUTION_ID=${this.id}&NOLOADING=undefined`, this.olap.modelConfig, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then((response: AxiosResponse<any>) => (this.olap = response.data))
                .catch(() => {})

            this.formatOlapTable()
            this.loading = false

            // console.log('LOADED OLAP: ', this.olap)
        },
        formatOlapTable() {
            this.olap.table = this.olap.table.replaceAll('</drillup>', ' <div class="drill-up"></div></drillup> ')
            this.olap.table = this.olap.table.replaceAll('</drilldown>', '<div class="drill-down"></div> </drilldown> ')
            this.olap.table = this.olap.table.replaceAll('../../../../knowage/themes/commons/img/olap/nodrill.png', '')
            this.olap.table = this.olap.table.replaceAll('src="../../../../knowage/themes/commons/img/olap/arrow-up.png"', ' <div class="drill-up-replace"></div ')
            this.olap.table = this.olap.table.replaceAll('src="../../../../knowage/themes/commons/img/olap/noSortRows.png"', ' <div class="sort-basic"></div ')
            this.olap.table = this.olap.table.replaceAll('src="../../../../knowage/themes/commons/img/olap/ASC-rows.png"', ' <div class="sort-asc"></div ')
            this.olap.table = this.olap.table.replaceAll('src="../../../../knowage/themes/commons/img/olap/DESC-rows.png"', ' <div class="sort-desc"></div ')
            this.olap.table = this.olap.table.replaceAll('<a href="#" onClick="parent.execExternal', '<a href="#" class="external-cross-navigation" crossParams="parent.execExternal')
            this.olap.table = this.olap.table.replaceAll('src="../../../../knowage/themes/commons/img/olap/cross-navigation.png"', ' <div class="cell-cross-navigation"></div ')
        },
        async drillDown(event: any) {
            this.loading = true
            // console.log('EVENT INSIDE DRILL DOWN: ', event)
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
            // console.log('EVENT INSIDE DRILL UP: ', event)

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
        async onShowParentMemberChanged(showParantMembers: boolean) {
            this.olap.modelConfig.showParentMembers = showParantMembers
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
        async onShowPropertiesChanged(showProperties: boolean) {
            this.olap.modelConfig.showProperties = showProperties
            await this.loadModelConfig()
        },
        onSortingSelect(payload: { sortingMode: string; sortingCount: number }) {
            this.sort = payload
            // console.log('SORTING: ', this.sort)

            if ((this.sort.sortingMode === 'no sorting' && this.olap.modelConfig.sortingEnabled) || (this.sort.sortingMode !== 'no sorting' && !this.olap.modelConfig.sortingEnabled)) {
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
            // console.log('EVENT ON SORT: ', event)
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

            // console.log('TEMP ARRAY: ', tempArray)
            //  console.log('TEMP DATA: ', postData)

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
                .catch(() => this.$store.commit('setError', { title: this.$t('common.toast.error'), msg: this.$t('documentExecution.olap.filterToolbar.putFilterOnAxisError') }))
            this.formatOlapTable()
            this.loading = false
        },
        async putFilterOnAxis(fromAxis, filter) {
            console.log('putFilterOnAxis ', fromAxis, filter)
            var toSend = { fromAxis: fromAxis, hierarchy: filter.selectedHierarchyUniqueName, toAxis: filter.axis }
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/axis/moveDimensionToOtherAxis?SBI_EXECUTION_ID=${this.id}`, toSend, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then((response: AxiosResponse<any>) => (this.olap = response.data))
                .catch(() => {})
            this.formatOlapTable()
            this.loading = false
        },
        execExternalCrossNavigation(event: any) {
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
            this.loading = true

            const tempString = event.target.attributes[1].textContent
            const tempParametersString = tempString.substring(tempString.indexOf('(') + 1, tempString.indexOf(')'))
            const temp = tempParametersString?.substring(1, tempParametersString.length - 1)?.split(',')

            let tempResponse = null
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/crossnavigation/getCrossNavigationUrl/${temp[0]},${temp[1]}?SBI_EXECUTION_ID=${this.id}`, null, { headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' } })
                .then((response: AxiosResponse<any>) => (tempResponse = response.data))
                .catch(() => {})
            console.log('TEMP RESPONSE: ', tempResponse)
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
            console.log('MODE: ', mode)
            this.mode = mode
            this.olapSidebarVisible = false
            this.crossNavigationDefinitionDialogVisible = false
        },
        selectCell(event: any) {
            console.log('EVENT FOR SELECT: ', event)
            const attributes = event.target.attributes

            if (attributes[0].localName !== 'axisordinal') {
                return
            }

            const cell = {
                axisordinal: attributes[0].value,
                dimensiontype: attributes[1].value,
                dimensionuniquename: attributes[2].value,
                hierarchyuniquename: attributes[3].value,
                level: attributes[4].value,
                member: attributes[5].value,
                parentmember: attributes[6].value,
                position: attributes[7].value,
                uniquename: attributes[9]?.value
            } as any

            if (this.selectedCell) {
                this.selectedCell.event.target.style.border = 'none'
            }

            this.selectedCell = { cell: cell, event: event }
            event.target.style.border = '1px solid red'

            console.log('SELECTED CELL: ', this.selectedCell)
        },
        cellSelected() {
            console.log('SELECTED CELLS: ', this.selectedCell)
            this.olapSidebarVisible = true
            this.crossNavigationDefinitionDialogVisible = true
            this.mode = 'view'
            this.selectedCell.event.target.style.border = 'none'
        },

        async handleTableClick(event: any) {
            console.log('EVENT: ', event)

            const eventTarget = event.target as any
            console.log('event?.target.tagname', eventTarget.tagName)

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
                }
            }
        }
    }
})
</script>

<style lang="scss">
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
}

.drill-down {
    background-image: url('../../../assets/images/olap/plus.gif');
    background-position: center;
    background-repeat: no-repeat;
    height: 0.8rem;
    width: 0.8rem;
}

.drill-up-replace {
    background-image: url('../../../assets/images/olap/arrow-up.png');
    background-position: center;
    background-repeat: no-repeat;
    height: 0.8rem;
    width: 0.8rem;
}

.sort-basic {
    background-image: url('../../../assets/images/olap/noSortRows.png');
    background-position: center;
    background-repeat: no-repeat;
    height: 0.8rem;
    width: 0.8rem;
}

.sort-asc {
    background-image: url('../../../assets/images/olap/ASC-rows.png');
    background-position: center;
    background-repeat: no-repeat;
    height: 0.8rem;
    width: 0.8rem;
}

.sort-desc {
    background-image: url('../../../assets/images/olap/DESC-rows.png');
    background-position: center;
    background-repeat: no-repeat;
    height: 0.8rem;
    width: 0.8rem;
}

.cell-cross-navigation {
    background-image: url('../../../assets/images/olap/cross-navigation.png');
    background-position: center;
    background-repeat: no-repeat;
    height: 0.8rem;
    width: 0.8rem;
}
</style>
