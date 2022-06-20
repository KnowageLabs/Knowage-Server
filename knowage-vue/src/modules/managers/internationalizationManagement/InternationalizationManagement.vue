<template>
    <div class="kn-page">
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
        <TabView @tab-click="switchTabConfirm($event.index)" v-model:activeIndex="activeTab" lazy data-test="tab-view" class="internationalization-management kn-tab kn-page-content">
            <TabPanel v-for="language in languages" :key="language">
                <template #header>
                    {{ language.language }}
                    <span v-if="language.defaultLanguage">{{ $t('managers.internationalizationManagement.defaultLanguage') }}</span>
                </template>

                <DataTable v-if="!loading" editMode="cell" :value="messages" :loading="loading" class="p-datatable kn-table" dataKey="id" responsiveLayout="stack" breakpoint="960px" v-model:filters="filters" data-test="messages-table">
                    <template #header>
                        <div class="table-header p-d-flex">
                            <span class="p-input-icon-left p-mr-3" :style="intDescriptor.headerStyles.searchBoxStyle">
                                <i class="pi pi-search" />
                                <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                            </span>
                            <div class="p-field-checkbox p-mt-4">
                                <Checkbox id="findEmptyFields" :binary="true" v-model="showOnlyEmptyFields" @change="filterEmptyMessages" data-test="checkbox" />
                                <label for="findEmptyFields">{{ $t('managers.internationalizationManagement.showBlankMessages') }}</label>
                            </div>
                        </div>
                    </template>
                    <template #empty>
                        {{ $t('common.info.noDataFound') }}
                    </template>
                    <template #filter="{ filterModel }">
                        <InputText type="text" v-model="filterModel.value" class="p-column-filter" />
                    </template>

                    <Column :headerStyle="intDescriptor.headerStyles.dirtyHeaderStyle">
                        <template #body="slotProps">
                            <i class="pi pi-flag" v-if="slotProps.data['dirty']"></i>
                        </template>
                    </Column>

                    <Column v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true" :class="{ disabledColumn: col.disabled, editableColumn: !col.disabled }">
                        <template #body="slotProps">
                            <InputText v-model="slotProps.data[slotProps.column.props.field]" v-if="!col.disabled" class="kn-material-input p-inputtext-sm p-p-2" @input="atFieldChange(slotProps)" :data-test="'input-field-' + slotProps.data['id']" />
                            <span v-else :class="{ disabledCell: col.disabled, 'kn-disabled-text': col.disabled, editableCell: !col.disabled }">{{ slotProps.data[slotProps.column.props.field] }}</span>
                        </template>
                    </Column>

                    <Column :headerStyle="intDescriptor.headerStyles.buttonsHeaderStyle">
                        <template #header>
                            <Button v-if="language.defaultLanguage" :label="$t('managers.internationalizationManagement.table.addLabel')" class="p-button kn-button--primary" @click="addEmptyLabel" />
                        </template>
                        <template #body="slotProps">
                            <div class="p-d-flex p-jc-center p-ai-center">
                                <Button icon="pi pi-save" class="p-button-link" @click="saveLabel(language, slotProps.data)" v-tooltip.top="$t('common.save')" />
                                <Button v-if="language.defaultLanguage" icon="pi pi-trash" class="p-button-link" @click="deleteLabelConfirm(language, slotProps, true)" v-tooltip.top="$t('common.delete')" />
                                <Button v-if="!language.defaultLanguage" icon="pi pi-times" class="p-button-link" @click="deleteLabelConfirm(language, slotProps, false)" v-tooltip.top="$t('common.cancel')" />
                            </div>
                        </template>
                    </Column>
                </DataTable>
            </TabPanel>
        </TabView>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { iLanguage, iMessage } from './InternationalizationManagement'
import intDescriptor from './InternationalizationManagementDescriptor.json'
import { AxiosResponse } from 'axios'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Checkbox from 'primevue/checkbox'
import Button from 'primevue/button'

