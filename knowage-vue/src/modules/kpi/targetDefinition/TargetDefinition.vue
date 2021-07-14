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
                        <div class="kn-list-item">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.name }}</span>
                                <span class="kn-list-item-text-secondary">{{ formatDate(slotProps.option.startValidity) }} - {{ formatDate(slotProps.option.endValidity) }}</span>
                            </div>
                            <Button icon="far fa-copy" class="p-button-text p-button-rounded p-button-plain" @click.stop="cloneTargetConfirm(slotProps.option)" data-test="clone-button" />
                            <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteTargetConfirm(slotProps.option.id)" data-test="delete-button" />
                        </div>
                    </template>
                </Listbox>
            </div>
            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-router-view">
                <KnHint :title="'kpi.targetDefinition.title'" :hint="'test'" v-if="showHint" data-test="bm-hint"></KnHint>
                <router-view @close="closeForm" @touched="touched = true" />
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
import KnHint from '@/components/UI/KnHint.vue'
import Listbox from 'primevue/listbox'
import axios from 'axios'

export default defineComponent({
    name: 'target-definition',
    components: { KnFabButton, Listbox, KnHint },
    data() {
        return {
            targetList: [] as iTargetDefinition[],
            selectedTarget: {} as iTargetDefinition,
            loading: false,
            showHint: true,
            formVisible: false,
            touched: false,
            targetDefinitionDecriptor: targetDefinitionDecriptor,
            formatDate: formatDate
        }
    },
    async created() {
        console.log('ROUTE PATH: ', this.$route.path)
        if (this.$route.path !== '/target-definition') {
            this.showHint = false
        }
        await this.loadAllMetadata()
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
            this.showHint = false
            const path = target.value ? `/target-definition/${target.value.id}` : '/target-definition/new-target-definition'
            if (!this.touched) {
                this.$router.push(path)
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.$router.push(path)
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
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteTarget(targetId)
            })
        },
        async deleteTarget(targetId: number) {
            await axios.delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpiee/' + targetId + '/deleteTarget').then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.closeForm()
                this.loadAllMetadata()
            })
        },
        cloneTargetConfirm(target: number) {
            this.$confirm.require({
                header: this.$t('common.toast.cloneConfirmTitle'),
                accept: () => this.showForm(target)
            })
        },
        closeForm() {
            console.log('close')
            this.showHint = true
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
