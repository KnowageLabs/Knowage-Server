# Theme Management

This functionality presents the user the possibility to create and set a specific theme for the tenant using the variables set in the css theme of the project.

Each variable is set in the **:root** element of the main app page, and it is changed using the `themeHelper.setTheme(variables)` method.

The variables to pass as a parameter in the setTheme method have a similar JSON syntax:

```json
{
    "--kn-color-default": "#aaaaaa",
    "--kn-color-primary": "#ffffff"
}
```

Notice the _--kn-_ prefix.

The application, in App.vue, will request the current theme during the creation process, and will return all customized variables for that tenant. All the properties not present in this list will not be changed in the app.

After the first loading all the customizations will be saved in the store, and will be refreshed just on browser refresh or during a new login.

## API

The following services are used in the component:

### Get Current Theme

In the main app.vue a service is called to get current theme and set it:

`GET /knowage/restful-services/thememanagement/current`

The service returns an item with all the variables to be changed from the default theme

```json
{
    "--kn-primary-color": "#aaaaaa",
    "--kn-secondary-color": "#000000"
}
```

### Get list of available themes

`GET /knowage/restful-services/thememanagement/`

The service returns an array of items with all the available themes and names. Each theme contains a config object containing all the variables to be changed from the default theme. Just one theme can be the active one.

```json
[
    {
        "id": 1,
        "themeName": "test theme name",
        "config": {
            "--kn-primary-color": "#aaaaaa",
            "--kn-secondary-color": "#000000"
        },
        "active": true
    }
]
```

### Add theme

To add the theme or to modify it in case the name is already present for the tenant:

`POST /knowage/restful-services/thememanagement/`

The payload should contain a json object with the selected theme like in the example:

```json
{
    "id": 1, // optional, if missing a new theme will be created.
    "themeName": "test theme name",
    "config": {
        "--kn-primary-color": "#aaaaaa",
        "--kn-secondary-color": "#000000"
    },
    "active": true
}
```

### Delete theme

To delete the theme:

`DELETE /knowage/restful-services/thememanagement?id=4`

The query attribute with the theme name is mandatory to select the theme to delete.

## Route

The page will be available on administrators menu and will present the following link: _knowage-vue/theme-management_.

## Version & License

This functionality is present from the 8.1.0 version and available only to EE users.
