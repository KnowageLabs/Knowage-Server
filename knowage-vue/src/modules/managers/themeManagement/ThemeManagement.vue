<template>
    <div class="p-grid p-m-0 kn-theme-management">
        <div class="kn-list--column kn-page p-col-2 p-sm-2 p-md-2 p-p-0">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('managers.themeManagement.title') }}
                </template>
                <template #right>
                    <FabButton icon="fas fa-plus" @click="showForm" />
                </template>
            </Toolbar>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
            <div class="p-p-2 kn-page-content">
                <Message severity="info">Info Message Content</Message>
                <div class="p-grid">
                    <Dropdown v-model="selectedTheme" :options="availableThemes" class="kn-material-input p-col" placeholder="Select a theme" :editable="true" />
                </div>

                <div>
                    <template v-for="(value, key) in descriptor.list" :key="key">
                        <h3>{{ key }}</h3>
                        <div v-for="property in value.properties" :key="property.key">
                            <div class="p-field">
                                <span class="p-float-label" v-if="property.type === 'text'">
                                    <InputText id="exampleTextInput" class="kn-material-input" type="text" v-model="property.value" />
                                    <label for="exampleTextInput" class="kn-material-input-label"> {{ property.label }} </label>
                                </span>
                                <span class="p-float-label" v-if="property.type === 'color'">
                                    <InputText id="exampleTextInput" class="kn-material-input" type="text" v-model="property.value" />
                                    <ColorPicker v-model="property.value" />
                                    <label for="exampleTextInput" class="kn-material-input-label"> {{ property.label }} </label>
                                </span>
                            </div>
                        </div>
                    </template>
                </div>
            </div>
        </div>
        <div class="p-col-10 p-sm-10 p-md-10 p-p-0 p-m-0 kn-page">
            <ThemeManagementExamples></ThemeManagementExamples>
        </div>
    </div>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import ColorPicker from 'primevue/colorpicker'
import FabButton from '@/components/UI/KnFabButton.vue'
import ThemeManagementDescriptor from '@/modules/managers/themeManagement/ThemeManagementDescriptor.json'
import ThemeManagementExamples from '@/modules/managers/themeManagement/ThemeManagementExamples.vue'
import themeHelper from '@/helpers/commons/themeHelper'
import Dropdown from 'primevue/dropdown'
import Message from 'primevue/message'

export default defineComponent({
    name: 'theme-management',
    components: { ColorPicker, Dropdown, FabButton, Message, ThemeManagementExamples },
    data() {
        return {
            descriptor: ThemeManagementDescriptor,
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

<style lang="scss">
.kn-theme-management {
    .p-float-label {
        display: flex;
        .kn-material-input {
            flex: 1;
        }
    }
}
</style>
