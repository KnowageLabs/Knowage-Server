<template>
    <Dialog id="metaweb-select-dialog" class="metaweb-dialog remove-padding p-fluid kn-dialog--toolbar--primary" :contentStyle="metawebSelectDialogDescriptor.dialog.style" :visible="visible" :modal="false" :closable="false" position="right" :baseZIndex="1" :autoZIndex="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ businessModel?.name }}
                </template>
                <template #end>
                    <Button class="metaweb-select-dialog-button kn-button p-button-text p-m-2" :label="$t('common.close')" @click="closeDialog"></Button>
                    <Button class="metaweb-select-dialog-button kn-button p-button-text" :label="$t('common.continue')" @click="onContinue"></Button>
                </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        <DataTable v-else :value="rows" class="p-datatable-sm kn-table p-ml-1" :scrollable="true" scrollHeight="100%" v-model:filters="filters" :globalFilterFields="metawebSelectDialogDescriptor.globalFilterFields">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>

            <template #header>
                <div class="table-header p-d-flex">
                    <span class="p-input-icon-left p-mr-3 p-col-12">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" v-model="filters['global'].value" :placeholder="$t('common.search')" />
                    </span>
                </div>
            </template>

            <Column field="value" :header="$t('metaweb.selectDialog.tableName')" style="flex:5"></Column>
            <Column :header="$t('metaweb.physicalModel.title')">
                <template #header>
                    <Checkbox class="p-mr-2" v-model="allPhysicalSelected" :binary="true" @change="setAllChecked('physical')" />
                </template>
                <template #body="slotProps">
                    <Checkbox v-model="selected[slotProps.data.value].physical" :binary="true" @change="setChecked(slotProps.data, 'physical')" />
                </template>
            </Column>
            <Column :header="$t('metaweb.businessModel.title')">
                <template #header>
                    <Checkbox class="p-mr-2" v-model="allBusinessSelected" :binary="true" @change="setAllChecked('business')" />
                </template>
                <template #body="slotProps">
                    <Checkbox v-model="selected[slotProps.data.value].business" :binary="true" @change="setChecked(slotProps.data, 'business')" />
                </template>
            </Column>
        </DataTable>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent, PropType } from 'vue'
    import { AxiosResponse } from 'axios'
    import { iBusinessModel } from '../../BusinessModelCatalogue'
    import { filterDefault } from '@/helpers/commons/filterHelper'
    import Checkbox from 'primevue/checkbox'
    import Column from 'primevue/column'
    import DataTable from 'primevue/datatable'
    import Dialog from 'primevue/dialog'
    import metawebSelectDialogDescriptor from '@/modules/managers/businessModelCatalogue/metaweb/metawebSelectDialog/MetawebSelectDialogDescriptor.json'

