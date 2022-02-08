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

### Route

The page will be available on administrators menu and will present the following link: _knowage-vue/theme-management_.

### Version & License

This functionality is present from the 8.1.0 version and available only to EE users.
