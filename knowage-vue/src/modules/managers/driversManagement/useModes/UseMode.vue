<template>
    <Card style="width:100%" class="p-m-2">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    <span>{{ $t('managers.driversManagement.useModes.title') }}</span>
                </template>
                <template #right>
                    <Button :label="$t('managers.driversManagement.add')" class="p-button-text p-button-rounded p-button-plain" :disabled="disableActionButton" @click="showForm" data-test="add-action-button" />
                    <Button label="Save" class="p-button-text p-button-rounded p-button-plain" @click="saveUsemode" />
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
                                <Badge :value="slotProps.option.numberOfErrors" class="p-ml-2" severity="danger" v-if="slotProps.option.numberOfErrors > 0"></Badge>
                                <div class="kn-list-item-text" v-tooltip.top="slotProps.option.description">
                                    <span>{{ slotProps.option.label }}</span>
                                    <span class="kn-list-item-text-secondary">{{ slotProps.option.name }}</span>
                                </div>
                                <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteModeConfirm(slotProps.option)" data-test="delete-button" />
                            </div>
                        </template>
                    </Listbox>
                </div>
                <div class="p-col-8 p-sm-8 p-md-9 p-p-0">
                    <UseModeDetail :selectedMode="selectedUseMode" :selectionTypes="selectionTypes" :roles="roles" :disabledRoles="disabledRoles" :layers="layers" :isDate="isDate"></UseModeDetail>
                </div>
            </div>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import Badge from 'primevue/badge'
import driversManagemenDetailtDescriptor from '../DriversManagementDetailDescriptor.json'
import Listbox from 'primevue/listbox'
import UseModeDetail from './UseModeDetail.vue'
import Tooltip from 'primevue/tooltip'
//import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
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
        selectionTypes: {
            type: Array,
            requierd: true
        },
        layers: {
            type: Array,
            requierd: true
        },
        isDate: {
            type: Boolean,
            requierd: true
        }
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
                this.selectedUseMode = { useID: -1 }
                this.modes.push(this.selectedUseMode)
            }
            this.disabledRoles = []
            this.modes.forEach((mode) => {
                if (mode != this.selectedUseMode) {
                    this.disabledRoles = this.disabledRoles.concat(mode.associatedRoles)
                }
            })
            console.log('Disabled:', this.disabledRoles)
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
            console.log('id useMode', useMode)
            if (useMode.useID != -1) {
                await axios
                    .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers/modes/' + useMode.useID)
                    .then(() => {
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.deleteTitle'),
                            msg: this.$t('common.toast.deleteSuccess')
                        })
                        this.modes.splice(this.modes.indexOf(useMode), 1)
                    })
                    .catch((error) => {
                        this.$store.commit('setError', {
                            title: this.$t('managers.driversManagement.deleteError'),
                            msg: error.message
                        })
                    })
            } else this.modes.splice(this.modes.indexOf(useMode))
            if (this.selectedUseMode === useMode) {
                this.selectedUseMode.useID = null
            }
            console.log('id useMode', useMode)
        },
        saveUsemode() {
            console.log('UseModes:', this.modes)
        }
    }
})
</script>
