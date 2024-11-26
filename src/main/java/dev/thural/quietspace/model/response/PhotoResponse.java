package dev.thural.quietspace.model.response;

import jakarta.persistence.Lob;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponse {
    private String name;
    private String type;
    @Lob
    private byte[] data;
}
