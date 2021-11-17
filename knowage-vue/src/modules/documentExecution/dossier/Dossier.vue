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
                            <InputText
                                id="activityName"
                                class="kn-material-input p-mb-2"
                                type="text"
                                v-model.trim="v$.activity.activityName.$model"
                                :class="{
                                    'p-invalid': v$.activity.activityName.$invalid && v$.activity.activityName.$dirty
                                }"
                                maxLength="100"
                                @blur="v$.activity.activityName.$touch()"
                                data-test="activityName-input"
                            />
                            <label for="activityName" class="kn-material-input-label"> {{ $t('documentExecution.dossier.headers.activity') }} * </label>
                        </span>
                        <KnValidationMessages
                            :vComp="v$.activity.activityName"
                            :additionalTranslateParams="{
                                fieldName: $t('documentExecution.dossier.headers.activity')
                            }"
                        />
                    </div>
                    <div class="p-field p-md-2">
                        <Button class="kn-button p-button-text" :disabled="buttonDisabled" :label="$t('documentExecution.dossier.launchActivity')" @click="createNewActivity" data-test="input-button" />
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
                <KnHint v-if="showHint" :title="'documentExecution.dossier.title'" :hint="'documentExecution.dossier.hint'" data-test="hint"></KnHint>
                <DataTable v-else :value="dossierActivities" :loading="loading" v-model:filters="filters" :scrollable="true" scrollHeight="40vh" :rows="20" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack" breakpoint="960px" data-test="activities-table">
                    <template #header>
                        <div class="table-header">
                            <span class="p-input-icon-left">
                                <i class="pi pi-search" />
                                <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" badge="0" />
                            </span>
                        </div>
                    </template>
                    <template #empty>
                        {{ $t('common.info.noDataFound') }}
                    </template>
                    <template #filter="{ filterModel }">
                        <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText>
                    </template>
                    <Column field="activity" :header="$t('documentExecution.dossier.headers.activity')" :sortable="true" />
                    <Column field="creationDate" :header="$t('managers.mondrianSchemasManagement.headers.creationDate')" :sortable="true" dataType="date">
                        <template #body="{data}">
                            {{ formatDate(data.creationDate) }}
                        </template>
                    </Column>
                    <Column v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field" :style="col.style" class="kn-truncated" :sortable="true" />
                    <Column header :style="dossierDescriptor.table.iconColumn.style" @rowClick="false">
                        <template #body="slotProps">
                            <Button icon="pi pi-download" class="p-button-link" @click="downloadActivity(slotProps.data)" />
                            <Button icon="pi pi-trash" class="p-button-link" @click="deleteDossierConfirm(slotProps.data)" data-test="delete-button" />
                        </template>
                    </Column>
                </DataTable>
            </template>
        </Card>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { AxiosResponse } from 'axios'
import useValidate from '@vuelidate/core'
import dossierDescriptor from './DossierDescriptor.json'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import KnHint from '@/components/UI/KnHint.vue'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'

export default defineComponent({
    name: 'dossier',
    components: { Card, Column, DataTable, KnHint, KnValidationMessages },
    props: { id: { type: String, required: false } },
    computed: {
        showHint() {
            if (this.dossierActivities.length != 0) {
                return false
            }
            return true
        },
        buttonDisabled(): any {
            return this.v$.$invalid
        }
    },
    created() {
        this.getDossierTemplate()
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
            v$: useValidate() as any,
            dossierDescriptor,
            activity: { activityName: '' } as any,
            loading: false,
            interval: null as any,
            dossierActivities: [] as any,
            columns: dossierDescriptor.columns,
            jsonTemplate: {} as any,
            filters: {
                global: [filterDefault]
            } as Object
        }
    },
    validations() {
        return {
            activity: createValidations('activity', dossierDescriptor.validations.activity)
        }
    },
    methods: {
        formatDate(date) {
            let fDate = new Date(date)
            return fDate.toLocaleString()
        },
        async getDossierActivities() {
            this.loading = true
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `dossier/activities/${this.id}`)
                .then((response: AxiosResponse<any>) => {
                    this.dossierActivities = [...response.data]
                })
                .finally(() => {
                    this.loading = false
                })
        },
        async getDossierTemplate() {
            this.loading = true
            let url = `/knowagedossierengine/api/start/dossierTemplate?documentId=${this.id}`
            await this.$http
                .get(url, { headers: { Accept: 'application/json, text/plain, */*' } })
                .then((response: AxiosResponse<any>) => {
                    this.jsonTemplate = { ...response.data }
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
                await this.$http
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
            let url = `/knowagedossierengine/api/dossier/run?activityName=${this.activity.activityName}&documentId=${this.id}`
            await this.$http.post(url, this.jsonTemplate, { headers: { Accept: 'application/json, text/plain, */*' } }).then((response: AxiosResponse<any>) => {
                if (response.data.errors) {
                    this.$store.commit('setError', { title: this.$t('common.error.saving'), msg: response.data.errors })
                } else {
                    this.$store.commit('setInfo', { title: this.$t('common.save'), msg: this.$t('documentExecution.dossier.saveSuccess') })
                }
            })
            this.getDossierActivities()
        },
        async downloadActivity(selectedActivity) {
            if (selectedActivity.status == 'ERROR') {
                if (selectedActivity.hasBinContent) {
                    var link = process.env.VUE_APP_DOSSIER_PATH + `dossier/activity/${selectedActivity.id}/txt?activityName=${selectedActivity.activity}`
                    window.open(link)
                } else {
                    await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `dossier/random-key/${selectedActivity.progressId}`).then((response: AxiosResponse<any>) => {
                        var url = `../api/start/errorFile?activityId=${selectedActivity.id}&randomKey=${response.data}&activityName=${selectedActivity.activity}`
                        if (this.jsonTemplate.PPT_TEMPLATE != null) {
                            url += '&type=PPT'
                            url += '&templateName=' + this.jsonTemplate.PPT_TEMPLATE.name
                        } else {
                            url += '&type=DOC'
                            url += '&templateName=' + this.jsonTemplate.DOC_TEMPLATE.name
                        }
                        link = process.env.VUE_APP_RESTFUL_SERVICES_PATH + url
                        window.open(link)
                        response.data.errors ? this.$store.commit('setError', { title: this.$t('common.error.generic'), msg: response.data.errors[0].message }) : ''
                    })
                }
            } else if (selectedActivity.partial == selectedActivity.total) {
                if (selectedActivity.hasBinContent) {
                    link = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `dossier/activity/${selectedActivity.id}/pptx?activityName=${selectedActivity.activity}`
                    window.open(link)
                } else if (selectedActivity.hasDocBinContent) {
                    link = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `dossier/activity/${selectedActivity.id}/doc?activityName=${selectedActivity.activity}`
                    window.open(link)
                } else {
                    link = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `dossier/random-key/${selectedActivity.progressId}`
                    await this.$http.get(link, { headers: { Accept: 'application/json, text/plain, */*' } }).then((response: AxiosResponse<any>) => {
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
            var link = process.env.VUE_APP_HOST_URL + `/knowagedossierengine/api/start/generatePPT?activityId=${id}&randomKey=${randomKey}&templateName=${this.jsonTemplate.PPT_TEMPLATE.name}&activityName=${activityName}`
            window.open(link)
        },

        storeDOC(id, randomKey, activityName) {
            var link = process.env.VUE_APP_HOST_URL + `/knowagedossierengine/api/start/generateDOC?activityId=${id}&randomKey=${randomKey}&templateName=${this.jsonTemplate.DOC_TEMPLATE.name}&activityName=${activityName}`
            window.open(link)
        }
    }
})
</script>
