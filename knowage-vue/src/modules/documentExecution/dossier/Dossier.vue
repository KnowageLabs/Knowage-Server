<template>
    <div class="kn-page">
        <Card class="p-m-3">
            <template #header>
                <Toolbar class="kn-toolbar kn-toolbar--secondary">
                    <template #left>
                        {{ $t('managers.glossary.common.details') }}
                    </template>
                </Toolbar>
            </template>
            <template #content>
                <form class="p-fluid p-formgrid p-grid">
                    <div class="p-field p-col-12 p-md-10">
                        <span class="p-float-label">
                            <InputText id="name" class="kn-material-input" type="text" v-model.trim="activityName" maxLength="100" />
                            <label for="name" class="kn-material-input-label"> {{ $t('documentExecution.dossier.headers.activity') }} * </label>
                        </span>
                    </div>
                    <div class="p-field p-md-2">
                        <Button class="p-button-link" :label="$t('documentExecution.dossier.launchActivity')" @click="createNewActivity" />
                    </div>
                </form>
            </template>
        </Card>

        <Card class="p-m-3">
            <template #header>
                <Toolbar class="kn-toolbar kn-toolbar--secondary">
                    <template #left>
                        {{ $t('documentExecution.dossier.launchedActivities') }}
                    </template>
                </Toolbar>
            </template>
            <template #content>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <DataTable v-if="dossierActivities.length != 0" :value="dossierActivities" :loading="loading" :rows="20" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack" breakpoint="960px" data-test="activities-table">
                    <Column field="activity" :header="$t('documentExecution.dossier.headers.activity')" :sortable="true" />
                    <Column field="creationDate" :header="$t('managers.mondrianSchemasManagement.headers.creationDate')" :sortable="true" dataType="date">
                        <template #body="{data}">
                            {{ formatDate(data.creationDate) }}
                        </template>
                    </Column>
                    <Column v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field" :style="col.style" class="kn-truncated" :sortable="true" />
                    <Column header style="text-align:right">
                        <template #body="slotProps">
                            <Button icon="pi pi-download" class="p-button-link" />
                            <Button icon="pi pi-trash" class="p-button-link" @click="deleteDossierConfirm(slotProps.data)" />
                        </template>
                    </Column>
                </DataTable>
                <KnHint v-else :title="'documentExecution.dossier.title'" :hint="'documentExecution.dossier.hint'"></KnHint>
            </template>
        </Card>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import dossierDescriptor from './DossierDescriptor.json'
import axios from 'axios'
import KnHint from '@/components/UI/KnHint.vue'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'

