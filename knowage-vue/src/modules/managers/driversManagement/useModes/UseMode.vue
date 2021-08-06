<template>
    <Card style="width:100%" class="p-m-2">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    <span>{{ $t('managers.driversManagement.useModes') }}</span>
                </template>
                <template #right>
                    <Button :label="$t('managers.driversManagement.add')" class="p-button-text p-button-rounded p-button-plain" :disabled="disableActionButton" @click="showForm" data-test="add-action-button" />
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div class="p-grid p-m-0">
                <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                    <Listbox v-if="!loading" class="kn-list" :options="modes" optionLabel="label" @change="showForm">
                        <template #empty>{{ $t('common.info.noDataFound') }}</template>
                        <template #option="slotProps">
                            <div class="kn-list-item" data-test="list-item">
                                <div class="kn-list-item-text">
                                    <span>{{ slotProps.option.name }}</span>
                                    <span class="kn-list-item-text-secondary">{{ slotProps.option.label }}</span>
                                </div>
                                <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteModeConfirm(slotProps.option.id)" data-test="delete-button" />
                            </div>
                        </template>
                    </Listbox>
                </div>
                <div class="p-col-8 p-sm-8 p-md-9 p-p-0">
                    <UseModeDetail :selectedMode="selectedUseMode"></UseModeDetail>
                </div>
            </div>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import driversManagemenDetailtDescriptor from '../DriversManagementDetailDescriptor.json'
import Listbox from 'primevue/listbox'
import UseModeDetail from './UseModeDetail.vue'
//import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
export default defineComponent({
    name: 'use-mode-card',
    components: { Listbox, UseModeDetail },
    props: {
        propModes: {
            type: Array,
            required: false
        }
    },
    data() {
        return {
            driversManagemenDetailtDescriptor,
            selectedUseMode: {} as any,
            modes: []
        }
    },
    watch: {
        propModes() {
            //this.v$.$reset()
            this.modes = this.propModes as any
        }
    },
    mounted() {
        if (this.propModes) {
            this.modes = this.propModes as any
        }
    },
    methods: {
        showForm(event: any) {
            this.setSelectedUseMode(event)
        },
        setSelectedUseMode(event: any) {
            if (event) {
                this.selectedUseMode = event.value
            }
            //this.formVisible = true
        }
    }
})
</script>
