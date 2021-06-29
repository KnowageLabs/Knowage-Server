<template>
    <div class="p-grid   p-fluid p-jc-center kn-height-full">
        <div class="p-col-12">
            <Card style="width: 100%; margin-bottom: 2em">
                <template #header>
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #left>
                            {{ $t('managers.usersManagement.attributes').toUpperCase() }}
                        </template>
                    </Toolbar>
                </template>
                <template #content>
                    <div class="p-field" v-for="attribute in attributes" :key="attribute.attributeId">
                        <div class="p-inputgroup" v-if="modelValue[attribute.attributeId]">
                            <span class="p-float-label">
                                <InputText class="p-inputtext p-component kn-material-input" :id="attribute.attributeId" @input="onInputChange(attribute, $event.target.value)" type="text" v-model="userAttributesForm[attribute.attributeId][attribute.attributeName]" />
                                <label :for="attribute.attributeName">{{ attribute.attributeName }}</label>
                            </span>
                            <Button icon="pi pi-trash" class="p-button-link" @click="eraseAttribute(attribute)" />
                        </div>
                    </div>
                </template>
            </Card>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iAttribute } from '../UsersManagement'

export default defineComponent({
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
            userAttributesForm: {}
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
        }
    }
})
</script>
