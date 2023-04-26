<template>
    <Accordion v-if="physicalModel" :multiple="true" :active-index="[0]">
        <AccordionTab>
            <template #header>
                <span>{{ $t('metaweb.physicalModel.misc') }}</span>
            </template>

            <div class="p-grid">
                <div v-for="(modelInfo, index) in metawebPropertyListTabDescriptor.physicalModelInfo" :key="index" class="p-col-6">
                    <div class="p-fluid">
                        <div class="p-field">
                            <label :for="modelInfo.name" class="kn-material-input-label"> {{ $t(modelInfo.label) }} </label>
                            <InputText :id="modelInfo.name" v-model="physicalModel[modelInfo.name]" class="kn-material-input" :disabled="true" :data-test="'input-' + modelInfo.label" />
                        </div>
                    </div>
                </div>
            </div>
        </AccordionTab>

        <AccordionTab v-for="(categoryKey, index) in Object.keys(categories)" :key="index">
            <template #header>
                <span>{{ categoryKey }}</span>
            </template>

            <div class="p-grid">
                <div v-for="(prop, index) in categories[categoryKey]" :key="index" class="p-col-6">
                    <div class="p-fluid">
                        <div class="p-field">
                            <label class="kn-material-input-label"> {{ prop.propertyType.name }} </label>
                            <InputText v-model="prop.value" class="kn-material-input" :disabled="true" :data-test="'input-' + prop.propertyType.name" />
                        </div>
                    </div>
                </div>
            </div>
        </AccordionTab>
    </Accordion>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iColumn, iPhysicalModel } from '../../Metaweb'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import metawebPropertyListTabDescriptor from './MetawebPropertyListTabDescriptor.json'

export default defineComponent({
    name: 'metaweb-property-list-tab',
    components: { Accordion, AccordionTab },
    props: { selectedPhysicalModel: { type: Object as PropType<iColumn | iPhysicalModel | null> } },
    data() {
        return {
            metawebPropertyListTabDescriptor,
            physicalModel: null as iColumn | iPhysicalModel | null,
            categories: [] as any[]
        }
    },
    watch: {
        selectedPhysicalModel() {
            this.loadPhysicalModel()
        }
    },
    created() {
        this.loadPhysicalModel()
    },
    methods: {
        loadPhysicalModel() {
            this.physicalModel = this.selectedPhysicalModel as iColumn | iPhysicalModel

            this.loadCategories()
        },
        loadCategories() {
            this.categories = {} as any
            if (this.physicalModel) {
                for (let i = 0; i < this.physicalModel.properties.length; i++) {
                    const tempProperty = this.physicalModel?.properties[i]
                    const key = Object.keys(tempProperty)[0]
                    const newKey = key?.split('.')

                    if (!this.categories[newKey[0]]) {
                        this.categories[newKey[0]] = []
                    }
                    this.categories[newKey[0]].push(tempProperty[key])
                }
            }
        }
    }
})
</script>
