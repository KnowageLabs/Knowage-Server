<template>
    <div class="p-m-2">
        <div class="p-fluid p-formgrid p-grid">
            <div class="p-field p-col-4">
                <span class="p-float-label">
                    <Dropdown id="mondrian-schema-select" v-model="selectedMondrianSchema" class="kn-material-input" :options="mondrianSchemas" option-label="name" @change="onMondrianSchemaSelected"> </Dropdown>
                    <label for="mondrian-schema-select" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.designerDialog.selectMondrianSchema') }}</label>
                </span>
            </div>
            <div class="p-col-8"></div>

            <div v-if="selectedMondrianSchema" class="p-field p-col-4">
                <span class="p-float-label">
                    <Dropdown id="cube-select" v-model="selectedCube" class="kn-material-input" :options="cubes" @change="onCubeSelected"> </Dropdown>
                    <label for="cube-select" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.designerDialog.selectCube') }}</label>
                </span>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iMondrianTemplate, iMondrianSchema } from '../../DocumentDetails'
import Dropdown from 'primevue/dropdown'
import { AxiosResponse } from 'axios'
import { mapActions } from 'pinia'
import mainStore from '@/App.store'

export default defineComponent({
    name: 'document-detail-mondrian-form',
    components: { Dropdown },
    props: { sbiExecutionId: { type: String }, mondrianModel: { type: Object as PropType<iMondrianTemplate> }, mondrianSchemas: { type: Array as PropType<iMondrianSchema[]> } },
    data() {
        return {
            model: {} as iMondrianTemplate,
            selectedMondrianSchema: null as iMondrianSchema | null,
            cubes: [] as string[],
            selectedCube: ''
        }
    },
    watch: {
        mondrianModel() {
            this.loadModel()
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        ...mapActions(mainStore, ['setLoading']),
        loadModel() {
            this.model = this.mondrianModel as iMondrianTemplate
        },
        onMondrianSchemaSelected() {
            if (!this.selectedMondrianSchema) return

            this.model.mondrianSchema = this.selectedMondrianSchema.name
            this.model.mondrianSchemaId = this.selectedMondrianSchema.currentContentId
            this.model.id = this.selectedMondrianSchema.id
            this.loadCubes()
        },
        async loadCubes() {
            if (!this.selectedMondrianSchema) return

            this.setLoading(true)
            await this.$http
                .get(import.meta.env.VITE_OLAP_PATH + `1.0/designer/allcubes/${this.selectedMondrianSchema.currentContentId}?SBI_EXECUTION_ID=${this.sbiExecutionId}`)
                .then((response: AxiosResponse<any>) => (this.cubes = response.data))
                .catch(() => {})
            this.setLoading(false)
        },
        onCubeSelected() {
            if (!this.selectedCube) return

            this.loadMDX()
        },
        async loadMDX() {
            if (!this.selectedMondrianSchema || !this.selectedCube) return

            this.setLoading(true)
            await this.$http
                .get(import.meta.env.VITE_OLAP_PATH + `1.0/designer/cubes/getMDX/${this.selectedMondrianSchema.currentContentId}/${this.selectedCube}?SBI_EXECUTION_ID=${this.sbiExecutionId}`, { headers: { Accept: 'application/json, text/plain, */*' } })
                .then((response: AxiosResponse<any>) => {
                    this.model.mdxQuery = response.data
                    this.model.mondrianMdxQuery = response.data
                })
                .catch(() => {})
            this.setLoading(false)
        }
    }
})
</script>
