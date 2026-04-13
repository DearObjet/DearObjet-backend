package app.dearobjet.backend.domain.classes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateClassRequest {

    @NotBlank
    private String className;

    @NotBlank
    private String classDescription;

    @NotNull
    @DecimalMin("0.0")
    private Double price;

    @NotNull
    @Min(1)
    private Integer maxCapacity;

    private String notes;
}
