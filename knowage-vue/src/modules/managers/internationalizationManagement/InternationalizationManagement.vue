<template>
    <TabView @tab-click="logEvent">
        <TabPanel v-for="language in languages" :key="language">
            <template #header>
                {{ $t(`language.${language.locale}`) }}
                <span v-if="language.isDefault">(DEFAULT)</span>
            </template>
            {{ language }}
        </TabPanel>
    </TabView>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
// import Column from 'primevue/column'
// import DataTable from 'primevue/datatable'

interface Language {
    locale: string
    isDefault: boolean | false
}

export default defineComponent({
    name: 'internationalization-management',
    components: {
        TabView,
        TabPanel
        // Column,
        // DataTable
    },
    data() {
        return {
            loading: false,
            languages: Array<Language>(),
            currentUser: {} as any,
            messages: [] as any,
            defaultLangMessages: [] as any
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
                        var isDefault = false
                        if (languagesArray[idx] === this.$i18n.locale) {
                            isDefault = true
                        }
                        this.languages.push({ locale: languagesArray[idx], isDefault: isDefault })
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
            console.log('IM CLICKED', event)
        }
    }
})
</script>
