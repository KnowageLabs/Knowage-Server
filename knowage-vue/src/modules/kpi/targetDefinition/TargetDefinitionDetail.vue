<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" :disabled="buttonDisabled" @click="showCategoryDialog" />
            <Button class="kn-button p-button-text p-button-rounded" icon="pi pi-times" @click="closeTemplate" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-0 p-fluid p-jc-center">
        <div class="p-col-9">
            <target-definition-form :selectedTarget="target" @valueChanged="updateTarget" :vcomp="v$.target"></target-definition-form>
            <apply-target-card :kpi="kpi" @kpiChanged="updateKpi" @showDialog="addKpiDialog"></apply-target-card>
        </div>
    </div>
    <add-kpi-dialog :kpi="filteredKpi" :dialogVisible="kpiDialogVisible" :loadingKpi="loadingAllKpi" @close="closeKpiDialog" @add="addKpi"></add-kpi-dialog>
    <Dialog :header="$t('kpi.targetDefinition.saveTarget')" v-model:visible="categoryDialogVisiable" :modal="true" :closable="true" class="p-fluid kn-dialog--toolbar--primary">
        <div class="p-pt-4">
            <span class="p-float-label">
                <AutoComplete id="category" v-model="target.category" :suggestions="filteredCategory" @complete="searchCategory($event)" field="valueName" />
                <label for="category" class="kn-material-input-label"> {{ $t('kpi.targetDefinition.kpiCategory') }}</label>
            </span>
        </div>
        <template #footer>
            <Button :label="$t('common.save')" icon="pi pi-check" class="kn-button kn-button--primary " @click="handleSubmit" />
        </template>
    </Dialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import { formatDate } from '@/helpers/commons/localeHelper'
import AutoComplete from 'primevue/autocomplete'
import targetDefinitionDetailDecriptor from './TargetDefinitionDetailDescriptor.json'
import targetDefinitionValidationDescriptor from './TargetDefinitionValidationDescriptor.json'
import Dialog from 'primevue/dialog'
import axios from 'axios'
import { iCategory, iTargetDefinition, iValues } from './TargetDefinition'
import useValidate from '@vuelidate/core'
import AddKpiDialog from './AddKpiDialog.vue'
import TargetDefinitionForm from './TargetDefinitionForm.vue'
import ApplyTargetCard from './ApplyTargetCard.vue'

