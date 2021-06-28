<template>
    <TabView @tab-click="logEvent">
        <TabPanel v-for="language in languages" :key="language">
            <template #header>
                <!-- {{ $t(`language.${language.languageTag}`) }} -->
                {{ language.languageTag }}
                <span v-if="language.defaultLanguage">(DEFAULT)</span>
            </template>
            language: {{ language }}
            <br />
            messages: {{ messages }}
            <br />
            defaultLangMessages: {{ defaultLangMessages }}

            <div class="p-fluid">
                <div class="card">
                    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                    <DataTable v-if="!loading" editMode="cell" :value="messages" :scrollable="true" scrollHeight="40vh" :loading="loading" :rows="15" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack" breakpoint="960px" v-model:filters="filters">
                        <template #header>
                            <div class="table-header">
                                <span class="p-input-icon-left">
                                    <i class="pi pi-search" />
                                    <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" badge="0" />
                                </span>
                            </div>
                        </template>
                        <template #empty>
                            {{ $t('common.info.noDataFound') }}
                        </template>
                        <template #filter="{ filterModel }">
                            <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText>
                        </template>

                        <Column field="label" header="Label" :sortable="true">
                            <template #editor="slotProps">
                                <InputText v-model="slotProps.data[slotProps.column.props.field]" />
                            </template>
                        </Column>
                        <Column field="defaultMessageCode" header="Default Message Code" :sortable="true">
                            <template #editor="slotProps">
                                <InputText v-model="slotProps.data[slotProps.column.props.field]" />
                            </template>
                        </Column>
                        <Column field="message" header="Message Code" :sortable="true">
                            <template #editor="slotProps">
                                <InputText v-model="slotProps.data[slotProps.column.props.field]" />
                            </template>
                        </Column>
                        <Column @rowClick="false">
                            <template #body>
                                <Button icon="pi pi-save" class="p-button-link" />
                                <Button icon="pi pi-trash" class="p-button-link" />
                            </template>
                        </Column>
                    </DataTable>
                </div>
            </div>
        </TabPanel>
    </TabView>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import axios from 'axios'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'

interface Language {
    languageTag: string
    defaultLanguage: boolean | false
}

export default defineComponent({
    name: 'internationalization-management',
    components: {
        TabView,
        TabPanel,
        Column,
        DataTable
    },
    data() {
        return {
            loading: false,
            currentUser: {} as any,
            languages: Array<Language>(),
            messages: [] as any,
            defaultLangMessages: [] as any,
            filters: {
                global: [filterDefault]
            } as Object
        }
    },
    async created() {
        this.getLicences()
        this.getCurrentUser()
    },

    methods: {
        async getLicences() {
            return axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/languages`)
                .then((response) => {
                    console.log('response.data: ', response.data)
                    let languagesArray = response.data.sort()
                    for (var idx in languagesArray) {
                        var defaultLanguage = false
                        if (languagesArray[idx] === this.$i18n.locale) {
                            defaultLanguage = true
                        }
                        this.languages.push({
                            languageTag: languagesArray[idx],
                            defaultLanguage: defaultLanguage
                        })
                    }
                })
                .finally(() => ((this.loading = false), console.log('after sorting', this.languages)))
        },
        async getCurrentUser() {
            return axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/currentuser`)
                .then((response) => {
                    this.currentUser = response.data
                    console.log(this.currentUser)
                })
                .finally(() => (this.loading = false))
        },
        logEvent(event) {
            // console.log('IM CLICKED', event.index)
            // console.log(this.languages[event.index])
            var selectedTab = this.languages[event.index]
            this.getMessages(selectedTab)
        },
        getMessages(selectedTab) {
            var currLanguage = selectedTab.languageTag.replace('_', '-')
            this.messages = []
            this.loading = true
            return (
                axios
                    // sbiModule_restServices.promiseGet("2.0/i18nMessages", "internationalization/?currLanguage="+selectedTab.languageTag)
                    .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/i18nMessages/internationalization/?currLanguage=' + currLanguage)
                    .then((response) => {
                        // var emptyMessage = { value: false, originalMessages: [] }
                        //For Default Language
                        if (selectedTab.defaultLanguage) {
                            //If database is empty show one row of input fields
                            if (response.data.length == 0) {
                                this.messages = [
                                    {
                                        language: '',
                                        label: '',
                                        message: ''
                                    }
                                ]
                                this.defaultLangMessages = []
                            } else {
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
                                    var translatedMessageArray = response.data.filter(function(item) {
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
            )
        }
    }
})
</script>
