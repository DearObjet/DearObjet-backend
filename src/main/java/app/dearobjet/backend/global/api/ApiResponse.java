package app.dearobjet.backend.global.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class ApiResponse<T> {
    private final T data;
}
