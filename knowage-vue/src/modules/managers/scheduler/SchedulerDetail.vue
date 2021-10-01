<template>
    <p>{{ selectedJob }}</p>
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
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iPackage } from './Scheduler'
import Card from 'primevue/card'
import SchedulerDocumentsTable from './SchedulerDocumentsTable/SchedulerDocumentsTable.vue'

export default defineComponent({
    name: 'scheduler-detail',
    components: { Card, SchedulerDocumentsTable },
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
