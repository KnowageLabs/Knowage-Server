<template>
    <TabView @tab-click="switchTabConfirm($event.index)" lazy data-test="tab-view">
        <TabPanel v-for="language in languages" :key="language">
            <template #header>
                {{ language.language }}
                <span v-if="language.defaultLanguage">{{ this.$t('managers.internationalizationManagement.defaultLanguage') }}</span>
            </template>
            <div class="p-fluid card">
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />

                <!-- da pitamo dal ovo zele, ovo sam stavio umesto dirty dialoga na tab, posto ne postoji nacin da blokiram promenu taba kada se klikne na njega -->
                <Message v-show="dirty">You have unsaved changes</Message>

                <DataTable v-if="!loading" editMode="cell" :value="messages" :scrollable="true" scrollHeight="40vh" :loading="loading" :rows="15" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack" breakpoint="960px" v-model:filters="filters" data-test="messages-table">
                    <template #header class="p-fluid">
                        <div class="table-header">
                            <div class="p-field-checkbox">
                                <Checkbox id="findEmptyFields" :binary="true" v-model="showOnlyEmptyFields" @change="filterEmptyMessages" data-test="checkbox" />
                                <label for="findEmptyFields">{{ this.$t('managers.internationalizationManagement.showBlankMessages') }}</label>
                            </div>
                            <span class="p-input-icon-left">
                                <i class="pi pi-search" />
                                <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                            </span>
                        </div>
                    </template>
                    <template #empty>
                        {{ $t('common.info.noDataFound') }}
                    </template>
                    <template #filter="{ filterModel }">
                        <InputText type="text" v-model="filterModel.value" class="p-column-filter" />
                    </template>

                    <Column>
                        <template #body="slotProps">
                            <i class="pi pi-flag" v-if="slotProps.data['dirty']"></i>
                        </template>
                    </Column>

                    <Column v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true">
                        <template #editor="slotProps">
                            <InputText v-model="slotProps.data[slotProps.column.props.field]" v-if="!col.disabled" @input="atFieldChange(slotProps)" />
                            <span id="disabledMessageField" v-if="col.disabled">{{ slotProps.data[slotProps.column.props.field] }}</span>
                        </template>
                    </Column>

                    <Column>
                        <template #header>
                            <Button v-if="language.defaultLanguage" :label="this.$t('managers.internationalizationManagement.table.addLabel')" class="p-button-link" @click="addEmptyLabel" />
                        </template>
                        <template #body="slotProps">
                            <Button icon="pi pi-save" class="p-button-link" @click="saveLabel(language, slotProps.data)" />
                            <Button icon="pi pi-trash" class="p-button-link" @click="deleteLabelConfirm(language, slotProps.data)" />
                        </template>
                    </Column>
                </DataTable>
            </div>
        </TabPanel>
    </TabView>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import intDescriptor from './InternationalizationManagementDescriptor.json'
import axios from 'axios'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Checkbox from 'primevue/checkbox'
import Button from 'primevue/button'
import Message from 'primevue/message'

