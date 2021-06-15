<template>
    <Dialog class="kn-dialog--toolbar--primary" v-bind:visible="visibility" footer="footer" :closable="false" modal>
        <template #header>
            {{ $t('licenceDialog.title') + ' - MyHostName:' + ' TO DO' }}
        </template>
        <TabView class="knTab kn-tab">
            <TabPanel>
                <LicenceTab :licenses="licenses['DESKTOP-53JSLPH']"></LicenceTab>
            </TabPanel>
        </TabView>
        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.ok') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iHost, iLicense } from './License'
import Dialog from 'primevue/dialog'
import axios from 'axios'
import LicenceTab from './LicenceTab.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'license-dialog',
    components: { Dialog, LicenceTab, TabView, TabPanel },
    data() {
        return {
            hosts: [] as iHost[],
            licenses: {} as { [key: string]: iLicense[] }
        }
    },
    async created() {
        await this.loadLicenses()
        console.log('HOSTS: ', this.hosts)
        console.log('LICENSES: ', this.licenses)
    },
    props: {
        visibility: Boolean
    },
    emits: ['update:visibility'],
    methods: {
        closeDialog() {
            this.$emit('update:visibility', false)
        },
        async loadLicenses() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/license').then((response) => {
                this.hosts = response.data.hosts
                this.licenses = response.data.licenses
            })
        }
    }
})
</script>

<style scoped lang="scss">
.newsDialog {
    min-width: 800px;
    max-width: 1200px;
    width: 800px;
}
</style>
