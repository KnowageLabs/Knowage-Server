<template>
    <div class="md-list-item-text">
        <h4>{{ details?.leftOperandAlias }}</h4>
        <p>
            <span>{{ details?.operator.toLowerCase() }}</span
            ><span>{{ details?.rightOperandValue.toLowerCase() }}</span>
        </p>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'qbe-filter-detail',
    components: {},
    props: { details: { type: Object } },
    data() {
        return {}
    },
    async created() {},
    methods: {
     
    }
})
</script>
