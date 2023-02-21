<template>
    <Accordion v-if="businessModel" :multiple="true" :active-index="[0, 1]">
        <AccordionTab>
            <template #header>
                <span>{{ $t('metaweb.physicalModel.misc') }}</span>
            </template>

            <div class="p-grid">
                <div v-for="(modelInfo, index) in metawebBusinessPropertyListTabDescriptor.businessModelInfo" :key="index" class="p-col-6">
                    <div class="p-field">
                        <label :for="modelInfo.name" class="kn-material-input-label"> {{ $t(modelInfo.label) }} </label>
                        <InputText :id="modelInfo.name" v-model="businessModel[modelInfo.name]" class="kn-material-input" data-test="input-name" />
                    </div>
                </div>

                <div v-if="businessModel.physicalTable && meta" class="p-col-6">
                    <div class="p-field">
                        <label class="kn-material-input-label"> {{ $t('metaweb.businessModel.physicalTable') }} </label>
                        <InputText v-model="meta.physicalModels[businessModel.physicalTable.physicalTableIndex].name" class="kn-material-input" :disabled="true" />
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
                    <div v-if="prop.propertyType.admissibleValues.length !== 0" class="p-fluid">
                        <div class="p-field">
                            <label class="kn-material-input-label"> {{ prop.propertyType.name }} </label>
                            <Dropdown v-model="prop.value" class="kn-material-input" :options="prop.propertyType.admissibleValues" @change="updateCategoryValue(prop)" />
                        </div>
                    </div>
                    <div v-if="prop.propertyType.admissibleValues.length === 0 && prop.type !== 'structural.attribute' && prop.type !== 'structural.sqlFilter' && prop.type !== 'behavioural.notEnabledRoles'" class="p-fluid">
                        <div class="p-field">
                            <label class="kn-material-input-label"> {{ prop.propertyType.name }} </label>
                            <InputText v-model="prop.value" class="kn-material-input" :disabled="prop.type === 'physical.physicaltable'" @change="updateCategoryValue(prop)" />
                        </div>
                    </div>

                    <div v-if="prop.type === 'structural.attribute'" class="p-fluid">
                        <div class="p-field">
                            <label class="kn-material-input-label"> {{ prop.propertyType.name }} </label>
                            <Dropdown v-model="prop.value" class="kn-material-input" :options="profileAttributes" @change="updateCategoryValue(prop)" />
                        </div>
                    </div>

                    <div v-if="prop.type === 'behavioural.notEnabledRoles'" class="p-fluid">
                        <div class="p-field">
                            <label class="kn-material-input-label"> {{ prop.propertyType.name }} </label>
                            <MultiSelect v-model="prop.value" class="kn-material-input" :options="roleOptions" @change="updateCategoryValue(prop)" />
                        </div>
                    </div>

                    <div v-if="businessModel.physicalColumn && categoryKey === 'physical'" class="p-fluid">
                        <div class="p-field">
                            <label class="kn-material-input-label"> {{ $t('metaweb.businessModel.physicalColumn') }} </label>
                            <InputText v-model="businessModel.physicalColumn.name" class="kn-material-input" :disabled="true" />
                        </div>
                    </div>
                </div>
            </div>
        </AccordionTab>
    </Accordion>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iBusinessModel } from '../../../Metaweb'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Dropdown from 'primevue/dropdown'
import metawebBusinessPropertyListTabDescriptor from './MetawebBusinessPropertyListTabDescriptor.json'
import MultiSelect from 'primevue/multiselect'
import mainStore from '../../../../../../../App.store'

export default defineComponent({
    name: 'metaweb-business-property-list-tab',
    components: { Accordion, AccordionTab, Dropdown, MultiSelect },
    props: { selectedBusinessModel: { type: Object as PropType<iBusinessModel | null> }, roles: { type: Array }, propMeta: { type: Object } },
    emits: ['metaUpdated'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            metawebBusinessPropertyListTabDescriptor,
            meta: null as any,
            businessModel: null as iBusinessModel | null,
            categories: [] as any[],
            roleOptions: [] as any[]
        }
    },
    computed: {
        profileAttributes(): any[] {
            return (this.store.$state as any).user.attributes ? Object.keys((this.store.$state as any).user.attributes) : []
        }
    },
    watch: {
        selectedBusinessModel() {
            this.loadMeta()
            this.loadBusinessModel()
        },
        roles() {
            this.loadRoleOptions()
        }
    },
    created() {
        this.loadMeta()
        this.loadBusinessModel()
    },
    methods: {
        loadMeta() {
            this.meta = this.propMeta as any
        },
        loadBusinessModel() {
            this.businessModel = this.selectedBusinessModel as iBusinessModel

            this.loadCategories()
        },
        loadRoleOptions() {
            this.roleOptions = this.roles as any[]
        },
        loadCategories() {
            this.categories = {} as any
            if (this.businessModel) {
                for (let i = 0; i < this.businessModel.properties.length; i++) {
                    const tempProperty = this.businessModel?.properties[i]

                    const key = Object.keys(tempProperty)[0]
                    const newKey = key?.split('.')

                    if (!this.categories[newKey[0]]) {
                        this.categories[newKey[0]] = []
                    }

                    this.categories[newKey[0]].push({ ...tempProperty[key], type: key })
                }
            }
        },
        updateCategoryValue(property: any) {
            this.businessModel?.properties?.forEach((el: any) => {
                const key = Object.keys(el)[0]
                if (key === property.type) {
                    el[key].value = property.value
                }
            })

            this.$emit('metaUpdated')
        }
    }
})
</script>
