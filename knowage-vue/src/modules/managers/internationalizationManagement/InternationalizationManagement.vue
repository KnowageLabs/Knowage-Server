<template>
    <TabView @tab-click="selectLanguage">
        <TabPanel v-for="language in languages" :key="language">
            <template #header>
                {{ language.language }}
                <span v-if="language.defaultLanguage">{{ this.$t('managers.internationalizationManagement.defaultLanguage') }}</span>
            </template>
            language: {{ language }}
            <br />
            messages: {{ messages }}
            <br />
            defaultLangMessages: {{ defaultLangMessages }}
            <div class="p-fluid card">
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <DataTable v-if="!loading" editMode="cell" :value="messages" :scrollable="true" scrollHeight="40vh" :loading="loading" :rows="15" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack" breakpoint="960px" v-model:filters="filters">
                    <template #header class="p-fluid">
                        <div class="table-header">
                            <div class="p-field-checkbox">
                                <Checkbox id="findEmptyFields" :binary="true" v-model="emptyMessage.value" />
                                <label for="findEmptyFields">{{ this.$t('managers.internationalizationManagement.showBlankMessages') }}</label>
                            </div>
                            <span class="p-input-icon-left">
                                <i class="pi pi-search" />
                                <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" />
                            </span>
                        </div>
                    </template>
                    <template #empty>
                        {{ $t('common.info.noDataFound') }}
                    </template>
                    <template #filter="{ filterModel }">
                        <InputText type="text" v-model="filterModel.value" class="p-column-filter" />
                    </template>

                    <Column field="label" :header="this.$t('common.label')" :sortable="true">
                        <template #editor="slotProps">
                            <InputText v-model="slotProps.data[slotProps.column.props.field]" />
                        </template>
                    </Column>
                    <Column field="defaultMessageCode" :header="this.$t('managers.internationalizationManagement.table.defaultMessage')" :sortable="true">
                        <template #editor="slotProps">
                            <InputText v-model="slotProps.data[slotProps.column.props.field]" />
                        </template>
                    </Column>
                    <Column field="message" :header="this.$t('managers.internationalizationManagement.table.messageCode')" :sortable="true">
                        <template #editor="slotProps">
                            <InputText v-model="slotProps.data[slotProps.column.props.field]" />
                        </template>
                    </Column>
                    <Column @rowClick="false">
                        <template #header>
                            <Button :label="this.$t('managers.internationalizationManagement.table.addLabel')" class="p-button-link" @click="addEmptyLabel"></Button>
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

    data() {
        return {
            loading: false,
            intDescriptor,
            currentUser: {} as any,
            languages: intDescriptor.languages,
            defaultLanguage: {} as any,
            messages: [] as any,
            emptyMessage: { value: false, originalMessages: [] } as any,
            defaultLangMessages: [] as any,
            filters: {
                global: [filterDefault]
            } as Object
        }
    },
    async created() {
        // this.getLanguages()
        this.setDefaultLanguage()
        this.getMessages(this.defaultLanguage)
    },

    methods: {
        setDefaultLanguage() {
            let defaultLanguageIndex
            console.log('setDefaultLanguage() {this.languages', this.languages)
            for (var language in this.languages) {
                if (this.languages[language].defaultLanguage) {
                    defaultLanguageIndex = language
                    this.defaultLanguage = this.languages[language]
                    console.log('DEFAULT LANGUAGE', this.defaultLanguage)
                }
            }
            //stavi defaultni jezik kao prvi tab
            this.languages.unshift(this.languages.splice(defaultLanguageIndex, 1)[0])
        },

        // ovo je get metod za ubuduce?
        // async getLanguages() {
        //   return axios
        //     .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/languages`)
        //     .then((response) => {
        //       console.log("response.data: ", response.data);
        //       let languagesArray = response.data.sort();
        //       for (var idx in languagesArray) {
        //         var defaultLanguage = false;
        //         if (languagesArray[idx] === this.$i18n.locale) {
        //           defaultLanguage = true;
        //         }
        //         this.languages.push({
        //           languageTag: languagesArray[idx],
        //           defaultLanguage: defaultLanguage,
        //         });
        //       }
        //     })
        //     .finally(
        //       () => ((this.loading = false), console.log("after sorting", this.languages))
        //     );
        // },

        //provaliti sta ovo radi tacno, sa copy i filterom, show only blank fields

        // toggleEmptyMessages(){
        // 		if(this.emptyMessage.value){
        // 			this.emptyMessage.originalMessages = {...this.messages};
        // 			this.messages = $filter('filter')(this.messages, function(value){
        // 				return !value.message
        // 			})
        // 		}else {
        // 			this.messages = angular.copy(this.emptyMessage.originalMessages);
        // 		}

        // 	},

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
        selectLanguage(event) {
            var selectedTab = this.languages[event.index]
            this.getMessages(selectedTab)
        },

        //prikazi poruke u datatable
        getMessages(selectedTab) {
            this.messages = []
            this.loading = true
            return axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/i18nMessages/internationalization/?currLanguage=' + selectedTab.languageTag)
                .then((response) => {
                    this.emptyMessage = { value: false, originalMessages: [] }
                    //For Default Language
                    if (selectedTab.defaultLanguage) {
                        console.log('IS DEFAULT')
                        //If database is empty show one row of input fields
                        if (response.data.length == 0) {
                            this.messages = intDescriptor.defaultMessage
                            this.defaultLangMessages = []
                        } else {
                            this.defaultLangMessages = response.data
                            // angular.copy(defaultLangMessages, messages) dal je ovo tacno?
                            this.messages = response.data
                        }
                        //For other languages
                    } else {
                        console.log('NOT DEFAULT')
                        //If there are some messages in database
                        if (response.data.length != 0) {
                            this.defaultLangMessages.forEach((defMess) => {
                                // searching if default message was translated into current language
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
                })
                .finally(() => (this.loading = false))
        },

        //ovo moze jos da se uprosti
        saveLabel(langObj, message) {
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/i18nMessages'
            var toSave = { ...message }
            if (message.id) {
                delete toSave.defaultMessageCode

                axios.put(url, toSave).then((response) => {
                    if (response.data.errors) {
                        this.$store.commit('setError', { title: 'error', msg: response.data.errors })
                    } else {
                        this.$store.commit('setInfo', { title: 'ok', msg: 'ok' })
                    }
                    if (langObj.defaultLanguage) {
                        this.getMessages(langObj)
                    }
                })
            } else {
                if (toSave.defaultMessageCode) delete toSave.defaultMessageCode
                toSave.language = langObj.languageTag
                axios.post(url, toSave).then((response) => {
                    if (response.data.errors) {
                        this.$store.commit('setError', { title: 'error', msg: response.data.errors })
                    } else {
                        this.$store.commit('setInfo', { title: 'ok', msg: 'ok' })
                    }
                    if (langObj.defaultLanguage) {
                        this.getMessages(langObj)
                    }
                })
            }
        },

        //nisam mogao vise da uprostim od ovoga, verovatno moze jos
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
