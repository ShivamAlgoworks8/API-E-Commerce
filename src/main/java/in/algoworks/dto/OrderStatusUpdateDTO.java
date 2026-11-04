package in.algoworks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Request body used by the Admin Portal to move an order to a new status.
public class OrderStatusUpdateDTO {

    @NotBlank(message = "Status is required")
    private String status;

    @Size(max = 250, message = "Note must be at most 250 characters")
    private String note;

    public OrderStatusUpdateDTO() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
