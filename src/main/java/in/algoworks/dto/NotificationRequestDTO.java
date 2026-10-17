
package in.algoworks.dto;

import jakarta.validation.constraints.NotBlank;

public class NotificationRequestDTO {

    @NotBlank(message = "Notification title is required")
    private String title;

    @NotBlank(message = "Notification message is required")
    private String message;

    @NotBlank(message = "Notification type is required")
    private String type;

    @NotBlank(message = "Notification status is required")
    private String status;

    private Boolean read;

    private String date;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
