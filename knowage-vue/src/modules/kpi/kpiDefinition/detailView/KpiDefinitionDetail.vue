<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #left>{{ selectedKpi.name }}</template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="handleSubmit" :disabled="buttonDisabled" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplateConfirm" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="card">
        <TabView class="tabview-custom" data-test="tab-view">
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.kpi.kpiDefinition.formulaTitle') }}</span>
                </template>

                {{ selectedKpi }}
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.kpi.kpiDefinition.cardinalityTtitle') }}</span>
                </template>
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.kpi.kpiDefinition.tresholdTitle') }}</span>
                </template>
            </TabPanel>
        </TabView>
    </div>
</template>

<script lang="ts">
import axios from 'axios'
import { defineComponent } from 'vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import tabViewDescriptor from './KpiDefinitionDetailDescriptor.json'

export default defineComponent({
    components: { TabView, TabPanel },
    props: {
        id: {
            type: String,
            required: false
        },
        version: {
            type: String,
            required: false
        }
    },
    emits: ['touched', 'closed', 'inserted'],
    data() {
        return {
            tabViewDescriptor,
            touched: false,
            loading: false,
            selectedKpi: {} as any
        }
    },
    watch: {
        id() {
            this.loadSelectedKpi()
        }
    },
    async created() {
        this.loadSelectedKpi()
    },
    methods: {
        async loadSelectedKpi() {
            this.loading = true
            if (this.id) {
                await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${this.id}/${this.version}/loadKpi`).then((response) => (this.selectedKpi = { ...response.data }))
            } else {
                this.selectedKpi = {} as any
            }
            this.loading = false
        },
        closeTemplateConfirm() {
            if (!this.touched) {
                this.closeTemplate()
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.closeTemplate()
                    }
                })
            }
        },
        closeTemplate() {
            this.$router.push('/kpidef')
            this.$emit('closed')
        }
    }
})
</script>
