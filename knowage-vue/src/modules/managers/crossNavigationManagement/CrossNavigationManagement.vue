<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="p-col-4 p-sm-4 p-md-3 p-p-0 kn-page">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('managers.widgetGallery.title') }}
                    </template>
                    <template #right>
                        <KnFabButton icon="fas fa-plus" @click="toggleAdd" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
                <KnListBox :options="navigations" :settings="typeDescriptor.knListSettings" @delete="deleteTemplate($event, item)"></KnListBox>
            </div>
            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-router-view">
                <!-- <router-view @saved="savedElement" /> -->
            </div>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'
// import KnHint from '@/components/UI/KnHint.vue'
// import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'metadata-management',
    components: { KnFabButton, KnListBox },
    data() {
        return {
            navigations: [] as any,
            loading: false,
            touched: false
        }
    },
    created() {
        this.loadAll()
    },
    methods: {
        async loadAll() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/crossNavigation/listNavigation/')
                .then((response) => (this.navigations = response.data))
                .finally(() => (this.loading = false))
        }
    }
})
</script>
