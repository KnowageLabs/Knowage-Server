<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" :disabled="buttonDisabled" @click="showCategoryDialog" />
            <Button class="kn-button p-button-text p-button-rounded" icon="pi pi-times" @click="closeTemplate" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-0 p-fluid p-jc-center">
        <div class="p-col-9">
            <Card>
                <template #content>
                    <form class="p-fluid p-m-5">
                        <div class="p-field">
                            <span class="p-float-label">
                                <InputText
                                    id="name"
                                    class="kn-material-input"
                                    type="text"
                                    maxLength="100"
                                    v-model.trim="v$.target.name.$model"
                                    :class="{
                                        'p-invalid': v$.target.name.$invalid && v$.target.name.$dirty
                                    }"
                                    @change="setDirty"
                                    @blur="v$.target.name.$touch()"
                                />
                                <label for="name" class="kn-material-input-label">Name * </label>
                            </span>
                            <KnValidationMessages :vComp="v$.target.name" :additionalTranslateParams="{ fieldName: $t('kpi.targetDefinition.name') }"></KnValidationMessages>
                        </div>
                        <div class="kn-flex">
                            <div class="p-d-flex p-jc-between">
                                <div>
                                    <span class="p-float-label">
                                        <Calendar
                                            id="startDate"
                                            class="kn-material-input"
                                            v-model="v$.target.startValidity.$model"
                                            :class="{
                                                'p-invalid': v$.target.startValidity.$invalid && v$.target.startValidity.$dirty
                                            }"
                                            :showIcon="true"
                                            :manualInput="false"
                                            @date-select="setDirty"
                                            @blur="v$.target.startValidity.$touch()"
                                        />
                                        <label for="startDate" class="kn-material-input-label"> {{ $t('kpi.targetDefinition.startDate') }} * </label>
                                    </span>
                                    <KnValidationMessages :vComp="v$.target.startValidity" :additionalTranslateParams="{ fieldName: $t('kpi.targetDefinition.startDate') }"></KnValidationMessages>
                                </div>
                                <div class="p-d-flex">
                                    <div>
                                        <span class="p-float-label">
                                            <Calendar
                                                id="endDate"
                                                class="kn-material-input"
                                                v-model="v$.target.endValidity.$model"
                                                :class="{
                                                    'p-invalid': v$.target.endValidity.$invalid && v$.target.endValidity.$dirty
                                                }"
                                                :showIcon="true"
                                                :manualInput="false"
                                                @date-select="setDirty"
                                                @blur="v$.target.endValidity.$touch()"
                                            />
                                            <label for="endDate" class="kn-material-input-label"> {{ $t('kpi.targetDefinition.endDate') }} * </label>
                                        </span>
                                        <KnValidationMessages :vComp="v$.target.endValidity" :additionalTranslateParams="{ fieldName: $t('kpi.targetDefinition.endDate') }" :specificTranslateKeys="{ is_after_date: 'kpi.targetDefinition.endDateBeforeStart' }"></KnValidationMessages>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </form>
                </template>
            </Card>
            <Card>
                <template #header>
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #left>
                            {{ $t('kpi.targetDefinition.applyTargetonKPI') }}
                        </template>
                    </Toolbar>
                </template>
                <template #footer>
                    <div class="p-d-inline-flex">
                        <Button class="kn-button kn-button--secondary " @click="addKpiDialog()">{{ $t('kpi.targetDefinition.addKpiBtn') }}</Button>
                    </div>
                </template>
                <template #content>
                    <DataTable :value="kpi" :loading="loading" class="editable-cells-table" dataKey="id" responsiveLayout="stack" editMode="cell" :scrollable="true" scrollHeight="400px" data-test="selected-kpi-table">
                        <template #empty>
                            {{ $t('common.info.noElementSelected') }}
                        </template>
                        <template #loading>
                            {{ $t('common.info.dataLoading') }}
                        </template>

                        <Column field="kpiName" :header="$t('kpi.targetDefinition.kpiName')" key="kpiName" :sortable="true" class="kn-truncated" :style="targetDefinitionDetailDecriptor.table.column.style"></Column>
                        <Column field="value" :header="$t('kpi.targetDefinition.kpiValue')" key="value" :sortable="true" class="kn-truncated" :style="targetDefinitionDetailDecriptor.table.column.style">
                            <template #body="slotProps">
                                {{ slotProps.data[slotProps.column.props.field] }}
                            </template>
                            <template #editor="slotProps">
                                <InputNumber v-model="slotProps.data[slotProps.column.props.field]" showButtons />
                            </template>
                        </Column>
                        <Column :style="targetDefinitionDetailDecriptor.table.iconColumn.style">
                            <template #body="slotProps">
                                <Button icon="pi pi-trash" class="p-button-link" @click="deleteKpi(slotProps.data)" />
                            </template>
                        </Column>
                    </DataTable>
                </template>
            </Card>
        </div>
    </div>
    <Dialog :header="$t('kpi.targetDefinition.addKpiBtn')" :visible="kpiDialogVisible" :modal="true" :closable="false" class="p-fluid kn-dialog--toolbar--primary">
        <template #header>
            <div>
                <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0">
                    <template #left>
                        {{ $t('kpi.targetDefinition.addKpiBtn') }}
                    </template>
                    <template #right>
                        <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" @click="addKpi" />
                        <Button icon="pi pi-times" class="kn-button p-button-text p-button-rounded" @click="closeKpiDialog" />
                    </template>
                </Toolbar>
            </div>
        </template>
        <DataTable
            :paginator="true"
            :rows="15"
            :rowsPerPageOptions="[10, 15, 20]"
            v-model:selection="selectedKpi"
            :value="filteredKpi"
            :loading="loadingAllKpi"
            class="p-datatable-sm kn-table"
            dataKey="kpiId"
            responsiveLayout="stack"
            v-model:filters="filters"
            filterDisplay="menu"
            :globalFilterFields="targetDefinitionDetailDecriptor.globalFilterFields"
        >
            <template #header>
                <div class="table-header">
                    <span class="p-input-icon-left">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" type="text" v-model="filters['global'].value" :placeholder="$t('common.search')" badge="0" data-test="search-input" />
                    </span>
                </div>
            </template>
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <template #loading>
                {{ $t('common.info.dataLoading') }}
            </template>

            <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
            <Column v-for="col of targetDefinitionDetailDecriptor.columnsAllKPI" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true" class="kn-truncated"> </Column>
        </DataTable>
    </Dialog>
    <Dialog :header="$t('kpi.targetDefinition.saveTarget')" v-model:visible="categoryDialogVisiable" :modal="true" :closable="true" class="p-fluid kn-dialog--toolbar--primary">
        <div class="p-pt-4">
            <span class="p-float-label">
                <AutoComplete id="category" v-model="target.category" :suggestions="filteredCategory" @complete="searchCategory($event)" field="valueName" />
                <label for="category" class="kn-material-input-label"> {{ $t('kpi.targetDefinition.kpiCategory') }}</label>
            </span>
        </div>
        <template #footer>
            <Button label="Apply" icon="pi pi-check" class="kn-button kn-button--primary " @click="handleSubmit" />
        </template>
    </Dialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import { formatDate } from '@/helpers/commons/localeHelper'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { FilterOperator } from 'primevue/api'