export default defineComponent({
    name: 'metaweb-select-dialog',
    components: { Checkbox, Column, DataTable, Dialog },
    props: { visible: { type: Boolean }, selectedBusinessModel: { type: Object as PropType<iBusinessModel> } },
    emits: ['close', 'metaSelected'],
    data() {
        return {
            metawebSelectDialogDescriptor,
            businessModel: null as iBusinessModel | null,
            datasourceStructure: null as any,
            rows: [] as { value: string }[],
            selected: {} as any,
            allPhysicalSelected: false,
            allBusinessSelected: false,
            filters: {
                global: [filterDefault]
            } as Object,
            loading: false
        }
    },
    watch: {
        async businessModel() {
            await this.loadData()
        },
        async visible(value: boolean) {
            if (value) {
                this.loading = true
                await this.loadDatasourceStructure()
                this.loadRows()
                this.loading = false
            }
        }
    },
    async created() {
        await this.loadData()
    },
    methods: {
        async loadData() {
            this.loading = true
            this.loadBusinessModel()
            await this.loadDatasourceStructure()
            this.loadRows()
            this.loading = false
        },
        loadBusinessModel() {
            this.businessModel = this.selectedBusinessModel as iBusinessModel
        },
        async loadDatasourceStructure() {
            if (this.businessModel?.dataSourceId) {
                let url = `2.0/datasources/structure/${this.businessModel.dataSourceId}?`
                const urlParams = {} as any
                if (this.businessModel.tablePrefixLike) urlParams.tablePrefixLike = this.businessModel.tablePrefixLike
                if (this.businessModel.tablePrefixNotLike) urlParams.tablePrefixNotLike = this.businessModel.tablePrefixNotLike
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url, { params: urlParams }).then((response: AxiosResponse<any>) => (this.datasourceStructure = response.data))
            }
        },
        watch: {
            async businessModel() {
                await this.loadData()
            },
            visible() {
                this.loadRows()
            }
        },
        async created() {
            await this.loadData()
        },
        methods: {
            async loadData() {
                this.loadBusinessModel()
                await this.loadDatasourceStructure()
                this.loadRows()
            },
            loadBusinessModel() {
                this.businessModel = this.selectedBusinessModel as iBusinessModel
            },
            async loadDatasourceStructure() {
                this.loading = true
                if (this.businessModel?.dataSourceId) {
                    let url = `2.0/datasources/structure/${this.businessModel.dataSourceId}?`
                    const urlParams = {} as any
                    if (this.businessModel.tablePrefixLike) urlParams.tablePrefixLike = this.businessModel.tablePrefixLike
                    if (this.businessModel.tablePrefixNotLike) urlParams.tablePrefixNotLike = this.businessModel.tablePrefixNotLike
                    await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url, { params: urlParams }).then((response: AxiosResponse<any>) => (this.datasourceStructure = response.data))
                }
                this.loading = false
            },
            loadRows() {
                this.rows = []
                if (this.datasourceStructure) {
                    Object.keys(this.datasourceStructure).forEach((key: string) => {
                        this.rows.push({ value: key })
                        this.selected[key] = { physical: false, business: false }
                    })
                }
            },
            setChecked(row: { value: string }, typeChecked: string) {
                if (typeChecked === 'business' && this.selected[row.value].business) {
                    this.selected[row.value].physical = true
                } else if (typeChecked === 'physical' && !this.selected[row.value].physical) {
                    this.selected[row.value].business = false
                }
            },
            setAllChecked(typeChecked: string) {
                Object.keys(this.selected).forEach((key: string) => {
                    if (typeChecked === 'business') {
                        this.selected[key].business = this.allBusinessSelected
                        if (this.allBusinessSelected) {
                            this.selected[key].physical = true
                            this.allPhysicalSelected = true
                        }
                    } else {
                        this.selected[key].physical = this.allPhysicalSelected
                        if (!this.allPhysicalSelected) {
                            this.selected[key].business = false
                            this.allBusinessSelected = false
                        }
                    }
                })
            },
            closeDialog() {
                this.$emit('close')
                this.selected = {}
                this.allPhysicalSelected = false
                this.allBusinessSelected = false
            },
            async onContinue() {
                if (!this.checkIfPhysicalModelIsSelected()) {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: this.$t('metaweb.selectDialog.noPhysicalModelsSelectedError')
                    })
                    return
                }

                await this.sendCheckedMetaweb()
            },
            async sendCheckedMetaweb() {
                this.loading = true
                const physicalModels = [] as string[]
                const businessModels = [] as string[]

                this.prepareDataForPost(physicalModels, businessModels)

                await this.$http
                    .post(process.env.VUE_APP_META_API_URL + `/1.0/metaWeb/create`, { datasourceId: '' + this.businessModel?.dataSourceId, physicalModels: physicalModels, businessModels: businessModels, modelName: this.businessModel?.name })
                    .then((response: AxiosResponse<any>) => {
                        this.$emit('metaSelected', response.data)
                    })
                    .catch(() => {})

                this.loading = false
            },
            prepareDataForPost(physicalModels, businessModels) {
                Object.keys(this.selected).forEach((key: string) => {
                    if (this.selected[key].physical) physicalModels.push(key)
                    if (this.selected[key].business) businessModels.push(key)
                })
            },
            checkIfPhysicalModelIsSelected() {
                let isSelected = false
                const keys = Object.keys(this.selected)

                for (let i = 0; i < keys.length; i++) {
                    if (this.selected[keys[i]].physical) {
                        isSelected = true
                        break
                    }
                }

                return isSelected
            }
        }
    })
</script>

<style lang="scss">
    .full-screen-dialog.p-dialog {
        max-height: 100%;
        height: 100vh;
        width: calc(100vw - var(--kn-mainmenu-width));
        margin: 0;
    }
    .full-screen-dialog.p-dialog .p-dialog-content {
        padding: 0;
        margin: 0;
    }
    #metaweb-select-dialog .p-toolbar-group-right {
        height: 100%;
    }

    .metaweb-select-dialog-button {
        font-size: 0.8rem;
    }
</style>
