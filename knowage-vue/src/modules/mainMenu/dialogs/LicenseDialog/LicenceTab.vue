<template>
    <div>
        <FabButton icon="fas fa-plus" />
    </div>
    <Listbox class="kn-list--column" :options="licensesList">
        <template #empty>{{ $t('common.info.noDataFound') }}</template>
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
                <Button icon="far fa-trash-alt" class="p-button-link p-button-sm" />
                <Button icon="far fa-trash-alt" class="p-button-link p-button-sm" />
                <Button icon="far fa-trash-alt" class="p-button-link p-button-sm" />
            </div>
        </template>
    </Listbox>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iLicense } from './License'
import Avatar from 'primevue/avatar'
import FabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import licenceTabDescriptor from './LicenceTabDescriptor.json'

export default defineComponent({
    name: 'license-tab',
    components: {
        Avatar,
        FabButton,
        Listbox
    },
    props: {
        licenses: {
            type: Array,
            required: true
        }
    },
    data() {
        return {
            licenceTabDescriptor,
            licensesList: [] as iLicense[]
        }
    },
    watch: {
        licenses() {
            this.loadLicenses()
        }
    },
    created() {
        this.loadLicenses()
    },
    methods: {
        loadLicenses() {
            this.licensesList = this.licenses as iLicense[]
        },
        setLicenseClass(status: string) {
            return status === 'LICENSE_VALID' ? 'valid' : 'invalid'
        },
        licenseText(status: string) {
            return status === 'LICENSE_VALID' ? this.$t('licenseDialog.validLicense') : this.$t('licenseDialog.invalidLicense')
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
</style>
