package be.acara.frontend.controller.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = false)
public class EventDtoList extends PageImpl<EventDto> {
    
    private Set<EventDto> popularEvents;
    
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public EventDtoList(@JsonProperty("content") List<EventDto> content,
                        @JsonProperty("number") int page,
                        @JsonProperty("size") int size,
                        @JsonProperty("totalElements") long total,
                        @JsonProperty("pageable") JsonNode pageable,
                        @JsonProperty("last") boolean last,
                        @JsonProperty("totalPages") int totalPages,
                        @JsonProperty("sort") JsonNode sort,
                        @JsonProperty("first") boolean first,
                        @JsonProperty("numberOfElements") int numberOfElements,
                        @JsonProperty("empty") boolean empty) {
        super(content, PageRequest.of(page, size), total);
    }
    
    public EventDtoList(List<EventDto> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }
    
    public EventDtoList(List<EventDto> content, Set<EventDto> popularEvents) {
        super(content);
        this.popularEvents = popularEvents;
    }
    
    public Set<EventDto> getPopularEvents() {
        return popularEvents;
    }
    
    public void setPopularEvents(Set<EventDto> popularEvents) {
        this.popularEvents = popularEvents;
    }
}
