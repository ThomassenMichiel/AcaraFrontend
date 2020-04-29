package be.acara.frontend.model;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventModel {

    private Long id;

    @Future(message = "Date must be in the future")
    @NotNull(message = "Field cannot be empty")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime eventDate;

    @NotBlank(message = "NotBlank.event.name")
    private String name;

    @NotBlank(message = "NotBlank.event.description")
    private String description;

    private byte[] image;

    @NotBlank(message = "NotBlank.event.location")
    private String location;

    @NotBlank(message = "NotBlank.event.category")
    private String category;

    @NotNull(message = "Minimum price is 1 euro")
    @Min(value = 1, message = "Minimum price is 1 euro")
    private BigDecimal price;

    private String youtubeUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventModel)) return false;
        EventModel event = (EventModel) o;
        return Objects.equals(getId(), event.getId()) &&
                Objects.equals(getEventDate(), event.getEventDate()) &&
                Objects.equals(getName(), event.getName()) &&
                Objects.equals(getDescription(), event.getDescription()) &&
                Arrays.equals(getImage(), event.getImage()) &&
                Objects.equals(getLocation(), event.getLocation()) &&
                Objects.equals(getCategory(), event.getCategory()) &&
                Objects.equals(getPrice(), event.getPrice());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getId(), getEventDate(), getName(), getDescription(), getLocation(), getCategory(), getPrice());
        result = 31 * result + Arrays.hashCode(getImage());
        return result;
    }
}
