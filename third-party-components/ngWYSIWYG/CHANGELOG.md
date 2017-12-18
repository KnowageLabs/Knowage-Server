<a name="0.3.0"></a>

## Bug fixes

0.3.0 XSS vulnerability fix from (2015-09-23) happen to be replacing inline styles because $sanitize was utilized. I will have to think some good workaround for this but for now
I recommend using configuration for the XSS. In other words if you want to sanitize the user's input, use sanitize: true in configuration

## Imprevements

- added configuration for the editor
- added configurable toolbar

<a name="0.2.1"></a>
0.2.1 XSS vulnerability fix (2015-09-23)

## Bug fixes

- used $sanitize to prevent XSS when render from the model to view. Gret read about it: https://www.blackhat.com/docs/eu-14/materials/eu-14-Javed-Revisiting-XSS-Sanitization-wp.pdf

## Imprevements

- added changelog


