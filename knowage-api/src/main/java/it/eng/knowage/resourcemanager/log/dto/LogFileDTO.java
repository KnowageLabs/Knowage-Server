package it.eng.knowage.resourcemanager.log.dto;

import it.eng.knowage.boot.validation.NotInvalidCharacters;
import it.eng.knowage.boot.validation.Xss;

import javax.validation.constraints.NotNull;
import java.io.File;

// per rapprensentare un file di log
public class LogFileDTO{

    @NotNull
    @Xss
    @NotInvalidCharacters

    private String name;
    private long size;
    private long lastModified;

    public LogFileDTO(File file) {
        this.name = file.getName();
        this.size = file.length();
        this.lastModified = file.lastModified();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CustomFile [name=" + name + "]";
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

//versione rivisitata e compattata dei metodi equals e hashCode contenuti in FileDTO.java
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LogFileDTO other = (LogFileDTO) obj;
        return size == other.size && lastModified == other.lastModified &&
               (name != null ? name.equals(other.name) : other.name == null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (int) (lastModified ^ (lastModified >>> 32));
        return result;
    }
}
