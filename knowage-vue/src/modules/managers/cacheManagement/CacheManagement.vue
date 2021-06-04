<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #left>
                CACHE MANAGEMENT
            </template>
        </Toolbar>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
        <div class="kn-page-content p-grid p-m-0">
            <div class="p-col-6 p-sm-12 p-md-6 p-p-0">
                <RuntimeInformationCard :item="cache"></RuntimeInformationCard>
            </div>
            <div class="p-col-6 p-sm-12 p-md-6 p-p-0">
                FORM
            </div>
            <div class="p-col-12 p-sm-12 p-p-0">
                DATATABLE
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iCache } from './CacheManagement'
import axios from 'axios'
import RuntimeInformationCard from './cards/RuntimeInformationCard/RuntimeInformationCard.vue'

export default defineComponent({
    name: 'cache-management',
    components: {
        RuntimeInformationCard
    },
    data() {
        return {
            cache: {} as iCache,
            loading: false
        }
    },
    async created() {
        await this.loadCache()
        console.log('Cache: ', this.cache)
    },
    methods: {
        async loadCache() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/cacheee')
                .then((response) => (this.cache = response.data))
                .finally(() => (this.loading = false))
        }
    }
})
</script>
