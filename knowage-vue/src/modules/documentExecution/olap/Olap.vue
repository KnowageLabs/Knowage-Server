<template>
    <div class="kn-height-full kn-width-full olap-page-container">
        <div class="p-d-flex p-flex-column">
            <div v-if="olapSidebarVisible" id="olap-backdrop" @click="olapSidebarVisible = false"></div>
            <OlapSidebar v-if="olapSidebarVisible" class="olap-sidebar kn-overflow-y" :olap="olap" @openCustomViewDialog="customViewSaveDialogVisible = true" @drillTypeChanged="onDrillTypeChanged"></OlapSidebar>

            <div ref="olap-table" v-if="olap && olap.table && !customViewVisible" v-html="olap.table" @click="handleTableClick"></div>
            <Button @click="olapSidebarVisible = true">OPEN SIDEBAR</Button>

            <OlapCustomViewTable v-if="customViewVisible" class="p-m-2" :olapCustomViews="olapCustomViews" @close="$emit('closeOlapCustomView')" @applyCustomView="$emit('applyCustomView', $event)"></OlapCustomViewTable>
        </div>

        <!-- DIALOGS ------------------------------------------->
        <OlapCustomViewSaveDialog :visible="customViewSaveDialogVisible" :sbiExecutionId="id" @close="customViewSaveDialogVisible = false"></OlapCustomViewSaveDialog>
        <KnOverlaySpinnerPanel :visibility="loading" />
    </div>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import { iOlapCustomView } from './Olap'
import olapDescriptor from './OlapDescriptor.json'
import OlapSidebar from './olapSidebar/OlapSidebar.vue'
import OlapCustomViewTable from './customView/OlapCustomViewTable.vue'
import OlapCustomViewSaveDialog from './customViewSaveDialog/OlapCustomViewSaveDialog.vue'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'

export default defineComponent({
    name: 'olap',
    components: { OlapSidebar, OlapCustomViewTable, OlapCustomViewSaveDialog, KnOverlaySpinnerPanel },
    props: { id: { type: String }, olapId: { type: String }, reloadTrigger: { type: Boolean }, olapCustomViewVisible: { type: Boolean } },
    emits: ['closeOlapCustomView', 'applyCustomView'],
    data() {
        return {
            olapDescriptor,
            olap: null as any,
            olapSidebarVisible: false,
            customViewVisible: false,
            olapCustomViews: [] as iOlapCustomView[],
            customViewSaveDialogVisible: false,
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
            console.log('LOADED OLAP CUSTOM VIEWS: ', this.olapCustomViews)
        },
        async loadOlapModel() {
            this.loading = true
            await this.$http
                .post(
                    process.env.VUE_APP_OLAP_PATH + `1.0/model/?SBI_EXECUTION_ID=${this.id}`,
                    {},
                    {
                        headers: {
                            Accept: 'application/json, text/plain, */*',
                            'Content-Type': 'application/x-www-form-urlencoded'
                        }
                    }
                )
                .then(async (response: AxiosResponse<any>) => {
                    this.olap = response.data

                    console.log('LOADED FIRST OLAP: ', this.olap)
                    console.log('MODEL CONFIG: ', this.olap.modelConfig)

                    await this.loadModelConfig()
                })
                .catch(() => {})
            this.loading = false
        },
        async loadModelConfig() {
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/modelconfig?SBI_EXECUTION_ID=${this.id}&NOLOADING=undefined`, this.olap.modelConfig, {
                    headers: {
                        Accept: 'application/json, text/plain, */*',
                        'Content-Type': 'application/json;charset=UTF-8'
                    }
                })
                .then((response: AxiosResponse<any>) => (this.olap = response.data))
                .catch(() => {})

            this.formatOlapTable()
            this.loading = false

            console.log('LOADED OLAP: ', this.olap)
        },
        formatOlapTable() {
            this.olap.table = this.olap.table.replaceAll('</drillup>', ' <div class="drill-up"></div></drillup> ')
            this.olap.table = this.olap.table.replaceAll('</drilldown>', '<div class="drill-down"></div> </drilldown> ')
            this.olap.table = this.olap.table.replaceAll('../../../../knowage/themes/commons/img/olap/nodrill.png', '')
            this.olap.table = this.olap.table.replaceAll('src="../../../../knowage/themes/commons/img/olap/arrow-up.png"', ' <div class="drill-up-replace"></div ')
        },
        async drillDown(event: any) {
            this.loading = true
            console.log('EVENT INSIDE DRILL DOWN: ', event)
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
            console.log('EVENT INSIDE DRILL UP: ', event)

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
        async handleTableClick(event: Event) {
            console.log('EVENT: ', event)

            const eventTarget = event.target as any

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
</style>
