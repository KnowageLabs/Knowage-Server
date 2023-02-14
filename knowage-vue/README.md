# knowage-vue

## Project setup

```
npm install
```

### Local environment variables

Create an **.env.local** file in the project root and set the following properties depending on your environment:

-   VITE_API_URL=http://localhost:8080

### Compiles and hot-reloads for development

```
npm run dev
```

### Compiles and minifies for production

```
npm run build
```

### Jest Unit-tests

```
npm run test:unit
```

## Project Structure (src)

```
.
├── assets
│   └── ...
├── components
│   ├── knMenu
│   │   ├── KnMenu.spec.js
│   │   ├── KnMenu.vue
│   │   └── KnMenuItem.vue
│   └── ...
├── helpers
├── i18n
│   ├── en_BG.json
│   ├── it_IT.json
│   └── ...
├── modules
│   ├── managers
│   │   ├── galleryManagement
│   │   │   ├── GalleryManagement.routes.js
│   │   │   ├── GalleryManagement.spec.js
│   │   │   └── GalleryManagement.vue
│   │   └── managers.routes.js
│   └── ...
│   └── shared
│       ├── 404.vue
│       └── IframeRenderer.vue
├── App.i18n.js
├── App.routes.js
├── App.spec.js
├── App.store.js
├── App.vue
├── main.ts
```
