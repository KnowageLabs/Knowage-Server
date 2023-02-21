<template>
    <Card class="p-m-2">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('common.parameters') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div v-for="(parameter, index) in parameters" :key="index" class="p-mt-3">
                <span class="p-float-label">
                    <InputText v-model.trim="parameter.value" class="kn-material-input" data-test="parameter-input" />
                    <label class="kn-material-input-label"> {{ parameter.name }} </label>
                </span>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import Card from 'primevue/card'

    export default defineComponent({
        name: 'function-catalog-parameters-form',
        components: { Card },
        props: { propParameters: { type: Array } },
        data() {
            return {
                parameters: [] as any[]
            }
        },
        watch: {
            propParameters() {
                this.loadParameters()
            }
        },
        created() {
            this.loadParameters()
        },
        methods: {
            loadParameters() {
                this.parameters = []
                this.propParameters?.forEach((el: any) => {
                    el.value = el.defaultValue
                    this.parameters.push(el)
                })
            }
        }
    })
</script>
