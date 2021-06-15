<template>
    <FabButton icon="fas fa-plus" @click="showForm" data-test="open-form-button" />
    <Listbox class="kn-list--column" :options="licensesList">
        <template #empty>{{ $t('common.info.noDataFound') }}</template>
        <template #option="slotProps">
            <div class="kn-list-item" data-test="list-item">
                <Avatar :image="require('https://www.primefaces.org/wp-content/uploads/2020/05/placeholder.png')" class="p-mr-2" size="xlarge" shape="circle" />
                <div class="kn-list-item-text">
                    <span>{{ slotProps.option.product }}</span>
                    <span class="kn-list-item-text-secondary">{{ slotProps.option.product }}</span>
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
        }
    }
})
</script>
