<template>
    <TabView>
        <TabPanel v-for="language in availableLanguages" :key="language" :header="language">
            <p>{{ language }}</p>
        </TabPanel>
    </TabView>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'internationalization-management',
    components: {
        TabView,
        TabPanel
    },
    data() {
        return {
            loading: false,
            availableLanguages: [] as any,
            currentUser: {} as any
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
                    this.availableLanguages = response.data
                    console.log(this.availableLanguages)
                })
                .finally(() => (this.loading = false))
        },
        async getCurrentUser() {
            return axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/currentuser`)
                .then((response) => {
                    this.currentUser = response.data
                    console.log(this.currentUser)
                })
                .finally(() => (this.loading = false))
        }
    }
})
</script>
