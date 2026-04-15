package app.dearobjet.backend.domain.user.dto;

public record UpdateBusinessProfileRequest(
        String instagramId,
        String phoneNumber,
        String businessPhoneNumber,
        String bankName,
        String bankAccountNumber,
        String accountHolder,
        String taxInvoiceEmail
) {
}
