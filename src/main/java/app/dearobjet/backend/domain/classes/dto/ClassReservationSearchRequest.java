package app.dearobjet.backend.domain.classes.dto;

public class ClassReservationSearchRequest {

    private String status;
    private int page = 1;
    private int size = 20;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPage() {
        return Math.max(page, 1);
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return Math.max(size, 1);
    }

    public void setSize(int size) {
        this.size = size;
    }
}
