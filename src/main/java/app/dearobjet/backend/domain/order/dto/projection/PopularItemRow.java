package app.dearobjet.backend.domain.order.dto.projection;

public interface PopularItemRow {
    Long getItemId();
    Long getQty();
    Long getSales();
}