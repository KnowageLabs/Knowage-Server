<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary">
        <template #left>{{ document?.label }} </template>

        <template #right>
            <div>
                <i class="fa fa-filter kn-cursor-pointer" v-tooltip.left="$t('common.parameters')" @click="parameterSidebarVisible = !parameterSidebarVisible"></i>
            </div>
        </template>
    </Toolbar>
    <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" />

    <div id="document-execution-view" class="p-d-flex p-flex-row">
        <div v-if="parameterSidebarVisible" id="document-execution-backdrop" @click="parameterSidebarVisible = false"></div>

        <!-- <Registry v-if="mode === 'registry' && urlData && !loading && false" :id="urlData.sbiExecutionId"></Registry> -->
        <KnParameterSidebar class="document-execution-parameter-sidebar" v-if="parameterSidebarVisible" :filtersData="filtersData" @execute="onExecute" @exportCSV="onExportCSV"></KnParameterSidebar>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import KnParameterSidebar from '@/components/UI/KnParameterSidebar/KnParameterSidebar.vue'
// import Registry from '../registry/Registry.vue'

export default defineComponent({
    name: 'document-execution',
    components: {
        KnParameterSidebar
        // Registry
    },
    props: { id: { type: String } },
    data() {
        return {
            document: null as any,
            filtersData: null as any,
            urlData: null as any,
            exporters: null as any,
            mode: null as any,
            parameterSidebarVisible: false,
            user: null as any,
            loading: false
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
            // console.log('LOADED DOCUMENT: ', this.document)
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

            let test = new FormData()

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

                    test.append(k, decodeURIComponent(postObject.params[k]).replace(/\+/g, ' '))
                }
            }

            for (let i = postForm.elements.length - 1; i >= 0; i--) {
                const postFormElement = postForm.elements[i].id.replace('postForm_', '')
                if (!(postFormElement in postObject.params)) {
                    postForm.removeChild(postForm.elements[i])

                    console.log('postFormElement', postFormElement)
                    delete test[postForm.elements[i]]
                }
            }

            // console.log('POST FORM: ', postForm)
            // console.log('POST FORM TEST: ', test)
            // postForm.submit()

            this.$http.post(`/knowageqbeengine/servlet/AdapterHTTP`, test, {
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
                postData.parameters[this.filtersData.filterStatus[key].urlName] = this.filtersData.filterStatus[key].parameterValue[0].value
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
</style>
