package dev.thural.quietspacebackend.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Date;
@Getter
@Setter
@Builder
public class ErrorResponse {

    @JsonInclude (JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private Date timestamp;

    @JsonInclude (JsonInclude.Include.NON_NULL)
    private int code;

    @JsonInclude (JsonInclude.Include.NON_NULL)
    private String status;

    @JsonInclude (JsonInclude.Include.NON_NULL)
    private String message;

    @JsonInclude (JsonInclude.Include.NON_NULL)
    private String stackTrace;

    @JsonInclude (JsonInclude.Include.NON_NULL)
    private Object data;
}
