<template>
    <div class="p-fluid p-jc-center kn-height-full">
        <div class="p-col-12">
            <Card style="width: 100%; margin-bottom: 2em">
                <template #header>
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #left>
                            {{ $t('managers.usersManagement.attributes') }}
                        </template>
                    </Toolbar>
                </template>
                <template #content>
                    <div class="p-m-5">
                        <div class="p-field" v-for="attribute in attributes" :key="attribute.attributeId">
                            <div class="p-inputgroup" v-if="modelValue[attribute.attributeId]">
                                <span class="p-float-label">
                                    <InputText :disabled="attribute.lovId" class="p-inputtext p-component kn-material-input" :id="attribute.attributeId" @input="onInputChange(attribute, $event.target.value)" type="text" v-model="userAttributesForm[attribute.attributeId][attribute.attributeName]" />
                                    <label :for="attribute.attributeName">{{ attribute.attributeName }}</label>
                                </span>
                                <Button v-if="attribute.lovId" icon="pi pi-pencil" class="p-button-text p-button-rounded p-button-plain" @click="openLovValuesDialog(attribute)" />
                                <Button icon="pi pi-times-circle" class="p-button-text p-button-rounded p-button-plain" @click="eraseAttribute(attribute)" />
                            </div>
                        </div>
                    </div>
                </template>
            </Card>
        </div>
    </div>
    <UserAttributesLovValueDialog :attribute="selectedAttribute" :selection="initialSelection" :dialogVisible="lovDialogVisible" @saveLovValues="onSaveLovValues" @closeDialog=";(lovDialogVisible = false), (selectedAttribute = null)"> </UserAttributesLovValueDialog>
</template>
<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iAttribute } from '../UsersManagement'
import UserAttributesLovValueDialog from './UserAttributesLovValueDialog.vue'

export default defineComponent({
    components: { UserAttributesLovValueDialog },
    props: {
        attributes: {
            type: Object as PropType<iAttribute[]>,
            required: true
        },
        modelValue: {
            type: Object as any,
            required: true
        }
    },
    data() {
        return {
            selectedAttribute: null as iAttribute | null,
            lovDialogVisible: false,
            userAttributesForm: {},
            initialSelection: null as any
        }
    },
    watch: {
        modelValue: function(model) {
            this.userAttributesForm = { ...model }
        }
    },
    methods: {
        onInputChange(attribute: iAttribute, value) {
            const newObj = {}
            newObj[attribute.attributeName] = value
            const newValue = this.modelValue ? { ...this.modelValue } : {}
            newValue[attribute.attributeId] = newObj
            this.$emit('update:modelValue', newValue)
            this.$emit('formDirty')
        },
        eraseAttribute(attr: iAttribute) {
            this.onInputChange(attr, '')
        },
        openLovValuesDialog(attribute: iAttribute) {
            // const path_matcher = /\{;\{[A-Za-z0-9;_]*\}*/g

            this.selectedAttribute = attribute
            let value: any = null
            value = this.userAttributesForm[attribute.attributeId][attribute.attributeName] as String

            this.initialSelection = []
            if (value) {
                if (this.selectedAttribute.multivalue) {
                    if (this.selectedAttribute.syntax) {
                        const match = /(?<=\{;\{)[A-Za-z0-9;_]*(?!\}\})/
                        if (match.test(value)) {
                            const ind = value.indexOf('{;{')
                            const indEnd = value.indexOf('}}')
                            this.initialSelection = value.substring(ind + 3, indEnd).split(';')
                        }
                    } else {
                        this.initialSelection = value.split(',')?.map((val) => val.substring(1, val.length - 1))
                    }
                }
            }

            this.lovDialogVisible = true
        },
        onSaveLovValues(selectedLovValues) {
            let newValue = ''
            if (this.selectedAttribute && Array.isArray(selectedLovValues)) {
                if (this.selectedAttribute.syntax) {
                    newValue = selectedLovValues.reduce((prev, curr, ind) => {
                        prev += ind > 0 ? ';' : ''
                        prev += curr.value
                        return prev
                    }, '')
                    newValue = `{;{${newValue}}}`
                } else {
                    newValue = selectedLovValues.reduce((prev, curr, ind) => {
                        prev += ind > 0 ? ',' : ''
                        prev += `'${curr.value}'`
                        return prev
                    }, '')
                }
            } else {
                newValue = selectedLovValues.value
            }
            if (this.selectedAttribute) {
                this.onInputChange(this.selectedAttribute, newValue)
            }
            this.lovDialogVisible = false
            this.selectedAttribute = null
        }
    }
})
</script>