export default defineComponent({
    name: 'internationalization-management',
    components: {
        TabView,
        TabPanel,
        Column,
        DataTable,
        Checkbox,
        Button
    },

    computed: {
        columns() {
            if (this.selectedLanguage.defaultLanguage) {
                return intDescriptor.defaultLanguageColumns
            } else {
                return intDescriptor.notDefaultLanguageColumns
            }
        }
    },

    data() {
        return {
            loading: false,
            intDescriptor,
            languages: [] as iLanguage[],
            defaultLanguage: {} as iLanguage,
            selectedLanguage: {} as iLanguage,
            messages: [] as iMessage[],
            allMessages: [] as iMessage[],
            defaultLangMessages: [] as iMessage[],
            showOnlyEmptyFields: false,
            initialShowEmptyFields: false,
            dirty: false,
            activeTab: 0,
            previousActiveTab: -1,
            filters: {
                global: [filterDefault]
            } as Object
        }
    },
    async created() {
        await this.getLanguages()
        this.setDefaultLanguage()
        this.getMessages(this.defaultLanguage)
    },

    methods: {
        filterEmptyMessages() {
            this.messages = this.showOnlyEmptyFields ? [...this.allMessages.filter((message) => !message.message)] : [...this.allMessages]
        },

        atFieldChange(slotProps) {
            slotProps.data.dirty = true
            this.dirty = true
        },

        setDefaultLanguage() {
            let defaultLanguageIndex
            for (var language in this.languages) {
                if (this.languages[language].defaultLanguage) {
                    defaultLanguageIndex = language
                    this.defaultLanguage = this.languages[language]
                }
            }
            this.languages.unshift(this.languages.splice(defaultLanguageIndex, 1)[0])
            this.selectLanguage(0)
        },

        addEmptyLabel() {
            var tempMessage = {
                language: '',
                label: '',
                message: ''
            }
            this.messages.unshift(tempMessage)
        },

        selectLanguage(index) {
            var selectedTab = this.languages[index]
            this.selectedLanguage = this.languages[index]
            this.getMessages(selectedTab)
        },

        async switchTabConfirm(index) {
            if (!this.dirty) {
                this.switchTab(index)
                this.previousActiveTab = this.activeTab
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.switchTab(index)
                        this.dirty = false
                    },
                    reject: () => {
                        this.activeTab = this.previousActiveTab
                    }
                })
            }
        },
        switchTab(index) {
            this.initialShowEmptyFields = this.showOnlyEmptyFields.valueOf()
            this.showOnlyEmptyFields = false
            this.activeTab = index
            this.selectLanguage(index)
        },

        initCheck() {
            if (this.initialShowEmptyFields) {
                this.showOnlyEmptyFields = true
                this.filterEmptyMessages()
            }
        },
        async setDataForDefaultLanguage(response: AxiosResponse<any>) {
            if (response.data.length == 0) {
                this.addEmptyLabel()
            } else {
                await this.setDefaultLanguageValues(response)
                this.initCheck()
            }
        },
        async setDefaultLanguageValues(response: AxiosResponse<any>) {
            this.defaultLangMessages = response.data
            this.messages = response.data
        },

        async setEmptyDatatableData(selectedTab) {
            this.defaultLangMessages.forEach((defMess) => {
                var newMess = {} as any
                newMess.language = selectedTab.languageTag
                newMess.label = defMess.label
                newMess.defaultMessageCode = defMess.message
                newMess.message = ''
                this.messages.push(newMess)
            })
        },

        async checkForMessages(response, selectedTab) {
            if (response.data.length != 0) {
                await this.setFilledDatatableData(response, selectedTab)
            } else {
                await this.setEmptyDatatableData(selectedTab)
            }
            this.initCheck()
        },

        async setFilledDatatableData(response, selectedTab) {
            this.defaultLangMessages.forEach((defMess) => {
                var translatedMessage = response.data.find((item) => {
                    return item.label == defMess.label
                })
                if (translatedMessage) {
                    translatedMessage.defaultMessageCode = defMess.message
                    this.messages.push(translatedMessage)
                } else {
                    var message = {
                        language: selectedTab.languageTag,
                        label: defMess.label,
                        defaultMessageCode: defMess.message,
                        message: ''
                    }
                    this.messages.push(message)
                }
            })
        },

        getMessages(selectedTab) {
            this.messages = []
            this.loading = true
            return this.$http
                .get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/i18nMessages/internationalization/?currLanguage=' + selectedTab.languageTag)
                .then((response: AxiosResponse<any>) => {
                    if (selectedTab.defaultLanguage) {
                        this.setDataForDefaultLanguage(response)
                    } else {
                        this.checkForMessages(response, selectedTab)
                    }
                    this.allMessages = [...this.messages]
                })
                .finally(() => (this.loading = false))
        },

        async getLanguages() {
            this.loading = true
            return this.$http
                .get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/internationalization/languages')
                .then((response: AxiosResponse<any>) => {
                    this.languages = response.data.wrappedObject
                })
                .finally(() => (this.loading = false))
        },

        saveOrUpdateMessage(url, toSave, langObj) {
            if (toSave.id) {
                delete toSave.defaultMessageCode

                return this.$http.put(url, toSave)
            } else {
                if (toSave.defaultMessageCode) delete toSave.defaultMessageCode
                toSave.language = langObj.languageTag
                return this.$http.post(url, toSave)
            }
        },

        saveLabel(langObj, message) {
            let url = import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/i18nMessages'
            var toSave = { ...message } as iMessage
            delete toSave.dirty
            this.saveOrUpdateMessage(url, toSave, langObj).then((response: AxiosResponse<any>) => {
                if (response.data.errors) {
                    this.$store.commit('setError', { msg: response.data.errors })
                } else {
                    this.$store.commit('setInfo', { msg: this.$t('common.toast.updateSuccess') })
                }
                this.getMessages(langObj)
            })
            this.initialShowEmptyFields = false
            this.showOnlyEmptyFields = false
            this.dirty = false
        },

        deleteLabelConfirm(langObj, message, isDefault) {
            let msgToDelete = message.data
            let index = message.index
            if (msgToDelete.id) {
                let url = ''
                if (msgToDelete.defaultMessageCode) {
                    url = import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/i18nMessages/'
                    this.$confirm.require({
                        message: this.$t('managers.internationalizationManagement.delete.deleteMessage'),
                        header: this.$t('managers.internationalizationManagement.delete.deleteMessageTitle'),
                        icon: 'pi pi-exclamation-triangle',
                        accept: () => this.deleteLabel(url, msgToDelete.id, langObj)
                    })
                } else {
                    url = import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/i18nMessages/deletedefault/'
                    this.$confirm.require({
                        message: this.$t('managers.internationalizationManagement.delete.deleteDefault'),
                        header: this.$t('managers.internationalizationManagement.delete.deleteDefaultTitle'),

                        icon: 'pi pi-exclamation-triangle',
                        accept: () => this.deleteLabel(url, msgToDelete.id, langObj)
                    })
                }
            } else {
                isDefault ? this.messages.splice(index, 1) : this.$store.commit('setError', { title: this.$t('managers.internationalizationManagement.delete.deleteDefaultTitle'), msg: this.$t('managers.internationalizationManagement.delete.cantDelete') })
            }
        },

        async deleteLabel(url, id, langObj) {
            await this.$http.delete(url + id).then((response: AxiosResponse<any>) => {
                if (response.data.errors) {
                    this.$store.commit('setError', { title: 'Error', msg: response.data.errors })
                } else {
                    this.$store.commit('setInfo', { title: this.$t('common.toast.deleteTitle'), msg: this.$t('common.toast.deleteSuccess') })
                    this.getMessages(langObj)
                }
            })
            this.initialShowEmptyFields = false
            this.showOnlyEmptyFields = false
        }
    }
})
</script>

<style lang="scss">
.internationalization-management {
    .disabledCell,
    .disabledColumn,
    .disabledEditableField {
        cursor: not-allowed;
    }
    .editableCell,
    .editableColumn {
        cursor: pointer;
    }
    .p-datatable .p-datatable-tbody > tr:hover {
        background-color: var(--kn-color-selected);
    }
}
</style>
