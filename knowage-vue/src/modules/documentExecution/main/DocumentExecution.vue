<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary">
        <template #left>{{ document?.label }} </template>

        <template #right>
            <div class="p-d-flex p-jc-around">
                <i class="pi pi-pencil kn-cursor-pointer p-mx-4" v-tooltip.left="$t('documentExecution.main.editCockpit')" @click="editCockpitDocument"></i>
                <i class="pi pi-book kn-cursor-pointer p-mx-4" v-tooltip.left="$t('common.onlineHelp')" @click="openHelp"></i>
                <i class="pi pi-refresh kn-cursor-pointer p-mx-4" v-tooltip.left="$t('common.refresh')" @click="refresh"></i>
                <i class="fa fa-filter kn-cursor-pointer p-mx-4" v-tooltip.left="$t('common.parameters')" @click="parameterSidebarVisible = !parameterSidebarVisible"></i>
                <i class="fa fa-ellipsis-v kn-cursor-pointer  p-mx-4" v-tooltip.left="$t('common.menu')" @click="toggle"></i>
                <Menu ref="menu" :model="toolbarMenuItems" :popup="true" />
                <i class="fa fa-times kn-cursor-pointer p-mx-4" v-tooltip.left="$t('common.close')" @click="closeDocument"></i>
            </div>
        </template>
    </Toolbar>
    <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" />

    <div id="document-execution-view" class="p-d-flex p-flex-row">
        <div v-if="parameterSidebarVisible" id="document-execution-backdrop" @click="parameterSidebarVisible = false"></div>

        <!-- <Registry v-if="mode === 'registry' && urlData && !loading" :id="urlData.sbiExecutionId"></Registry> -->
        <!-- <Registry v-if="mode === 'registry' && urlData && !loading" :id="'e2d23b864b7811ec9215b918e5768f09'"></Registry> -->

        <!-- <router-view v-slot="{ Component }">
            <keep-alive>
                <component :is="Component" :key="$route.fullPath"></component>
            </keep-alive>
        </router-view> -->

        <!-- <iframe class="document-execution-iframe" :src="url"></iframe> -->

        <KnParameterSidebar class="document-execution-parameter-sidebar document-execution-parameter-sidebar kn-overflow-y" v-if="parameterSidebarVisible" :filtersData="filtersData" :propDocument="document" @execute="onExecute" @exportCSV="onExportCSV"></KnParameterSidebar>

        <DocumentExecutionHelpDialog :visible="helpDialogVisible" :propDocument="document" @close="helpDialogVisible = false"></DocumentExecutionHelpDialog>
        <DocumentExecutionRankDialog :visible="rankDialogVisible" :propDocumentRank="documentRank" @close="rankDialogVisible = false"></DocumentExecutionRankDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import DocumentExecutionHelpDialog from './dialogs/DocumentExecutionHelpDialog.vue'
import DocumentExecutionRankDialog from './dialogs/DocumentExecutionRankDialog.vue'
import KnParameterSidebar from '@/components/UI/KnParameterSidebar/KnParameterSidebar.vue'
import Menu from 'primevue/menu'
// import Registry from '../registry/Registry.vue'

