package it.eng.knowage.logmanager.resource.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import it.eng.knowage.boot.validation.FilesCheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
* DTO used by the REST endpoint to request a ZIP download of selected log files.
* - selectedLogsNames: list of filenames (strings) sent by the client.
* - Uses custom FilesCheck validation to ensure each entry is acceptable.
*/
public class DownloadLogFilesDTO {

    @JsonDeserialize(as = ArrayList.class, contentAs = String.class)
    @JsonSerialize(as = ArrayList.class, contentAs = String.class)
    @FilesCheck(message = "One or more files are not valid")
    private List<String> selectedLogsNames;

    public DownloadLogFilesDTO() {
        super();
    }

    public DownloadLogFilesDTO(List<String> selectedLogsNames) {
        super();
        this.selectedLogsNames = selectedLogsNames;
    }

    @Override
    public int hashCode() {
        return Objects.hash(selectedLogsNames);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        DownloadLogFilesDTO other = (DownloadLogFilesDTO) obj;
        return Objects.equals(selectedLogsNames, other.selectedLogsNames);
    }

    public List<String> getSelectedLogsNames() {
        return selectedLogsNames;
    }

    public void setSelectedLogsNames(List<String> selectedLogsNames) {
        this.selectedLogsNames = selectedLogsNames;
    }
}
