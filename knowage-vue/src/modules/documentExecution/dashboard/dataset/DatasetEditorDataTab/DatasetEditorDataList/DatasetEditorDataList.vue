<template>
    <Card id="dataset-editor-list-card">
        <template #title>
            <Button label="Add Dataset" icon="pi pi-plus-circle" class="p-button-outlined" @click="showCalculatedFieldDialog"></Button>
        </template>
        <template #content> Lorem ipsum dolor sit amet, consectetur adipisicing elit. Inventore sed consequuntur error repudiandae numquam deserunt quisquam repellat libero asperiores earum nam nobis, culpa ratione quam perferendis esse, cupiditate neque quas! </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
// import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'

export default defineComponent({
    name: 'dataset-editor-data-list',
    components: { Card },
    props: {},
    emits: [],
    data() {
        return {}
    },
    setup() {},
    async created() {},
    methods: {}
})
</script>

<style lang="scss">
#dataset-editor-list-card .p-card-body .p-card-title {
    display: flex;
    justify-content: end;
}
</style>
