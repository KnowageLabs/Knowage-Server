<template>
    <div class="p-grid p-m-0 kn-theme-management">
        <div class="kn-list--column kn-page p-col-2 p-sm-2 p-md-3 p-p-0">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('managers.themeManagement.title') }}
                </template>
                <template #end>
                    <FabButton icon="fas fa-plus" @click="toggleAdd" />
                    <Menu ref="menu" :model="addMenuItems" :popup="true" style="width: 240px"></Menu>
                </template>
            </Toolbar>
            <KnInputFile label="" :change-function="uploadTheme" accept="application/json,application/zip" :trigger-input="triggerInput" />
            <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" />
            <KnListBox :options="availableThemes" :selected="selectedTheme" :settings="descriptor.knListSettings" @click="selectTheme" @delete.stop="deleteThemeConfirm" />
        </div>

        <div class="p-col p-p-0 p-m-0 kn-page">
            <KnHint v-if="!selectedTheme.themeName" :title="$t('managers.themeManagement.title')" :hint="$t('managers.themeManagement.hint')"></KnHint>
            <ThemeManagementExamples v-else :properties="selectedTheme.config"></ThemeManagementExamples>
        </div>

        <div v-if="selectedTheme.themeName" class="kn-list--column kn-page p-col-2 p-sm-2 p-md-3 p-p-0">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ themeToSend.themeName }}
                </template>
                <template #end>
                    <Button v-if="selectedTheme.id" icon="pi pi-download" class="p-button-text p-button-rounded p-button-plain" @click="downloadTheme" :title="$t('managers.themeManagement.download')" />
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="handleSave" :title="$t('managers.themeManagement.save')" />
                </template>
            </Toolbar>
            <div class="p-p-2 p-mt-2 p-d-flex p-ai-center">
                <span class="p-float-label kn-flex">
                    <InputText id="themeName" v-model="themeToSend.themeName" class="kn-material-input" type="text" />
                    <label for="themeName" class="kn-material-input-label"> Theme name </label>
                </span>
                <InputSwitch v-model="themeToSend.active" v-tooltip="'active'"></InputSwitch>
            </div>
            <Divider class="p-my-2" />
            <div class="p-p-2 kn-page-content">
                <div>
                    <template v-for="(value, key) in themeHelper.descriptor" :key="key">
                        <Fieldset :legend="key" :toggleable="true" :collapsed="true">
                            <div v-for="property in value.properties" :key="property.key">
                                <div class="p-field">
                                    <span v-if="property.type === 'text'" class="p-float-label">
                                        <InputText id="exampleTextInput" v-model="selectedTheme.config[property.key]" class="kn-material-input p-inputtext-sm" type="text" @change="updateModelToSend(property.key)" />
                                        <label for="exampleTextInput" class="kn-material-input-label"> {{ property.label }} </label>
                                    </span>
                                    <span v-if="property.type === 'color'" class="p-float-label">
                                        <InputText id="exampleTextInput" v-model="selectedTheme.config[property.key]" class="kn-material-input p-inputtext-sm" type="text" @change="updateModelToSend(property.key)" />
                                        <input v-model="selectedTheme.config[property.key]" type="color" @change="updateModelToSend(property.key)" />
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
import themeHelper from '@/helpers/themeHelper/themeHelper'
import KnInputFile from '@/components/UI/KnInputFile.vue'
import { downloadDirect } from '@/helpers/commons/fileHelper'
import Divider from 'primevue/divider'
import Menu from 'primevue/menu'
import Fieldset from 'primevue/fieldset'
import InputSwitch from 'primevue/inputswitch'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'
import KnHint from '@/components/UI/KnHint.vue'
import { mapActions, mapState } from 'pinia'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'theme-management',
    components: { Divider, FabButton, Fieldset, InputSwitch, Menu, KnHint, KnInputFile, KnListBox, ThemeManagementExamples },
    data() {
        return {
            descriptor: ThemeManagementDescriptor,
            currentTheme: {},
            selectedTheme: { config: {} } as any,
            themeToSend: { config: {} } as any,
            availableThemes: [] as any[],
            triggerInput: false,
            loading: false,
            themeHelper: new themeHelper(),
            addMenuItems: [
                { label: this.$t('managers.themeManagement.new'), icon: 'fas fa-plus', command: () => this.addTheme() },
                {
                    label: this.$t('managers.themeManagement.import'),
                    icon: 'fas fa-file-import',
                    command: () => {
                        this.triggerInputFile(true)
                    }
                }
            ]
        }
    },
    mounted() {
        this.loading = true
        this.currentTheme = this.themeHelper.getDefaultKnowageTheme()
        this.getAllThemes()
    },
    computed: {
        ...mapState(mainStore, ['defaultTheme'])
    },
    methods: {
        ...mapActions(mainStore, ['setInfo', 'setTheme']),
        triggerInputFile(value) {
            this.triggerInput = value
        },
        addTheme() {
            this.themeToSend = { ...this.descriptor.emptyTheme }
            this.overrideDefaultValues(this.descriptor.emptyTheme)
        },
        toggleAdd(event) {
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.menu.toggle(event)
            this.triggerInputFile(false)
        },
        deleteThemeConfirm(event: any) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteTheme(event)
            })
        },
        async deleteTheme(event) {
            this.loading = true
            await this.$http.delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `thememanagement/${event.item.id}`).then(() => {
                this.setInfo({ title: this.$t('common.toast.deleteTitle'), msg: this.$t('common.toast.deleteSuccess') })

                this.themeToSend = { config: {} }
                this.selectedTheme = { config: {} }

                this.getAllThemes()
            })
            this.loading = false
        },
        async getAllThemes(fullRefresh = true) {
            this.loading = true
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `thememanagement`).then((response: AxiosResponse<any>) => {
                this.availableThemes = response.data

                if (fullRefresh) this.overrideDefaultValues(this.availableThemes.filter((item) => item.active === true)[0])

                if (this.availableThemes.filter((item) => item.active === true).length == 0) {
                    this.setActiveTheme({})
                    this.themeToSend = { config: {} }
                    this.selectedTheme = { config: {} }
                }
            })
            this.loading = false
        },

        async handleSave() {
            await this.$http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `thememanagement`, this.themeToSend).then((response) => {
                this.setInfo({ title: this.$t('common.toast.updateTitle'), msg: this.$t('common.toast.updateSuccess') })
                if (!this.themeToSend.id) {
                    this.themeToSend.id = response.data
                    this.selectedTheme.id = response.data
                }
            })
            await this.getAllThemes(false)
            if (this.themeToSend.active) {
                this.setActiveTheme(this.themeToSend)
            }
            this.loading = false
        },
        overrideDefaultValues(newValues) {
            // no default theme
            if (newValues) {
                this.themeToSend = { ...newValues }
                this.selectedTheme.id = newValues.id
                this.selectedTheme.themeName = newValues.themeName
                this.selectedTheme.active = newValues.active
                this.selectedTheme.config = { ...this.currentTheme, ...newValues.config }
            } else {
                this.setTheme({})
                this.themeHelper.setTheme(this.defaultTheme)
            }
        },
        selectTheme(event) {
            this.overrideDefaultValues(event.item)
        },
        setActiveTheme(theme) {
            const newTheme = { ...this.defaultTheme, ...theme.config }
            this.setTheme(newTheme)
            this.themeHelper.setTheme(newTheme)
        },
        updateModelToSend(key) {
            this.themeToSend.config[key] = this.selectedTheme.config[key]
        },
        uploadTheme(event): void {
            const reader = new FileReader()
            reader.onload = this.onReaderLoad
            reader.readAsText(event.target.files[0])
            this.triggerInputFile(false)
            event.target.value = ''
        },
        onReaderLoad(event) {
            const json = JSON.parse(event.target.result)
            json.active = false
            this.importWidget(json)
        },
        importWidget(json: JSON) {
            this.$http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + 'thememanagement', json).then(() => {
                this.setInfo({ title: this.$t('managers.themeManagement.uploadTheme'), msg: this.$t('managers.themeManagement.themeSuccessfullyUploaded') })

                this.getAllThemes()
            })
        },
        downloadTheme(): void {
            let themeToDownload = { ...this.selectedTheme }
            if (themeToDownload.id) delete themeToDownload.id
            downloadDirect(JSON.stringify(themeToDownload), themeToDownload.themeName, 'application/json')
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
