<template>
  <div class="galleryHint hint">
      <Card class="galleryHintContent">
        <template #title>
            <i class="far fa-life-ring galleryHintIcon"></i> {{$t('managers.gallery.title')}}
        </template>
        <template #content>
            {{$t('managers.gallery.hint')}}
        </template>
    </Card>
  </div>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'

    export default defineComponent({
        name: 'gallery-hint',

    })
</script>

<style lang="scss" scoped>
    .galleryHint{
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        height: 100%;

        .galleryHintContent{
            border-radius: 0;
        }

        .galleryHintIcon{
            font-size: 3rem;
        }
    }

</style>