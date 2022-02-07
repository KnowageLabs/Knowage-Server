<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-3 p-sm-3 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('managers.themeManagement.title') }}
                    </template>
                    <template #right>
                        <FabButton icon="fas fa-plus" @click="showForm" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
                <div class="p-p-2">
                    <Dropdown v-model="selectedTheme" :options="availableThemes" placeholder="Select a theme" />
                </div>
            </div>
            <div class="p-col-9 p-sm-9 p-md-9 p-p-4">
                <ThemeManagementExamples></ThemeManagementExamples>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import FabButton from '@/components/UI/KnFabButton.vue'
import ThemeManagementExamples from '@/modules/managers/themeManagement/ThemeManagementExamples.vue'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'theme-management',
    components: { Dropdown, FabButton, ThemeManagementExamples },
    data() {
        return {
            selectedTheme: {} as any,
            availableThemes: [] as any[]
        }
    },
    async mounted() {
        this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `thememanagement`).then((response: AxiosResponse<any>) => {
            this.availableThemes = response.data.themes
        })
    },
    methods: {}
})
</script>
