package it.eng.spagobi.profiling.bo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "Esito dell'operazione di inserimento per un singolo utente")
public class UserBOResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID dell'utente processato", example = "mrossi_99")
    private String userId;
    @Schema(description = "Indica se l'inserimento è andato a buon fine", example = "true")
    private boolean success;
    @Schema(description = "ID numerico generato dal database in caso di successo", example = "10521", nullable = true)
    private Integer createdUserId;
    @Schema(description = "Messaggio informativo o dettaglio dell'errore", example = "User processed successfully")
    private String message;

    // --------------------
    // Costruttori
    // --------------------

    public UserBOResult() {
        // costruttore vuoto richiesto da Jackson
    }

    public UserBOResult(String userId, boolean success, Integer createdUserId, String message) {
        this.userId = userId;
        this.success = success;
        this.createdUserId = createdUserId;
        this.message = message;
    }

    // --------------------
    // Getter & Setter
    // --------------------

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getCreatedUserId() {
        return createdUserId;
    }

    public void setCreatedUserId(Integer createdUserId) {
        this.createdUserId = createdUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // --------------------
    // Utility opzionali
    // --------------------

    @Override
    public String toString() {
        return "BulkUserInsertResult{" +
                "userId='" + userId + '\'' +
                ", success=" + success +
                ", createdUserId=" + createdUserId +
                ", message='" + message + '\'' +
                '}';
    }
}
