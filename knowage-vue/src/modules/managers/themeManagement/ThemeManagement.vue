<template>
    <div class="p-grid p-m-0 kn-theme-management">
        <div class="kn-list--column kn-page p-col-2 p-sm-2 p-md-3 p-p-0">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('managers.themeManagement.title') }}
                </template>
                <template #end>
                    <FabButton icon="fas fa-plus" @click="showForm" />
                </template>
            </Toolbar>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
            <KnListBox :options="availableThemes" :settings="descriptor.knListSettings" @click="selectTheme" @delete.stop="deleteTheme" />
        </div>

        <div class="p-col p-p-0 p-m-0 kn-page">
            <ThemeManagementExamples :properties="selectedTheme.config"></ThemeManagementExamples>
        </div>

        <div class="kn-list--column kn-page p-col-2 p-sm-2 p-md-3 p-p-0" v-if="selectedTheme">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ selectedTheme.themeName }}
                </template>
                <template #end>
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="handleSave" />
                </template>
            </Toolbar>
            <div class="p-p-2 p-mt-2 p-d-flex p-ai-center">
                <span class="p-float-label kn-flex">
                    <InputText id="themeName" class="kn-material-input" type="text" v-model="selectedTheme.themeName" />
                    <label for="themeName" class="kn-material-input-label"> Theme name </label>
                </span>
                <InputSwitch v-model="selectedTheme.active" v-tooltip="'active'"></InputSwitch>
            </div>
            <Divider class="p-my-2" />
            <div class="p-p-2 kn-page-content" v-if="selectedTheme.config">
                <div>
                    <template v-for="(value, key) in descriptor.list" :key="key">
                        <Fieldset :legend="key" :toggleable="true">
                            <div v-for="property in value.properties" :key="property.key">
                                <div class="p-field">
                                    <span class="p-float-label" v-if="property.type === 'text'">
                                        <InputText id="exampleTextInput" class="kn-material-input p-inputtext-sm" type="text" @change="updateModelToSend(property.key)" v-model="selectedTheme.config[property.key]" />
                                        <label for="exampleTextInput" class="kn-material-input-label"> {{ property.label }} </label>
                                    </span>
                                    <span class="p-float-label" v-if="property.type === 'color'">
                                        <InputText id="exampleTextInput" class="kn-material-input p-inputtext-sm" type="text" v-model="selectedTheme.config[property.key]" @change="updateModelToSend(property.key)" />
                                        <input type="color" v-model="selectedTheme.config[property.key]" @change="updateModelToSend(property.key)" />
                                        <label for="exampleTextInput" class="kn-material-input-label"> {{ property.label }} </label>
                                    </span>
                                </div>
                            </div>
                        </Fieldset>
                    </template>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import FabButton from '@/components/UI/KnFabButton.vue'
import ThemeManagementDescriptor from '@/modules/managers/themeManagement/ThemeManagementDescriptor.json'
import ThemeManagementExamples from '@/modules/managers/themeManagement/ThemeManagementExamples.vue'
import themeHelper from '@/helpers/commons/themeHelper'
import Divider from 'primevue/divider'
import Fieldset from 'primevue/fieldset'
import InputSwitch from 'primevue/inputswitch'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'

export default defineComponent({
    name: 'theme-management',
    components: { Divider, FabButton, Fieldset, InputSwitch, KnListBox, ThemeManagementExamples },
    data() {
        return {
            descriptor: ThemeManagementDescriptor,
            currentTheme: {},
            selectedTheme: { config: {} } as any,
            themeToSend: { config: {} } as any,
            availableThemes: [] as any[]
        }
    },
    mounted() {
        this.getCurrentThemeProperties()
        this.getAllThemes()
    },
    methods: {
        getAllThemes() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `thememanagement`).then((response: AxiosResponse<any>) => {
                this.availableThemes = response.data.themes
                if (this.availableThemes.length === 0) {
                    this.availableThemes = [
                        {
                            themeName: 'Default Theme',
                            config: {},
                            active: true
                        }
                    ]
                }
                this.overrideDefaultValues(this.availableThemes.filter((item) => item.active === true)[0])
            })
        },
        getCurrentThemeProperties() {
            for (let k in ThemeManagementDescriptor.list) {
                for (let property of ThemeManagementDescriptor.list[k].properties) {
                    this.currentTheme[property.key] = getComputedStyle(document.documentElement)
                        .getPropertyValue(property.key)
                        .trim()
                }
            }
        },
        async handleSave() {
            await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `thememanagement`, this.themeToSend)
            if (this.selectedTheme.active) {
                this.setActiveTheme(this.selectedTheme)
            }
            this.getAllThemes()
        },
        overrideDefaultValues(newValues) {
            this.selectedTheme.themeName = newValues.themeName
            this.selectedTheme.active = newValues.active
            this.selectedTheme.config = { ...this.currentTheme, ...newValues.config }
        },
        selectTheme(event) {
            this.themeToSend = event.item
            this.overrideDefaultValues(event.item)
        },
        setActiveTheme(theme) {
            this.$store.commit('setTheme', theme)
            themeHelper.setTheme(theme)
        },
        updateModelToSend(key) {
            this.themeToSend.config[key] = this.selectedTheme.config[key]
        }
    }
})
</script>

<style lang="scss">
.kn-theme-management {
    .p-fieldset-content {
        padding: 0;
    }
    .p-float-label {
        display: flex;
        .kn-material-input {
            flex: 1;
        }
    }
}
</style>
