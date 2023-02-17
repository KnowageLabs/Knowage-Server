<template>
    <Dialog :style="lovsManagementProfileAttributesListDescriptor.dialog.style" :header="$t('managers.lovsManagement.profileAttributes')" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <Listbox
            class="kn-list"
            :options="profileAttributes"
            :list-style="lovsManagementProfileAttributesListDescriptor.listBox.style"
            :filter="true"
            :filter-placeholder="$t('common.search')"
            option-label="name"
            filter-match-mode="contains"
            :filter-fields="lovsManagementProfileAttributesListDescriptor.filterFields"
            :empty-filter-message="$t('common.info.noDataFound')"
            @change="$emit('selected', $event.value.attributeName)"
        >
            <template #empty>{{ $t('common.info.noDataFound') }}</template>
            <template #option="slotProps">
                <div class="kn-list-item">
                    <div class="kn-list-item-text">
                        <span>{{ slotProps.option.attributeName }}</span>
                    </div>
                </div>
            </template></Listbox
        >
        <template #footer>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import Listbox from 'primevue/listbox'
import lovsManagementProfileAttributesListDescriptor from './LovsManagementProfileAttributesListDescriptor.json'

export default defineComponent({
    name: 'lovs-management-info-dialog',
    components: { Dialog, Listbox },
    props: { visible: { type: Boolean }, profileAttributes: { type: Array } },
    emits: ['close', 'selected'],
    data() {
        return {
            lovsManagementProfileAttributesListDescriptor
        }
    },
    created() {},
    methods: {}
})
</script>
