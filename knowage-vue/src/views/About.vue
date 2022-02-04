<template>
  <div class="about">
    <Toolbar>
        <template #start>
            About
        </template>

        <template #end>
            <Button icon="pi pi-search" class="p-mr-2" />
            <Button icon="pi pi-times" class="p-button-danger p-button-text" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-1">
      <div class="p-col">
          <Card>
            <template #title>
                Advanced Card
            </template>
            <template #content>
                Lorem ipsum dolor sit amet, consectetur adipisicing elit. Inventore sed consequuntur error repudiandae numquam deserunt
                quisquam repellat libero asperiores earum nam nobis, culpa ratione quam perferendis esse, cupiditate neque quas!
            </template>
            <template #footer>
                <Button icon="pi pi-check" label="Save" />
                <Button icon="pi pi-times" label="Cancel" class="p-button-secondary" style="margin-left: .5em" />
            </template>
        </Card>
      </div>
    </div>
    
  </div>
</template>

<script>
import Toolbar from 'primevue/toolbar';
export default {
  name: 'About',
  components: {
    Toolbar
  }
  
}
</script>
