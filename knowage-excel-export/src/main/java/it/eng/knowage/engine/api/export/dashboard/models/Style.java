package it.eng.knowage.engine.api.export.dashboard.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Sheet;

@NoArgsConstructor
@Getter
@Setter
public class Style {
    private Sheet sheet;
    private String alignItems;
    private String justifyContent;
    private String backgroundColor;
    private String color;
    private String fontFamily;
    private String fontSize;
    private String fontStyle;
    private String fontWeight;
}