import AutoComplete from 'primevue/autocomplete'
import targetDefinitionDetailDecriptor from './TargetDefinitionDetailDescriptor.json'
import targetDefinitionValidationDescriptor from './TargetDefinitionValidationDescriptor.json'
import Column from 'primevue/column'
import Calendar from 'primevue/calendar'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import InputNumber from 'primevue/inputnumber'
import axios from 'axios'
import { iCategory, iTargetDefinition, iValues } from './TargetDefinition'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'

export default defineComponent({
    name: 'target-definition-detail',
    components: {
        Calendar,
        DataTable,
        Column,
        Dialog,
        KnValidationMessages,
        AutoComplete,
        InputNumber
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
            selectedKpi: [] as iValues[],
            categories: [] as iCategory[],
            filteredCategory: [] as iCategory[],
            selectedCategory: null,
            loading: false,
            loadingAllKpi: false,
            kpiDialogVisible: false,
            categoryDialogVisiable: false,
            v$: useValidate() as any,
            filters: {
                global: [filterDefault],
                kpiName: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                kpiCategory: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                domainCode: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                kpiDate: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                kpiAuthor: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                }
            } as Object
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
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/domains/listByCode/KPI_TARGET_CATEGORY')
                .then((response) => (this.categories = response.data))
                .finally(() => console.log(this.categories))
        },
        searchCategory(event) {
            console.log(event)
            setTimeout(() => {
                if (!event.query.trim().length) {
                    this.filteredCategory = [...this.categories]
                } else {
                    this.filteredCategory = this.categories.filter((category) => {
                        return category.valueName.toLowerCase().startsWith(event.query.toLowerCase())
                    })
                }
            }, 250)
            console.log('filtered', this.filteredCategory)
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
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpiee/saveTarget'

            this.target.values = this.kpi.map((kpi: iValues) => {
                return {
                    kpiId: kpi.kpiId,
                    kpiVersion: kpi.kpiVersion,
                    value: kpi.value,
                    targetId: kpi.targetId
                }
            })
            console.log(this.target)
            let operation = this.target.id ? 'update' : 'insert'
            this.categoryDialogVisiable = true
            await axios.post(url, this.target).then((response) => {
                console.log('RESPONSE', response)
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
                    this.$emit('saved')
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
        addKpi() {
            this.kpi.push(...this.selectedKpi)
            this.kpiDialogVisible = false
            if (this.selectedKpi.length > 0) {
                this.$store.commit('setInfo', {
                    title: this.$t('kpi.targetDefinition.kpiAddedTitile'),
                    msg: this.$t('kpi.targetDefinition.kpiAddedMessage')
                })
                this.setDirty()
            }
            this.selectedKpi = []
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