export default defineComponent({
    name: 'target-definition-detail',
    components: {
        Dialog,
        AutoComplete,
        AddKpiDialog,
        TargetDefinitionForm,
        ApplyTargetCard
    },
    props: {
        id: {
            type: String
        },
        clone: {
            type: String
        }
    },
    data() {
        return {
            target: {} as iTargetDefinition,
            formatDate: formatDate,
            targetDefinitionDetailDecriptor: targetDefinitionDetailDecriptor,
            targetDefinitionValidationDescriptor,
            kpi: [] as iValues[],
            filteredKpi: [] as iValues[],
            categories: [] as iCategory[],
            filteredCategory: [] as iCategory[],
            selectedCategory: {} as iCategory,
            loading: false,
            loadingAllKpi: false,
            kpiDialogVisible: false,
            categoryDialogVisiable: false,
            v$: useValidate() as any
        }
    },
    validations() {
        const customValidators: ICustomValidatorMap = {
            'is-after-date': () => {
                return this.target && this.target.startValidity && this.target.endValidity && this.target.startValidity < this.target.endValidity
            }
        }
        return {
            target: createValidations('target', targetDefinitionValidationDescriptor.validations.target, customValidators)
        }
    },
    computed: {
        buttonDisabled(): any {
            return this.v$.$invalid || this.kpi.length < 1
        }
    },
    created() {
        if (this.id) {
            this.loadTarget()
            this.loadCategory()
        }
    },
    watch: {
        async id() {
            await this.checkId()
        },
        async clone() {
            await this.checkId()
        }
    },
    methods: {
        updateTarget(event) {
            this.target[event.fieldName] = event.value
            this.setDirty()
        },
        updateKpi(updatedKPIs) {
            this.kpi = [...updatedKPIs]
            this.setDirty()
        },
        async loadTarget() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpiee/' + this.id + '/loadTarget')
                .then((response) => {
                    this.target = {
                        id: this.clone == 'true' ? null : response.data.id,
                        name: this.clone == 'true' ? 'Copy of ' + response.data.name : response.data.name,
                        startValidity: new Date(response.data.startValidity),
                        endValidity: new Date(response.data.endValidity),
                        author: response.data.author,
                        category: response.data.category
                    }
                    this.kpi = response.data.values.map((val: any) => {
                        return {
                            kpiId: val.kpiId,
                            kpiName: val.kpi.name,
                            kpiVersion: val.kpiVersion,
                            kpiCategory: val.kpi.category?.valueName,
                            kpiDate: new Date(val.kpi.dateCreation),
                            kpiAuthor: val.kpi.author,
                            value: val.value,
                            targetId: val.targetId
                        }
                    })
                })
                .finally(() => (this.loading = false))
        },
        async loadKpi() {
            this.loadingAllKpi = true
            this.filteredKpi = []
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/listKpi')
                .then(
                    (response) =>
                        (this.filteredKpi = response.data
                            .filter((item) => !this.kpi || this.kpi.findIndex((kpi) => kpi.kpiId === item.id) < 0)
                            .map((kpi: any) => {
                                return {
                                    kpiId: kpi.id,
                                    kpiName: kpi.name,
                                    kpiVersion: kpi.version,
                                    kpiCategory: kpi.category?.valueName,
                                    kpiDate: new Date(kpi.dateCreation),
                                    kpiAuthor: kpi.author,
                                    targetId: this.target.id,
                                    value: 0
                                }
                            }))
                )
                .finally(() => (this.loadingAllKpi = false))
        },
        async loadCategory() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/domains/listByCode/KPI_TARGET_CATEGORY').then((response) => (this.categories = response.data))
        },
        searchCategory(event) {
            setTimeout(() => {
                if (!event.query.trim().length) {
                    this.filteredCategory = [...this.categories]
                } else {
                    this.filteredCategory = this.categories.filter((category) => {
                        return category.valueName && category.valueName.toLowerCase().startsWith(event.query.toLowerCase())
                    })
                }
            }, 250)
        },
        showCategoryDialog() {
            if (this.kpi.length < 1) {
                this.$store.commit('setError', {
                    title: this.$t('kpi.targetDefinition.noKpi'),
                    msg: this.$t('kpi.targetDefinition.noKpiMessage')
                })
            } else if (this.v$.$invalid) {
                this.v$.$touch()
            } else {
                this.loadCategory()
                this.categoryDialogVisiable = true
            }
        },
        async handleSubmit() {
            this.categoryDialogVisiable = false
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpiee/saveTarget'

            this.target.values = this.kpi.map((kpi: iValues) => {
                return {
                    kpiId: kpi.kpiId,
                    kpiVersion: kpi.kpiVersion,
                    value: kpi.value,
                    targetId: kpi.targetId
                }
            })
            if (typeof this.target.category !== 'object') {
                const valueCd = this.target.category
                this.target.category = {
                    valueCd: valueCd
                }
            }
            let operation = this.target.id ? 'update' : 'insert'

            await axios.post(url, this.target).then((response) => {
                if (response.data.errors != undefined && response.data.errors.length > 0) {
                    this.categoryDialogVisiable = false
                    this.$store.commit('setError', {
                        title: this.$t('kpi.targetDefinition.savingError'),
                        msg: response.data.errors[0].message
                    })
                } else {
                    this.$store.commit('setInfo', {
                        title: this.$t(this.targetDefinitionDetailDecriptor.operation[operation].toastTitle),
                        msg: this.$t(this.targetDefinitionDetailDecriptor.operation.success)
                    })
                    this.$emit('saved', response.data.id)
                }
            })
        },
        closeTemplate() {
            this.$emit('close')
        },
        setDirty(): void {
            this.$emit('touched')
        },
        deleteKpi(kpiSelected: any) {
            this.kpi.splice(this.kpi.indexOf(kpiSelected), 1)
            this.setDirty()
        },
        addKpiDialog() {
            this.loadKpi()
            this.kpiDialogVisible = true
        },
        closeKpiDialog() {
            this.kpiDialogVisible = false
        },
        addKpi(selectedKpi: iValues[]) {
            this.kpi = [...this.kpi, ...selectedKpi]
            this.kpiDialogVisible = false
            if (selectedKpi.length > 0) {
                this.$store.commit('setInfo', {
                    title: this.$t('kpi.targetDefinition.kpiAddedTitile'),
                    msg: this.$t('kpi.targetDefinition.kpiAddedMessage')
                })
                this.setDirty()
            }
        },
        async checkId() {
            if (this.id) {
                await this.loadTarget()
            } else {
                this.target = {}
                this.kpi = []
            }
            this.v$.$reset()
        }
    }
})
</script>
