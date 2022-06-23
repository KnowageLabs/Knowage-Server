<template>
    <Dialog class="kn-dialog--toolbar--primary" :style="licenseDialogDescriptor.card.style" v-bind:visible="visibility" footer="footer" :closable="false" modal>
        <template #header> {{ $t('licenseDialog.title') }} </template>
        <div v-if="!licenses.hosts || licenses.hosts.length === 0">
            <p>{{ $t('licenseDialog.noLicenses') }}</p>
        </div>
        <TabView class="kn-tab">
            <TabPanel v-for="host in licenses.hosts" :key="host.hardwareId">
                <template #header>
                    <span>{{ host.hostName }}</span>
                </template>
                <LicenceTab :licenses="licenses.licenses[host.hostName]" :host="host" :cpunumber="licenses.cpuNumber"></LicenceTab>
            </TabPanel>
        </TabView>
        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.close') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import licenseDialogDescriptor from './LicenseDialogDescriptor.json'
import LicenceTab from './LicenseTab.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import { mapState } from 'pinia'
import mainStore from '../../../../App.store.js'

export default defineComponent({
    name: 'license-dialog',
    components: { Dialog, LicenceTab, TabView, TabPanel },
    data() {
        return {
            cpuNumber: Number,
            licenseDialogDescriptor
        }
    },
    props: {
        visibility: Boolean
    },
    emits: ['update:visibility'],
    methods: {
        closeDialog() {
            this.$emit('update:visibility', false)
        }
    },
    computed: {
        ...mapState(mainStore, {
            user: 'user',
            licenses: 'licenses'
        })
    }
})
</script>
