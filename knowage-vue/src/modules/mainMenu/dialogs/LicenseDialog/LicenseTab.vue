<template>
    <div id="host-info">
        <div id="host-labels">
            <p>{{ $t('licenseDialog.hostName') }}</p>
            <p>{{ $t('licenseDialog.hardwareId') }}</p>
            <p>{{ $t('licenseDialog.numberOfCpu') }}</p>
        </div>
        <div>
            <p>{{ selectedHost.hostName }}</p>
            <p>{{ selectedHost.hardwareId }}</p>
            <p>{{ 'NUMBER OF CPU PLACEHOLDER' }}</p>
        </div>
    </div>
    <Toolbar class="kn-toolbar p-mb-2">
        <template #right>
            <FabButton icon="fas fa-plus" :style="licenseDialogDescriptor.fabButton.style" v-tooltip.top="$t('licenseDialog.dataRequired')" />
        </template>
    </Toolbar>
    <Listbox class="kn-list--column" :style="licenseDialogDescriptor.list.style" :options="licensesList">
        <template #empty>{{ $t('licenseDialog.noLicenses') }}</template>
        <template #option="slotProps">
            <div class="kn-list-item" data-test="list-item">
                <Avatar :image="require(`@/assets/images/licenseImages/${slotProps.option.product}.png`)" size="medium" />
                <div class="kn-list-item-text">
                    <span>{{ slotProps.option.product }}</span>
                    <span class="kn-list-item-text-secondary" :class="setLicenseClass(slotProps.option.status)">{{ licenseText(slotProps.option.status) }}</span>
                </div>
                <div class="kn-list-item-text">
                    <span class="kn-list-item-text-secondary">{{ $t('licenseDialog.licenseId') }}</span>
                    <span>{{ slotProps.option.licenseId }}</span>
                </div>
                <Button icon="pi pi-download" class="p-button-link" v-tooltip.top="$t('licenseDialog.downloadLicense')" @click="downloadLicence(slotProps.option.product)" data-test="download-button" />
                <Button icon="pi pi-pencil" class="p-button-link" v-tooltip.top="$t('licenseDialog.changeLicense')" />
                <Button icon="pi pi-trash" class="p-button-link" v-tooltip.top="$t('licenseDialog.deleteLicense')" />
            </div>
        </template>
    </Listbox>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iLicense, iHost } from './License'
import { downloadDirect } from '@/helpers/commons/fileHelper'
import licenseDialogDescriptor from './LicenseDialogDescriptor.json'
import axios from 'axios'
import FabButton from '@/components/UI/KnFabButton.vue'
import Avatar from 'primevue/avatar'
import Listbox from 'primevue/listbox'
import Tooltip from 'primevue/tooltip'

export default defineComponent({
    name: 'license-tab',
    components: {
        Avatar,
        FabButton,
        Listbox
    },
    directives: {
        tooltip: Tooltip
    },
    props: {
        licenses: {
            type: Array,
            required: true
        },
        host: {
            type: Object,
            required: true
        }
    },
    data() {
        return {
            licenseDialogDescriptor,
            licensesList: [] as iLicense[],
            selectedHost: {} as iHost
        }
    },
    watch: {
        licenses() {
            this.loadLicenses()
        },
        host() {
            this.loadHost()
        }
    },
    created() {
        this.loadLicenses()
        this.loadHost()
    },
    methods: {
        loadLicenses() {
            this.licensesList = this.licenses as iLicense[]
        },
        loadHost() {
            this.selectedHost = { ...this.host } as iHost
        },
        setLicenseClass(status: string) {
            return status === 'LICENSE_VALID' ? 'valid' : 'invalid'
        },
        licenseText(status: string) {
            return status === 'LICENSE_VALID' ? this.$t('licenseDialog.validLicense') : this.$t('licenseDialog.invalidLicense')
        },
        async downloadLicence(productName) {
            console.log(productName)
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/license/download` + `/${this.selectedHost.hostName}/` + `${productName}`, {
                    headers: {
                        Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9'
                    }
                })
                .then(
                    (response) => {
                        if (response.data.errors) {
                            this.$store.commit('setError', { title: this.$t('common.error.downloading'), msg: this.$t('common.error.errorCreatingPackage') })
                        } else {
                            this.$store.commit('setInfo', { title: this.$t('common.toast.success') })
                            var contentDisposition = response.headers['content-disposition']
                            var fileAndExtension = contentDisposition.match(/filename[^;\n=]*=((['"]).*?\2|[^;\n]*)/i)[1]
                            var completeFileName = fileAndExtension.replaceAll('"', '')
                            downloadDirect(response.data, completeFileName, 'application/zip; charset=utf-8')
                        }
                    },
                    (error) => this.$store.commit('setError', { title: this.$t('common.error.downloading'), msg: this.$t(error) })
                )
        }
    }
})
</script>

<style scoped>
.valid {
    color: #4caf50 !important;
}
.invalid {
    color: red !important;
}
#host-info {
    font-size: 0.7rem;
    padding: 0.5rem;
    border: 1px solid rgba(59, 103, 140, 0.1);
    background-color: #eaf0f6;
    margin: 0 auto;
    width: 80%;
    display: flex;
    flex-direction: row;
}
#host-labels {
    flex: 0.7;
    margin-left: 1rem;
}

#host-info p {
    margin: 0;
}
</style>
