<template>
    <div class="kn-page">
        <KnOverlaySpinnerPanel :visibility="loading" />
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('managers.bml.title') }}
            </template>
            <template #end>
                <Button icon="fa-solid fa-arrows-rotate" class="p-button-text p-button-rounded p-button-plain" @click="loadAllData" />
            </template>
        </Toolbar>
        <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" data-test="progress-bar" />
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { iLov, iAnalyticalDriver, iDocument } from './BehaviouralModelLineage'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
import ProgressBar from 'primevue/progressbar'
export default defineComponent({
    name: 'behavioural-model-lineage',
    components: { KnOverlaySpinnerPanel, ProgressBar },
    data() {
        return {
            loading: false,
            allLovs: [] as iLov[],
            allDrivers: [] as iAnalyticalDriver[],
            allDocuments: [] as iDocument[]
        }
    },
    created() {
        this.loadAllData()
    },
    methods: {
        async loadAllData() {
            this.loading = true
            await Promise.all([this.loadAllLovs(), this.loadAllDrivers(), this.loadAllDocuments()]).then(() => (this.loading = true))
        },
        async loadAllLovs() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/lovs/get/all/').then((response: AxiosResponse<any>) => {
                this.allLovs = response.data
            })
        },
        async loadAllDrivers() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers').then((response: AxiosResponse<any>) => {
                this.allDrivers = response.data
            })
        },
        async loadAllDocuments() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/documents').then((response: AxiosResponse<any>) => {
                this.allDocuments = response.data
            })
        }
    }
})
</script>
