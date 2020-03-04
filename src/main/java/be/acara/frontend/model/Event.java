package be.acara.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    private Long id;
    private LocalDateTime eventDate;
    private String name;
    private String description;
    private byte[] image;
    private String location;
    private String category;
    private BigDecimal price;
}