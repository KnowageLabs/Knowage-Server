<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #left>{{ selectedSchema.name }} </template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="card">
        <TabView class="tabview-custom" data-test="tab-view">
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.mondrianSchemasManagement.detail.title') }}</span>
                </template>

                <MondrianSchemasDetailTab :selectedSchema="selectedSchema" @fieldChanged="onFieldChange" />
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.mondrianSchemasManagement.workflow.title') }}</span>
                </template>
            </TabPanel>
        </TabView>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iSchema } from './MondrianSchemas'
import axios from 'axios'
import tabViewDescriptor from './MondrianSchemasTabViewDescriptor.json'
import useValidate from '@vuelidate/core'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import MondrianSchemasDetailTab from './MondrianSchemasDetailTab/MondrianSchemasDetailTab.vue'

export default defineComponent({
    components: {
        TabView,
        TabPanel,
        MondrianSchemasDetailTab
    },
    emits: ['touched', 'closed'],
    props: {
        id: {
            type: String,
            required: false
        }
    },
    data() {
        return {
            loading: false,
            tabViewDescriptor: tabViewDescriptor,
            selectedSchema: {} as iSchema,
            v$: useValidate() as any
        }
    },
    async created() {
        this.loadSelectedSchema()
    },
    computed: {
        buttonDisabled(): any {
            return this.v$.$invalid
        }
    },
    watch: {
        id() {
            this.loadSelectedSchema()
        }
    },
    methods: {
        setDirty() {
            this.$emit('touched')
        },
        closeTemplate() {
            this.$router.push('/schemas')
            this.$emit('closed')
        },
        onFieldChange(event) {
            this.selectedSchema[event.fieldName] = event.value
            this.$emit('touched')
        },
        async loadSelectedSchema() {
            this.loading = true
            if (this.id) {
                await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/mondrianSchemasResource/${this.id}`).then((response) => (this.selectedSchema = response.data))
            }
            this.loading = false
        }
    }
})
</script>