export default defineComponent({
    name: 'document-execution',
    components: { DocumentExecutionHelpDialog, DocumentExecutionRankDialog, KnParameterSidebar, Menu },
    props: { id: { type: String } },
    data() {
        return {
            document: null as any,
            filtersData: null as any,
            urlData: null as any,
            exporters: null as any,
            mode: null as any,
            parameterSidebarVisible: false,
            toolbarMenuItems: [] as any[],
            helpDialogVisible: false,
            documentRank: null as any,
            rankDialogVisible: false,
            user: null as any,
            loading: false
        }
    },
    computed: {
        url() {
            return (
                process.env.VUE_APP_HOST_URL +
                '/knowage/restful-services/publish?PUBLISHER=documentExecutionNg&OBJECT_ID=3251&OBJECT_LABEL=Registry_Test_1&MENU_PARAMETERS=%7B%7D&LIGHT_NAVIGATOR_DISABLED=TRUE&SBI_EXECUTION_ID=null&OBJECT_NAME=Registry_Test_1&EDIT_MODE=null&TOOLBAR_VISIBLE=null&CAN_RESET_PARAMETERS=null&EXEC_FROM=null&CROSS_PARAMETER=null'
            )
        }
    },
    async created() {
        //console.log('CURRENT ROUTE: ', this.$route)

        this.setMode()

        // console.log('MODE: ', this.mode)

        // console.log('ID: ', this.id)

        this.user = (this.$store.state as any).user

        // console.log('LOADED USER: ', this.user)

        await this.loadPage()
    },
    methods: {
        editCockpitDocument() {
            console.log('TODO - EDIT COCKPIT DOCUMENT')
        },
        openHelp() {
            this.helpDialogVisible = true
        },
        refresh() {
            console.log('TODO - REFRESH')
        },
        toggle(event: any) {
            this.createMenuItems()
            const menu = this.$refs.menu as any
            menu.toggle(event)
        },
        createMenuItems() {
            this.toolbarMenuItems = []
            this.toolbarMenuItems.push(
                {
                    label: this.$t('common.file'),
                    items: [{ icon: 'pi pi-print', label: this.$t('common.print'), command: () => this.print() }]
                },
                {
                    label: this.$t('common.export'),
                    items: [{ icon: 'pi pi-print', label: this.$t('common.export'), command: () => this.export() }]
                },
                {
                    label: this.$t('common.info.info'),
                    items: [
                        { icon: 'pi pi-info-circle', label: this.$t('common.metadata'), command: () => this.openMetadata() },
                        { icon: 'pi pi-star', label: this.$t('common.rank'), command: () => this.openRank() },
                        { icon: 'pi pi-file', label: this.$t('common.notes'), command: () => this.openNotes() }
                    ]
                },
                {
                    label: this.$t('common.shortcuts'),
                    items: [
                        { icon: '', label: this.$t('common.notes'), command: () => this.showScheduledExecutions() },
                        { icon: 'pi pi-file', label: this.$t('common.notes'), command: () => this.copyLink() },
                        { icon: 'pi pi-file', label: this.$t('common.notes'), command: () => this.openNotes() }
                    ]
                }
            )
        },
        print() {
            console.log('TODO - PRINT')
        },
        export() {
            console.log('TODO - EXPORT')
        },
        openMetadata() {
            console.log('TODO - OPEN METADATA')
        },
        async openRank() {
            console.log('TODO - OPEN RANK')
            await this.getRank()
            this.rankDialogVisible = true
        },
        openNotes() {
            console.log('TODO - OPEN NOTES')
        },
        showScheduledExecutions() {
            console.log('TODO - OPEN SCHEDULED EXECUTIONS')
        },
        copyLink() {
            console.log('TODO - OPEN COPY LINK')
        },
        closeDocument() {
            console.log('TODO - CLOSE DOCUMENT')
        },
        setMode() {
            if (this.$route.path.includes('registry')) {
                this.mode = 'registry'
            }
        },
        async loadPage() {
            this.loading = true
            await this.loadDocument()
            await this.loadFilters()
            await this.loadURL()
            await this.loadExporters()
            this.loading = false
        },
        async loadDocument() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents/${this.id}`).then((response: AxiosResponse<any>) => (this.document = response.data))
            console.log('LOADED DOCUMENT: ', this.document)
            switch (this.document.typeCode) {
                case 'DATAMART':
                    this.$router.push(`/document-execution/${this.document.label}/registry/${this.document.label}`)
            }
        },
        async loadFilters() {
            await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentexecution/filters`, { label: this.id, role: this.user.defaultRole, parameters: {} }).then((response: AxiosResponse<any>) => (this.filtersData = response.data))
            console.log('LOADED FILTERS DATA: ', this.filtersData)
        },
        async loadURL() {
            await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentexecution/url`, { label: this.id, role: this.user.defaultRole, parameters: {}, EDIT_MODE: 'null', IS_FOR_EXPORT: true }).then((response: AxiosResponse<any>) => (this.urlData = response.data))
            this.sendForm()
            // console.log('LOADED URL DATA: ', this.urlData)
        },
        async loadExporters() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/exporters/${this.urlData.engineLabel}`).then((response: AxiosResponse<any>) => (this.exporters = response.data.exporters))
            // console.log('LOADED EXPORTERS: ', this.exporters)
        },
        sendForm() {
            const documentUrl = this.urlData.url + '&timereloadurl=' + new Date().getTime()
            const postObject = { params: { document: null }, url: documentUrl.split('?')[0] }
            const paramsFromUrl = documentUrl.split('?')[1].split('&')
            // console.log('DOCUMENT URL: ', documentUrl)

            // console.log('PARAMS FROM URL: ', paramsFromUrl)

            for (let i in paramsFromUrl) {
                if (typeof paramsFromUrl !== 'function') {
                    postObject.params[paramsFromUrl[i].split('=')[0]] = paramsFromUrl[i].split('=')[1]
                }
            }

            // console.log('POST OBJECT: ', postObject)

            // TODO - Pitati odakle ovo dolazi?!!
            // if(cockpitEditing.documentMode) postObject.params.documentMode = cockpitEditing.documentMode;

            let postForm = document.getElementById('postForm_' + postObject.params.document) as any
            if (!postForm) {
                postForm = document.createElement('form')
                postForm.id = 'postForm_' + postObject.params.document
                postForm.action = postObject.url
                postForm.method = 'post'
                postForm.target = 'documentFrame'
                document.body.appendChild(postForm)
            }

            let formData = new FormData()

            for (let k in postObject.params) {
                const inputElement = document.getElementById('postForm_' + k) as any
                if (inputElement) {
                    inputElement.value = decodeURIComponent(postObject.params[k])
                    inputElement.value = inputElement.value.replace(/\+/g, ' ')
                } else {
                    const element = document.createElement('input')
                    element.type = 'hidden'
                    element.id = 'postForm_' + k
                    element.name = k
                    element.value = decodeURIComponent(postObject.params[k])
                    element.value = element.value.replace(/\+/g, ' ')
                    postForm.appendChild(element)

                    formData.append(k, decodeURIComponent(postObject.params[k]).replace(/\+/g, ' '))
                }
            }

            for (let i = postForm.elements.length - 1; i >= 0; i--) {
                const postFormElement = postForm.elements[i].id.replace('postForm_', '')

                if (!(postFormElement in postObject.params)) {
                    postForm.removeChild(postForm.elements[i])

                    formData.delete(postFormElement)
                }
            }

            // TODO: hardkodovano
            formData.append('documentMode', 'VIEW')

            this.$http.post(postObject.url, formData, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9'
                }
            })
        },
        onExecute() {
            console.log('EXECUTE PARAMS: ', this.filtersData)
        },
        async onExportCSV() {
            console.log('ON EXPORT CSV CLICKED!', this.document)
            console.log('ON EXPORT CSV CLICKED!', this.filtersData)
            const postData = { documentId: this.document.id, documentLabel: this.document.label, exportType: 'CSV', parameters: {} }
            Object.keys(this.filtersData.filterStatus).forEach((key: any) => {
                console.log('EL: ', this.filtersData.filterStatus[key])
                const param = this.filtersData.filterStatus[key]
                if (param.multivalue) {
                    let tempString = ''
                    for (let i = 0; i < this.filtersData.filterStatus[key].parameterValue.length; i++) {
                        tempString += this.filtersData.filterStatus[key].parameterValue[i].value
                        tempString += i === this.filtersData.filterStatus[key].parameterValue.length - 1 ? '' : ','
                    }

                    postData.parameters[this.filtersData.filterStatus[key].urlName] = tempString
                } else if (param.type === 'NUM' && !param.selectionType) {
                    postData.parameters[this.filtersData.filterStatus[key].urlName] = +this.filtersData.filterStatus[key].parameterValue[0].value
                } else {
                    postData.parameters[this.filtersData.filterStatus[key].urlName] = this.filtersData.filterStatus[key].parameterValue[0] ? this.filtersData.filterStatus[key].parameterValue[0].value : this.filtersData.filterStatus[key].parameterValue.value
                }
            })
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/export/cockpitData`, postData)
                .then(() => {
                    console.log('USPELO')
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('common.exportSuccess')
                    })
                })
                .catch(() => {})
            this.loading = false
            console.log('BLA', postData)
        },
        async getRank() {
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `documentrating/getvote`, { obj: this.document.id })
                .then((response: AxiosResponse<any>) => (this.documentRank = response.data))
                .catch((error: any) =>
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: error
                    })
                )
            this.loading = false
            console.log('LOADED DOCUMENT VOTE MAIN: ', this.documentRank)
        }
    }
})
</script>

<style lang="scss" scoped>
#document-execution-view {
    position: relative;
    height: 100%;
    width: 100%;
}

#document-execution-backdrop {
    background-color: rgba(33, 33, 33, 1);
    opacity: 0.48;
    z-index: 50;
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
}

.document-execution-parameter-sidebar {
    margin-left: auto;
}

.document-execution-iframe {
    width: 100%;
    height: 100%;
}

.document-execution-parameter-sidebar {
    height: 60vh;
}
</style>
