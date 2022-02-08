<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-2 p-sm-2 p-md-2 p-p-0">
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
                    <Message severity="info">Info Message Content</Message>
                    <Dropdown v-model="selectedTheme" :options="availableThemes" placeholder="Select a theme" :editable="true" />
                </div>
            </div>
            <div class="p-col-10 p-sm-10 p-md-10 p-p-0 p-m-0 kn-page">
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
import themeHelper from '@/helpers/commons/themeHelper'
import Dropdown from 'primevue/dropdown'
import Message from 'primevue/message'

export default defineComponent({
    name: 'theme-management',
    components: { Dropdown, FabButton, Message, ThemeManagementExamples },
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
    methods: {
        setActiveTheme() {
            this.$store.commit('setTheme', this.selectedTheme)
            themeHelper.setTheme(this.selectedTheme)
        }
    }
})
</script>
