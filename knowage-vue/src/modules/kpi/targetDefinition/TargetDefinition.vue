<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        Target definition
                    </template>
                    <template #right>
                        <KnFabButton icon="fas fa-plus" @click="showForm" data-test="open-form-button"></KnFabButton>
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <Listbox v-if="!loading" class="kn-list--column" :options="targetList" :filter="true" :filterPlaceholder="$t('common.search')" optionLabel="name" filterMatchMode="contains" :filterFields="targetDefinitionDecriptor.filterFields" emptyFilterMessage="noResults" @change="showForm">
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" data-test="list-item">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.name }}</span>
                                <span class="kn-list-item-text-secondary">{{ slotProps.option.startValidity }} - {{ slotProps.option.endValidity }}</span>
                            </div>
                            <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteTargetConfirm(slotProps.option.id)" data-test="delete-button" />
                            <Button icon="far fa-copy" class="p-button-text p-button-rounded p-button-plain" @click.stop="cloneTargetConfirm(slotProps.option.id)" data-test="clone-button" />
                        </div>
                    </template>
                </Listbox>
            </div>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iTargetDefinition } from './TargetDefinition'
import targetDefinitionDecriptor from './TargetDefinitionDescriptor.json'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import axios from 'axios'

export default defineComponent({
    name: 'target-definition',
    components: { KnFabButton, Listbox },
    data() {
        return {
            targetList: [] as iTargetDefinition[],
            loading: false,
            targetDefinitionDecriptor: targetDefinitionDecriptor
        }
    },
    created() {
        this.loadAllMetadata()
    },
    methods: {
        async loadAllMetadata() {
            this.loading = true
            this.targetList = []
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpiee/listTarget')
                .then((response) => {
                    this.targetList = response.data
                    console.log('response', response.data)
                    console.log('list', this.targetList)
                })
                .finally(() => (this.loading = false))
        },
        showForm() {
            console.log('showForm method')
        },
        deleteTargetConfirm(targetId: number) {
            console.log('Delete', targetId)
        },
        cloneTargetConfirm(targetId: number) {
            console.log('Clone', targetId)
        }
    }
})
</script>
