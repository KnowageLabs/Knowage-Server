<template>
    <div class="kn-hint hint">
        <Card class="kn-hintContent">
            <template #title> <i class="far fa-life-ring kn-hintIcon"></i> {{ $t(title) }} </template>
            <template #content> <div v-html="$t(hint)"></div></template>
        </Card>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'kn-hint',
    props: { title: String, hint: String }
})
</script>