export default defineComponent({
    name: 'internationalization-management',
    components: {
        TabView,
        TabPanel,
        Column,
        DataTable,
        Checkbox,
        Button,
        Message
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
            languages: intDescriptor.languages,
            defaultLanguage: {} as any,
            selectedLanguage: {} as any,
            messages: [] as any,
            allMessages: [] as any,
            defaultLangMessages: [] as any,
            showOnlyEmptyFields: false,
            dirty: false,
            filters: {
                global: [filterDefault]
            } as Object
        }
    },
    async created() {
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
            //stavi defaultni jezik kao prvi tab
            this.languages.unshift(this.languages.splice(defaultLanguageIndex, 1)[0])
            this.selectLanguage(0)
        },

        //dodaj novi red u tabelu
        addEmptyLabel() {
            var tempMessage = {
                language: '',
                label: '',
                message: ''
            }
            this.messages.unshift(tempMessage)
        },

        //selektuj jezik kada kliknes na tab
        selectLanguage(index) {
            var selectedTab = this.languages[index]
            this.selectedLanguage = this.languages[index]
            this.getMessages(selectedTab)
        },

        //resenje za dirty, ako uspem da disablujem promenu taba na klik
        switchTabConfirm(index) {
            if (!this.dirty) {
                this.selectLanguage(index)
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.dirty = false
                        this.selectLanguage(index)
                    }
                })
            }
        },

        /*  TODO: (Code review) - MEtoda ima veliku kompleksnost, probati da se razbije na vise metoda. */
        //prikazi poruke u datatable
        getMessages(selectedTab) {
            this.messages = []
            this.loading = true
            return axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/i18nMessages/internationalization/?currLanguage=' + selectedTab.languageTag)
                .then((response) => {
                    //For Default Language
                    if (selectedTab.defaultLanguage) {
                        //If database is empty show one row of input fields
                        if (response.data.length == 0) {
                            this.addEmptyLabel()
                        } else {
                            /* TODO: (Code review)  - Razmotriti da li ova dve promenljive trebaju da imaju referancu na isti niz.   */
                            this.defaultLangMessages = response.data
                            // angular.copy(defaultLangMessages, messages) dal je ovo tacno?
                            this.messages = response.data
                        }
                        //For other languages
                    } else {
                        //If there are some messages in database
                        if (response.data.length != 0) {
                            this.defaultLangMessages.forEach((defMess) => {
                                // searching if default message was translated into current language
                                /*  TODO: (Code review) - OVaj deo na dole nije dobar. Zasto raditi filter i uzeti onda prvi objekat kada postoji find i find index metoda   */
                                var translatedMessageArray = response.data.filter((item) => {
                                    return item.label == defMess.label
                                })

                                if (translatedMessageArray[0]) {
                                    // in case default message was translated into current language, we add the default translation message as a reference for translators
                                    translatedMessageArray[0].defaultMessageCode = defMess.message
                                    this.messages.push(translatedMessageArray[0])
                                } else {
                                    // in case default message was not translated into current language, we add an empty translation message
                                    var message = {
                                        language: selectedTab.languageTag,
                                        label: defMess.label,
                                        defaultMessageCode: defMess.message,
                                        message: ''
                                    }
                                    this.messages.push(message)
                                }
                            })
                        } else {
                            //If there are no messages in database, take Label and Message Code from Default one
                            this.defaultLangMessages.forEach((defMess) => {
                                var newMess = {} as any
                                newMess.language = selectedTab.languageTag
                                newMess.label = defMess.label
                                newMess.defaultMessageCode = defMess.message
                                newMess.message = ''
                                this.messages.push(newMess)
                            })
                        }
                    }
                    this.allMessages = [...this.messages]
                })
                .finally(() => (this.loading = false))
        },

        saveOrUpdateMessage(url, toSave, langObj) {
            if (toSave.id) {
                delete toSave.defaultMessageCode

                return axios.put(url, toSave)
            } else {
                if (toSave.defaultMessageCode) delete toSave.defaultMessageCode
                toSave.language = langObj.languageTag
                return axios.post(url, toSave)
            }
        },
        /*  TODO: (Code review) -- Prebaciti parametre za title i msg u translation files  */
        saveLabel(langObj, message) {
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/i18nMessages'
            var toSave = { ...message }
            delete toSave.dirty
            this.saveOrUpdateMessage(url, toSave, langObj).then((response) => {
                if (response.data.errors) {
                    this.$store.commit('setError', { title: 'error', msg: response.data.errors })
                } else {
                    this.$store.commit('setInfo', { title: 'ok', msg: 'ok' })
                }
                if (langObj.defaultLanguage) {
                    this.getMessages(langObj)
                }
            })
            this.dirty = false
        },

        //nisam mogao vise da uprostim od ovoga zbog razlicitih poruka, verovatno moze jos
        deleteLabelConfirm(langObj, message) {
            if (message.id) {
                let url = ''
                if (message.defaultMessageCode) {
                    url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/i18nMessages/'
                    this.$confirm.require({
                        message: this.$t('managers.internationalizationManagement.delete.deleteMessage'),
                        header: this.$t('managers.internationalizationManagement.delete.deleteMessageTitle'),
                        icon: 'pi pi-exclamation-triangle',
                        accept: () => this.deleteLabel(url, message.id, langObj)
                    })
                } else {
                    url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/i18nMessages/deletedefault/'
                    this.$confirm.require({
                        message: this.$t('managers.internationalizationManagement.delete.deleteDefault'),
                        header: this.$t('managers.internationalizationManagement.delete.deleteDefaultTitle'),

                        icon: 'pi pi-exclamation-triangle',
                        accept: () => this.deleteLabel(url, message.id, langObj)
                    })
                }
            } else {
                this.$store.commit('setError', { title: this.$t('managers.internationalizationManagement.delete.deleteDefaultTitle'), msg: this.$t('managers.internationalizationManagement.delete.cantDelete') })
            }
        },

        async deleteLabel(url, id, langObj) {
            await axios.delete(url + id).then((response) => {
                if (response.data.errors) {
                    this.$store.commit('setError', { title: 'Error', msg: response.data.errors })
                } else {
                    this.$store.commit('setInfo', { title: this.$t('common.toast.deleteTitle'), msg: this.$t('common.toast.deleteSuccess') })
                    this.getMessages(langObj)
                }
            })
        }
    }
})
</script>
