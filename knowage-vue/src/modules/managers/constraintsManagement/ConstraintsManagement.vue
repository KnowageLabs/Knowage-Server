<template>
    <div class="kn-page-content p-grid p-m-0">
        <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('managers.constraintManagement.title') }}
                </template>
                <template #right>
                    <FabButton icon="fas fa-plus" @click="showForm" data-test="open-form-button" />
                </template>
            </Toolbar>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
            <Listbox
                v-if="!loading"
                class="kn-list--column"
                :options="allCheks"
                optionLabel="label"
                :filter="true"
                :filterPlaceholder="$t('common.search')"
                filterMatchMode="contains"
                :filterFields="constraintManagementDescriptor.filterFields"
                :emptyFilterMessage="$t('common.info.noDataFound')"
                @change="showForm"
                data-test="check-list"
            >
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item" data-test="list-item">
                        <Avatar :icon="constraintManagementDescriptor.iconTypesMap[slotProps.option.predifined].icon" shape="circle" size="medium" />
                        <div class="kn-list-item-text" v-tooltip.top="slotProps.option.description">
                            <span>{{ slotProps.option.label }}</span>
                            <span class="kn-list-item-text-secondary">{{ slotProps.option.name }}</span>
                            <span class="kn-list-item-text-secondary">{{ slotProps.option.valueTypeCd }}</span>
                        </div>
                        <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteConstraintConfirm(slotProps.option.checkId)" v-if="!slotProps.option.predifined" data-test="delete-button" />
                    </div>
                </template>
            </Listbox>
        </div>
        <div class="kn-list--column p-col-8 p-sm-8 p-md-9 p-p-0">
            <KnHint :title="'managers.constraintManagement.title'" :hint="'managers.constraintManagement.hint'" v-if="!formVisible"></KnHint>
            <ConstraintsManagementDetail :selectedConstraint="selectedCheck" :domains="domains" @close="closeForm" @created="handleSave" @touched="touched = true" v-else></ConstraintsManagementDetail>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import FabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import constraintManagementDescriptor from './ConstraintsManagementDescriptor.json'
import Avatar from 'primevue/avatar'
import { iConstraint } from './ConstraintsManagement'
import ConstraintsManagementDetail from './ConstraintsManagementDetail.vue'
import KnHint from '@/components/UI/KnHint.vue'
import Tooltip from 'primevue/tooltip'

export default defineComponent({
    name: 'constraint-management',
    components: {
        FabButton,
        KnHint,
        Listbox,
        Avatar,
        ConstraintsManagementDetail
    },
    directives: {
        tooltip: Tooltip
    },
    data() {
        return {
            loading: false,
            touched: false,
            formVisible: false,
            predefinedChecks: [] as iConstraint[],
            customChecks: [] as iConstraint[],
            allCheks: [] as iConstraint[],
            selectedCheck: {} as iConstraint,
            domains: [] as any,
            constraintManagementDescriptor
        }
    },
    created() {
        this.loadAll()
    },
    methods: {
        async loadAll() {
            this.loading = true
            await this.getAllPredefinedChecks()
            await this.getAllCustomChecks()
            await this.getDomainTypes()
            this.loadCheks()
        },
        async getAllPredefinedChecks() {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/predefinedChecks`).then((response) => {
                this.predefinedChecks = response.data.map((check: any) => {
                    return {
                        checkId: check.checkId,
                        valueTypeId: check.valueTypeId,
                        name: check.name,
                        label: check.label,
                        description: check.description,
                        valueTypeCd: check.valueTypeCd,
                        firstValue: check.firstValue,
                        secondValue: check.secondValue,
                        predifined: true
                    }
                })
            })
        },
        async getAllCustomChecks() {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/customChecks`).then((response) => {
                this.customChecks = response.data.map((check: any) => {
                    return {
                        checkId: check.checkId,
                        valueTypeId: check.valueTypeId,
                        name: check.name,
                        label: check.label,
                        description: check.description,
                        valueTypeCd: check.valueTypeCd,
                        firstValue: check.firstValue,
                        secondValue: check.secondValue,
                        predifined: false
                    }
                })
            })
        },
        loadCheks() {
            this.allCheks = this.customChecks.concat(this.predefinedChecks)
            this.loading = false
        },
        showForm(event: any) {
            if (!this.touched) {
                this.setSelectedCheck(event)
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.setSelectedCheck(event)
                    }
                })
            }
        },
        async getDomainTypes() {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=CHECK`).then((response) => {
                this.domains = response.data
            })
        },
        deleteConstraintConfirm(id: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteConstraint(id)
            })
        },
        async deleteConstraint(id: number) {
            await axios
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/customChecks/' + id)
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    this.loadAll()
                    this.formVisible = false
                })
                .catch((error) => {
                    this.$store.commit('setError', {
                        title: this.$t('managers.constraintManagement.deleteError'),
                        msg: error.message
                    })
                })
        },
        setSelectedCheck(event: any) {
            if (event) {
                this.selectedCheck = event.value
            }
            this.formVisible = true
        },
        handleSave(event: any) {
            this.loadAll()
            this.touched = false
            this.selectedCheck = event
        },
        closeForm() {
            if (!this.touched) {
                this.formVisible = false
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.formVisible = false
                    }
                })
            }
        }
    }
})
</script>