export default defineComponent({
    name: 'configuration-management',
    components: {
        Card,
        Column,
        DataTable,
        KnHint
    },
    props: {
        id: {
            type: String,
            required: false
        }
    },
    created() {
        this.jsonTemplate = JSON.parse(this.jsonTemplateString)
        this.getDossierActivities()
        this.interval = setInterval(() => {
            this.getDossierActivities()
        }, 10000)
    },
    unmounted() {
        clearInterval(this.interval)
    },
    data() {
        return {
            activityName: '',
            loading: false,
            interval: null as any,
            dossierActivities: [] as any,
            columns: dossierDescriptor.columns,
            jsonTemplate: {} as any,
            jsonTemplateString:
                '{"name":null,"downloadable":null,"uploadable":null,"PPT_TEMPLATE":{"name":"MARE6.pptx","downloadable":null,"uploadable":null,"PPT_TEMPLATE":null,"DOC_TEMPLATE":null,"REPORT":[]},"DOC_TEMPLATE":null,"REPORT":[{"label":"Report-no-parameter","PLACEHOLDER":[{"value":"ph1"}],"PARAMETER":[],"imageName":null,"sheet":null,"sheetHeight":null,"sheetWidth":null,"deviceScaleFactor":null}]}'
        }
    },
    methods: {
        formatDate(date) {
            let fDate = new Date(date)
            return fDate.toLocaleString()
        },
        getDossierActivities() {
            this.loading = true
            return axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `dossier/activities/${this.id}`)
                .then((response) => {
                    this.dossierActivities = [...response.data]
                })
                .finally(() => {
                    this.loading = false
                })
        },
        deleteDossierConfirm(selectedDossier) {
            this.$confirm.require({
                message: this.$t('documentExecution.dossier.deleteConfirm'),
                header: this.$t('documentExecution.dossier.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteDossier(selectedDossier)
            })
        },
        async deleteDossier(selectedDossier) {
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `dossier/activity/${selectedDossier.id}`
            console.log(url)

            if (selectedDossier.status == 'DOWNLOAD' || selectedDossier.status == 'ERROR') {
                await axios
                    .delete(url, { headers: { Accept: 'application/json, text/plain, */*' } })
                    .then(() => {
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.deleteTitle'),
                            msg: this.$t('documentExecution.dossier.deleteSuccess')
                        })
                        this.getDossierActivities()
                    })
                    .catch((error) => {
                        if (error) {
                            this.$store.commit('setError', {
                                title: this.$t('common.error.generic'),
                                msg: error.message
                            })
                        }
                    })
            } else {
                this.$store.commit('setError', {
                    title: this.$t('common.error.generic'),
                    msg: this.$t('documentExecution.dossier.progressNotFinished')
                })
            }
        },
        async createNewActivity() {
            let url = `/knowagedossierengine/api/dossier/run?activityName=${this.activityName}&documentId=${this.id}`
            await axios.post(url, this.jsonTemplate, { headers: { Accept: 'application/json, text/plain, */*' } }).then((response) => {
                if (response.data.errors) {
                    this.$store.commit('setError', { msg: response.data.errors })
                } else {
                    this.$store.commit('setInfo', { msg: this.$t('documentExecution.dossier.saveSuccess') })
                }
            })
            this.getDossierActivities()
        },
        async downloadActivity(selectedActivity) {
            if (selectedActivity.status == 'ERROR') {
                if (selectedActivity.hasBinContent) {
                    //getCompleteExternalBaseUrl --------------------------------
                    var link = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `dossier/activity/${selectedActivity.id}/txt?activityName=${selectedActivity.activity}`
                } else {
                    await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `dossier/random-key/${selectedActivity.progressId}`).then((response) => {
                        var url = `../api/start/errorFile?activityId=${selectedActivity.id}&randomKey=${response.data}&activityName=${selectedActivity.activity}`
                        var link = ''
                        if (this.jsonTemplate.PPT_TEMPLATE != null) {
                            url += '&type=PPT'
                            url += '&templateName=' + this.jsonTemplate.PPT_TEMPLATE.name
                        } else {
                            url += '&type=DOC'
                            url += '&templateName=' + this.jsonTemplate.DOC_TEMPLATE.name
                        }
                        //getCompleteBaseUrl(url) --------------------------
                        link = process.env.VUE_APP_RESTFUL_SERVICES_PATH + url
                        console.log(link)
                        // $window.location = link
                        response.data.errors ? this.$store.commit('setError', { title: this.$t('common.error.generic'), msg: response.data.errors[0].message }) : ''
                    })
                }
            } else if (selectedActivity.partial == selectedActivity.total) {
                if (selectedActivity.hasBinContent) {
                    //getCompleteExternalBaseUrl --------------------------------
                    link = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `dossier/activity/${selectedActivity.id}/pptx?activityName=${selectedActivity.activity}`
                    console.log(link)
                    // $window.location = link
                } else if (selectedActivity.hasDocBinContent) {
                    //getCompleteExternalBaseUrl --------------------------------
                    link = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `dossier/activity/${selectedActivity.id}/doc?activityName=${selectedActivity.activity}`
                    console.log(link)
                    // $window.location = link
                } else {
                    await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `dossier/random-key/${selectedActivity.progressId}`).then((response) => {
                        if (this.jsonTemplate.PPT_TEMPLATE != null) {
                            this.storePPT(selectedActivity.id, response.data, selectedActivity.activity)
                        } else {
                            this.storeDOC(selectedActivity.id, response.data, selectedActivity.activity)
                        }
                        response.data.errors ? this.$store.commit('setError', { title: this.$t('common.error.generic'), msg: response.data.errors[0].message }) : ''
                    })
                }
            } else {
                this.$store.commit('setError', {
                    title: this.$t('common.error.generic'),
                    msg: this.$t('documentExecution.dossier.progressNotFinished')
                })
            }
        },
        storePPT(id, randomKey, activityName) {
            //getCompleteBaseUrl(url) --------------------------
            var link = `../api/start/generatePPT?activityId=${id}&randomKey=${randomKey}&templateName=${this.jsonTemplate.PPT_TEMPLATE.name}&activityName=${activityName}`
            console.log(link)
            // $window.location = link
        },

        storeDOC(id, randomKey, activityName) {
            //getCompleteBaseUrl(url) --------------------------
            var link = `../api/start/generateDOC?activityId=${id}&randomKey=${randomKey}&templateName=${this.jsonTemplate.DOC_TEMPLATE.name}&activityName=${activityName}`
            console.log(link)
            // $window.location = link
        }
    }
})
</script>

<style lang="scss" scoped></style>
