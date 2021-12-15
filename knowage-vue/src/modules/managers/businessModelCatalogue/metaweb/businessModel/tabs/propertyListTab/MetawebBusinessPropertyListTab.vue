<template>
    <Accordion v-if="businessModel" :multiple="true" :activeIndex="[0]">
        <AccordionTab>
            <template #header>
                <span>{{ $t('metaweb.physicalModel.misc') }}</span>
            </template>

            <div v-for="(modelInfo, index) in metawebBusinessPropertyListTabDescriptor.businessModelInfo" :key="index">
                <div class="p-fluid">
                    <div class="p-field">
                        <label :for="modelInfo.name" class="kn-material-input-label"> {{ $t(modelInfo.label) }} </label>
                        <InputText class="kn-material-input" v-model="businessModel[modelInfo.name]" :id="modelInfo.name" :disabled="true" />
                    </div>
                </div>
            </div>

            <div class="p-fluid" v-if="businessModel.physicalTable && meta">
                <div class="p-field">
                    <label class="kn-material-input-label"> {{ $t('metaweb.businessModel.physicalTable') }} </label>
                    <InputText class="kn-material-input" v-model="meta.metaSales.physicalModels[businessModel.physicalTable.physicalTableIndex].name" :disabled="true" />
                </div>
            </div>
        </AccordionTab>

        <AccordionTab v-for="(categoryKey, index) in Object.keys(categories)" :key="index">
            <template #header>
                <span>{{ categoryKey }}</span>
            </template>

            <div v-for="(prop, index) in categories[categoryKey]" :key="index">
                <!-- <div class="p-fluid">
                    <div class="p-field">
                        <label class="kn-material-input-label"> {{ prop.propertyType.name }} </label>
                        <InputText class="kn-material-input" v-model="prop.value" :disabled="true" />
                    </div>
                </div> -->

                <!-- first select -->
                <div class="p-fluid" v-if="prop.propertyType.admissibleValues.length !== 0">
                    <div class="p-field">
                        <label class="kn-material-input-label"> {{ prop.propertyType.name }} </label>
                        <Dropdown class="kn-material-input" v-model="prop.value" :options="prop.propertyType.admissibleValues" @change="updateCategoryValue(prop)" />
                    </div>
                </div>

                <!-- first input -->
                <div class="p-fluid" v-if="prop.propertyType.admissibleValues.length === 0 && prop.type !== 'structural.attribute' && prop.type !== 'structural.sqlFilter' && prop.type !== 'behavioural.notEnabledRoles'">
                    <div class="p-field">
                        <label class="kn-material-input-label"> {{ prop.propertyType.name }} </label>
                        <InputText class="kn-material-input" v-model="prop.value" :disabled="prop.type === 'physical.physicaltable'" @change="updateCategoryValue(prop)" />
                    </div>
                </div>

                <!--profile attributes visibility -->
                <!-- TODO ASK ABOUT PROFILE ATTRIBUTES -->
                <div class="p-fluid" v-if="prop.type === 'structural.attribute'">
                    <div class="p-field">
                        <label class="kn-material-input-label"> {{ prop.propertyType.name }} </label>
                        <Dropdown class="kn-material-input" v-model="prop.value" :options="profileAttributes" @change="updateCategoryValue(prop)" />
                    </div>
                </div>

                <!--profile role visibility -->
                <!-- TODO ASK ABOUT ROLES -->
                <div class="p-fluid" v-if="prop.type === 'behavioural.notEnabledRoles'">
                    <div class="p-field">
                        <label class="kn-material-input-label"> {{ prop.propertyType.name }} </label>
                        <Dropdown class="kn-material-input" v-model="roleVisibility" :options="roles" />
                    </div>
                </div>

                <!-- edit temporal hierarchy button -->
                <!-- TODO ASK ABOUT TEMPORAL -->
                <Button v-if="prop.value === 'temporal dimension' || prop.value === 'time dimension'" icon="fa fa-sitemap" v-tooltip.top="$t('metaweb.businessModel.temporalHierarchy')" class="p-button-text p-button-rounded p-button-plain" @click="editTemporalHierarchy" />

                <!-- last input -->
                <div class="p-fluid" v-if="businessModel.physicalColumn && categoryKey === 'physical'">
                    <div class="p-field">
                        <label class="kn-material-input-label"> {{ $t('metaweb.businessModel.physicalColumn') }} </label>
                        <InputText class="kn-material-input" v-model="businessModel.physicalColumn.name" :disabled="true" />
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
import metaMock from '../../../MetawebMock.json'

export default defineComponent({
    name: 'metaweb-business-property-list-tab',
    components: { Accordion, AccordionTab, Dropdown },
    props: { selectedBusinessModel: { type: Object as PropType<iBusinessModel | null> } },
    emits: ['metaUpdated'],
    data() {
        return {
            metawebBusinessPropertyListTabDescriptor,
            meta: metaMock as any,
            businessModel: null as iBusinessModel | null,
            categories: [] as any[],
            roleVisibility: null as any, // TODO ASK ABOUT THIS
            roles: [] as any[], // sbiModule_config.avaiableRoles TODO ASK ABOUT THIS
            profileAttributes: [] as any[] // sbiModule_config.profileAttributes   TODO ASK ABOUT THIS
        }
    },
    watch: {
        selectedBusinessModel() {
            this.loadBusinessModel()
        }
    },
    created() {
        this.loadBusinessModel()
    },
    methods: {
        loadBusinessModel() {
            this.businessModel = this.selectedBusinessModel as any
            this.businessModel = this.selectedBusinessModel as iBusinessModel

            this.loadCategories()
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
        editTemporalHierarchy() {
            console.log('editTemporalHierarchy clicked!')
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
