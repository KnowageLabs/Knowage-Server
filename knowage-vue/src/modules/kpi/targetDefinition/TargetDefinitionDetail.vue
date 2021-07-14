<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" @click="handleSubmit" />
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
                                <span class="p-float-label">
                                    <Calendar
                                        id="startDate"
                                        class="kn-material-input"
                                        v-model="target.startValidity"
                                        :class="{
                                            'p-invalid': !target.startValidity
                                        }"
                                        :showIcon="true"
                                        :manualInput="false"
                                        @change="setDirty"
                                    />
                                    <label for="startDate" class="kn-material-input-label"> Start Validity Date * </label>
                                </span>
                                <div class="p-d-flex">
                                    <span class="p-float-label">
                                        <Calendar id="endDate" class="kn-material-input" v-model="target.endValidity" :showIcon="true" :manualInput="false" @change="setDirty" />
                                        <label for="endDate" class="kn-material-input-label"> End Validity Date * </label>
                                    </span>
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
                    <div class="table-footer">
                        <span class="p-input-icon-left">
                            <Button class="kn-button kn-button--secondary" @click="addKpiDialog()">{{ $t('kpi.targetDefinition.addKpiBtn') }}</Button>
                        </span>
                    </div>
                </template>
                <template #content>
                    <DataTable :value="kpi" :loading="loading" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack">
                        <template #empty>
                            {{ $t('common.info.noDataFound') }}
                        </template>
                        <template #loading>
                            {{ $t('common.info.dataLoading') }}
                        </template>

                        <Column v-for="col of targetDefinitionDetailDecriptor.columns" :field="col.field" :header="col.header" :key="col.field" class="kn-truncated"> </Column
                        ><Column>
                            <template #body="slotProps">
                                <Button icon="pi pi-trash" class="p-button-link" @click="deleteKpi(slotProps.data)" />
                            </template>
                        </Column>
                    </DataTable>
                </template>
            </Card>
        </div>
    </div>
    <Dialog :visible="kpiDialogVisible" :modal="true" :closable="false" class="p-fluid kn-dialog--toolbar--primary">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0">
                <template #right>
                    <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" @click="addKpi" />
                    <Button class="kn-button p-button-text p-button-rounded" icon="pi pi-times" @click="closeKpiDialog" />
                </template>
            </Toolbar>
        </template>
        <DataTable
            :paginator="true"
            :rows="20"
            :rowsPerPageOptions="[10, 15, 20]"
            v-model:selection="selectedKpi"
            :value="allKpi"
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
            <Column v-for="col of targetDefinitionDetailDecriptor.columnsAllKPI" :field="col.field" :header="col.header" :key="col.field" class="kn-truncated"> </Column>
        </DataTable>
    </Dialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import { formatDate } from '@/helpers/commons/localeHelper'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { FilterOperator } from 'primevue/api'
import targetDefinitionDetailDecriptor from './TargetDefinitionDetailDescriptor.json'
import targetDefinitionValidationDescriptor from './TargetDefinitionValidationDescriptor.json'
import Column from 'primevue/column'
import Calendar from 'primevue/calendar'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import axios from 'axios'
import { iTargetDefinition } from './TargetDefinition'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'

export default defineComponent({
    name: 'target-definition-detail',
    components: {
        Calendar,
        DataTable,
        Column,
        Dialog,
        KnValidationMessages
    },
    props: {
        id: {
            type: String,
            required: false
        }
    },
    data() {
        return {
            target: {} as iTargetDefinition,
            formatDate: formatDate,
            targetDefinitionDetailDecriptor: targetDefinitionDetailDecriptor,
            targetDefinitionValidationDescriptor,
            kpi: [] as any,
            allKpi: [] as any,
            selectedKpi: [] as any,
            loading: false,
            loadingAllKpi: false,
            kpiDialogVisible: false,
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
        return {
            target: createValidations('target', targetDefinitionValidationDescriptor.validations.target)
        }
    },
    computed: {
        // filterArray(){
        //   return this.allKpi.filter(item => {
        //       this.kpi.forEach(element => {
        //           if(item != element)
        //           return item
        //       });
        //   })
        // }
    },
    created() {
        if (this.id) {
            this.loadTarget()
        }
    },
    watch: {
        async id() {
            await this.loadTarget()
        }
    },
    methods: {
        async loadTarget() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpiee/' + this.id + '/loadTarget')
                .then((response) => {
                    this.target = {
                        id: response.data.id,
                        name: response.data.name,
                        startValidity: new Date(response.data.startValidity),
                        endValidity: new Date(response.data.endValidity),
                        author: response.data.author,
                        category: response.data.category
                    }
                    this.kpi = []
                    response.data.values.map((val: any) => {
                        this.kpi.push({
                            kpiId: val.kpiId,
                            kpiName: val.kpi.name,
                            kpiVersion: val.kpiVersion,
                            kpiCategory: val.kpi.category.valueName,
                            kpiDate: new Date(val.kpi.dateCreation),
                            kpiAuthor: val.kpi.author,
                            value: val.value
                        })
                    })
                })
                .finally(() => (this.loading = false))
        },
        async loadKpi() {
            this.loadingAllKpi = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/listKpi')
                .then((response) =>
                    response.data.map((kpi: any) => {
                        this.allKpi.push({
                            kpiId: kpi.id,
                            kpiName: kpi.name,
                            kpiVersion: kpi.version,
                            kpiCategory: kpi.category?.valueName,
                            kpiDate: new Date(kpi.dateCreation),
                            kpiAuthor: kpi.author,
                            value: 0
                        })
                    })
                )
                .finally(() => (this.loadingAllKpi = false))
        },
        handleSubmit() {
            console.log('submit')
            if (this.v$.$invalid) {
                console.log(this.v$)
                return
            }
            this.target.values = this.kpi
            console.log(this.target)
        },
        closeTemplate() {
            this.$router.push('/target-definition')
            this.$emit('close')
        },
        setDirty(): void {
            this.$emit('touched')
            console.log('dirty')
        },
        deleteKpi(kpiSelected: any) {
            this.kpi.splice(this.kpi.indexOf(kpiSelected), 1)
        },
        addKpiDialog() {
            console.log('addKpiModal')
            this.loadKpi()
            this.kpiDialogVisible = true
        },
        closeKpiDialog() {
            this.kpiDialogVisible = false
        },
        addKpi() {
            this.kpi.push(...this.selectedKpi)
            this.kpiDialogVisible = false
            console.log(this.kpi)
        }
    }
})
</script>
