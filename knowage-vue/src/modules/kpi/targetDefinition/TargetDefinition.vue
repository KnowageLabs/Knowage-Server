<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('kpi.targetDefinition.title') }}
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
                                <span class="kn-list-item-text-secondary">{{ formatDate(slotProps.option.startValidity) }} - {{ formatDate(slotProps.option.endValidity) }}</span>
                            </div>
                            <Button icon="far fa-copy" class="p-button-text p-button-rounded p-button-plain" @click.stop="cloneTargetConfirm(slotProps.option.id)" data-test="clone-button" />
                            <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteTargetConfirm(slotProps.option.id)" data-test="delete-button" />
                        </div>
                    </template>
                </Listbox>
            </div>
            <div class="kn-list--column p-col-8 p-sm-8 p-md-9 p-p-0">
                <TargetDefinitionDetail :model="selectedTarget" v-if="formVisible" @close="closeForm" @touched="touched = true"></TargetDefinitionDetail>
            </div>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iTargetDefinition } from './TargetDefinition'
import { formatDate } from '@/helpers/commons/localeHelper'
import targetDefinitionDecriptor from './TargetDefinitionDescriptor.json'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import axios from 'axios'
import TargetDefinitionDetail from './TargetDefinitionDetail.vue'

export default defineComponent({
    name: 'target-definition',
    components: { KnFabButton, Listbox, TargetDefinitionDetail },
    data() {
        return {
            targetList: [] as iTargetDefinition[],
            selectedTarget: {} as iTargetDefinition,
            loading: false,
            formVisible: false,
            touched: false,
            targetDefinitionDecriptor: targetDefinitionDecriptor,
            formatDate: formatDate
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
                .then((response) =>
                    response.data.map((target: any) => {
                        this.targetList.push({
                            id: target.id,
                            name: target.name,
                            startValidity: new Date(target.startValidity),
                            endValidity: new Date(target.endValidity),
                            author: target.author,
                            values: target.values,
                            category: target.category
                        })
                    })
                )
                .finally(() => (this.loading = false))
        },
        showForm(target: any) {
            if (!this.touched) {
                this.setSelectedTarget(target)
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.setSelectedTarget(target)
                    }
                })
            }
        },
        setSelectedTarget(target: any) {
            if (target) {
                this.selectedTarget = target.value
            }
            this.formVisible = true
        },
        deleteTargetConfirm(targetId: number) {
            console.log('Delete', targetId)
        },
        cloneTargetConfirm(targetId: number) {
            console.log('Clone', targetId)
        },
        closeForm() {
            this.formVisible = false
        }
    }
})
</script>
<style lang="scss" scoped>
.kn-list-column {
    border-right: 1px solid #ccc;
}

.list-header {
    font-weight: bold;
}
</style>
