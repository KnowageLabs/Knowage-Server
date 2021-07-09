<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        Target definition
                    </template>
                    <template #right>
                        <KnFabButton icon="fas fa-plus" @click="showForm" data-test="open-form-button"></KnFabButton>
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
            </div>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import axios from 'axios'

export default defineComponent({
    name: 'target-definition',
    components: { KnFabButton },
    data() {
        return {
            metadataList: [],
            loading: false
        }
    },
    created() {
        this.loadAllMetadata()
    },
    methods: {
        async loadAllMetadata() {
            this.loading = true
            this.metadataList = []
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpiee/listTarget')
                .then((response) => {
                    console.log(response)
                })
                .finally(() => (this.loading = false))
        },
        showForm() {
            console.log('showForm method')
        }
    }
})
</script>
