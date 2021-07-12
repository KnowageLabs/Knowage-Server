<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" />
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
                                <InputText id="name" class="kn-material-input" type="text" v-model="target.name" @change="setDirty" />
                                <label for="name" class="kn-material-input-label">Name * </label>
                            </span>
                        </div>
                        <div class="kn-flex">
                            <div class="p-d-flex p-jc-between">
                                <span class="p-float-label">
                                    <Calendar id="startDate" class="kn-material-input" v-model="target.startValidity" :showIcon="true" :manualInput="false" @change="setDirty" />
                                    <label for="startnDate" class="kn-material-input-label"> Start Validity Date * </label>
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
                            Apply Target on Kpi
                        </template>
                    </Toolbar>
                </template>
                <template #content>
                    <DataTable class="p-datatable-sm kn-table">
                        <template #empty>
                            {{ $t('common.info.noDataFound') }}
                        </template>
                        <template #loading>
                            {{ $t('common.info.dataLoading') }}
                        </template>

                        <!-- <Column v-for="col of targetDefinitionDetailDecriptor.columns" :field="col.field" :header="col.header" :key="col.field" class="kn-truncated"> </Column> -->
                    </DataTable>
                </template>
            </Card>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iTargetDefinition } from './TargetDefinition'
import targetDefinitionDetailDecriptor from './TargetDefinitionDetailDescriptor.json'
import Calendar from 'primevue/calendar'
import DataTable from 'primevue/datatable'
export default defineComponent({
    name: 'target-definition-detail',
    components: {
        Calendar,
        DataTable
    },
    props: {
        model: {
            type: Object,
            required: false
        }
    },
    data() {
        return {
            target: {} as iTargetDefinition,
            selectedDate: new Date(),
            targetDefinitionDetailDecriptor: targetDefinitionDetailDecriptor
        }
    },
    watch: {
        model() {
            this.target = { ...this.model } as iTargetDefinition
        }
    },
    mounted() {
        if (this.model) {
            this.target = { ...this.model } as iTargetDefinition
        }
    },
    methods: {
        closeTemplate() {
            this.$emit('close')
        },
        setDirty(): void {
            this.$emit('touched')
            console.log('dirty')
        }
    }
})
</script>
