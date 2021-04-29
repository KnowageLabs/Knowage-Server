<template>
    <Card :class="['kn-tab-card--icon', 'selectable']">
        <template #content>
            <span class="heading">{{ element.name }}</span>
            <span v-if="element.description" class="subheading">{{ element.description }}</span>
            <i :class="['icon', element.icon]"></i>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'kn-tab-card',
    props: {
        element: {}
    }
})
</script>

<style lang="scss"></style>
