<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ job.jobName }}</template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <Card v-if="selectedJob" id="scheduler-detail-card" class="p-m-2">
        <template #content>
            <form v-if="job" class="p-fluid p-formgrid p-grid p-m-2">
                <div class="p-field p-col-6 p-mb-6">
                    <span class="p-float-label">
                        <InputText
                            id="jobName"
                            class="kn-material-input"
                            v-model.trim="job.jobName"
                            :class="{
                                'p-invalid': job.jobName.length === 0 && jobNameDirty
                            }"
                            maxLength="80"
                            :disabled="job.edit"
                            @blur="jobNameDirty = true"
                        />
                        <label for="jobName" class="kn-material-input-label"> {{ $t('managers.scheduler.packageName') }} *</label>
                    </span>
                    <small v-if="job.jobName.length === 0 && jobNameDirty" class="p-error ">
                        {{ $t('common.validation.required', { fieldName: $t('managers.scheduler.packageName') }) }}
                    </small>
                </div>
                <div class="p-field p-col-6 p-mb-6">
                    <span class="p-float-label">
                        <InputText id="jobDescription" class="kn-material-input" maxLength="120" v-model.trim="job.jobDescription" />
                        <label for="jobDescription" class="kn-material-input-label"> {{ $t('managers.scheduler.packageDescription') }} </label>
                    </span>
                </div>
            </form>
            <SchedulerDocumentsTable :jobDocuments="job.documents" @loading="setLoading"></SchedulerDocumentsTable>
            <SchedulerTimingOutputTable :jobTriggers="job.triggers" :jobDocuments="job.documents"></SchedulerTimingOutputTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iPackage } from './Scheduler'
import Card from 'primevue/card'
import SchedulerDocumentsTable from './SchedulerDocumentsTable/SchedulerDocumentsTable.vue'
import SchedulerTimingOutputTable from './SchedulerTimingOutputTable/SchedulerTimingOutputTable.vue'

export default defineComponent({
    name: 'scheduler-detail',
    components: { Card, SchedulerDocumentsTable, SchedulerTimingOutputTable },
    props: { id: { type: String }, clone: { type: String }, selectedJob: { type: Object } },
    data() {
        return {
            job: null as iPackage | null,
            jobNameDirty: false,
            loading: false
        }
    },
    watch: {
        selectedJob() {
            this.loadJob()
        }
    },
    created() {
        this.loadJob()
        // console.log('CLONE: ', this.clone)
    },
    methods: {
        loadJob() {
            this.job = { ...this.selectedJob } as iPackage
            console.log('LOADED JOB: ', this.job)
        },
        setLoading(loading: boolean) {
            // console.log('SET LOADING: ', loading)
            this.loading = loading
        }
    }
})
</script>

<style lang="scss">
#scheduler-detail-card .p-card-body {
    padding: 0;
}
</style>
