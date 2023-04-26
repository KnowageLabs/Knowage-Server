<template>
    <Card class="p-col-12 kn-card kn-card-layout kn-tab-card">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    <span>{{ $t('managers.driversManagement.useModes.title') }}</span>
                </template>
                <template #end>
                    <Button :label="$t('managers.driversManagement.add')" class="kn-button p-button-text" :disabled="disableActionButton" data-test="add-action-button" @click="showForm" />
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div class="p-grid p-m-0 p-col-12 p-p-0 kn-height-full">
                <div class="p-col-4 p-sm-4 p-md-3 p-p-0 kn-height-full">
                    <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />
                    <Listbox v-if="!loading" class="kn-list kn-height-full" :options="modes" option-label="label" data-test="usemodes-list" @change="showForm">
                        <template #empty>{{ $t('common.info.noDataFound') }}</template>
                        <template #option="slotProps">
                            <div class="kn-list-item" >
                                <Badge v-if="slotProps.option.numberOfErrors > 0 || slotProps.option.associatedRoles.length === 0" value="!" class="p-ml-2" severity="danger"></Badge>
                                <div v-tooltip.top="slotProps.option.description" class="kn-list-item-text">
                                    <span data-test="list-item">{{ slotProps.option.label }}</span>
                                    <span class="kn-list-item-text-secondary">{{ slotProps.option.name }}</span>
                                </div>
                                <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" data-test="delete-button" @click.stop="deleteModeConfirm(slotProps.option)" />
                            </div>
                        </template>
                    </Listbox>
                </div>
                <div class="p-col-8 p-sm-8 p-md-9 p-p-0">
                    <UseModeDetail data-test="modes-form" :selected-mode="selectedUseMode" :selection-types="selectionTypes" :roles="roles" :constraints="constraints" :lovs="lovs" :disabled-roles="disabledRoles" :layers="layers" :is-date="isDate" :show-map-driver="showMapDriver"></UseModeDetail>
                </div>
            </div>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import Badge from 'primevue/badge'
import driversManagemenDetailtDescriptor from '../DriversManagementDetailDescriptor.json'
import Listbox from 'primevue/listbox'
import UseModeDetail from './DriversManagementUseModeDetail.vue'
import Tooltip from 'primevue/tooltip'
import mainStore from '../../../../App.store'

export default defineComponent({
    name: 'use-mode-card',
    components: { Listbox, UseModeDetail, Badge },
    directives: {
        tooltip: Tooltip
    },
    props: {
        propModes: {
            type: Array,
            required: false
        },
        roles: {
            type: Array,
            requierd: true
        },
        constraints: {
            type: Array,
            requierd: true
        },
        selectionTypes: {
            type: Array,
            requierd: true
        },
        layers: {
            type: Array,
            requierd: true
        },
        lovs: {
            type: Array,
            requierd: true
        },
        isDate: {
            type: Boolean,
            requierd: true
        },
        showMapDriver: {
            type: Boolean,
            requierd: true
        }
    },
      setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            driversManagemenDetailtDescriptor,
            selectedUseMode: {} as any,
            modes: [] as any[],
            disabledRoles: [] as any[]
        }
    },
    watch: {
        propModes() {
            //this.v$.$reset()
            this.modes = this.propModes as any
            this.selectedUseMode = {}
        }
    },
    mounted() {
        if (this.propModes) {
            this.modes = this.propModes as any
        }
    },
    methods: {
        showForm(event: any) {
            if (event.value) {
                this.selectedUseMode = event.value
            } else {
                this.selectedUseMode = { useID: -1, idLov: null, defaultFormula: null, idLovForMax: null, idLovForDefault: null, associatedRoles: [], associatedChecks: [] }
                this.modes.push(this.selectedUseMode)
            }
            this.disabledRoles = []
            this.modes.forEach((mode) => {
                if (mode != this.selectedUseMode) {
                    this.disabledRoles = this.disabledRoles.concat(mode.associatedRoles)
                }
            })
        },
        deleteModeConfirm(useMode: any) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteMode(useMode)
            })
        },
        async deleteMode(useMode: any) {
            if (useMode.useID != -1) {
                await this.$http
                    .delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers/modes/' + useMode.useID)
                    .then(() => {
                        this.store.setInfo({
                            title: this.$t('common.toast.deleteTitle'),
                            msg: this.$t('common.toast.deleteSuccess')
                        })
                        this.modes.splice(this.modes.indexOf(useMode), 1)
                    })
                    .catch((error) => {
                        this.store.setError({
                            title: this.$t('managers.driversManagement.deleteError'),
                            msg: error.message
                        })
                    })
            } else this.modes.splice(this.modes.indexOf(useMode))
            if (this.selectedUseMode === useMode) {
                this.selectedUseMode.useID = null
            }
        }
    }
})
</script>
