package be.acara.frontend.controller;

import be.acara.frontend.controller.dto.EventDto;
import be.acara.frontend.model.EventModel;
import be.acara.frontend.model.EventModelList;
import be.acara.frontend.model.SearchModel;
import be.acara.frontend.service.EventService;
import be.acara.frontend.service.UserService;
import be.acara.frontend.service.mapper.EventMapper;
import be.acara.frontend.util.ImageUtil;
import be.acara.frontend.util.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/events")
public class EventController {
    private final EventMapper mapper;
    private final EventService eventService;
    private final UserService userService;
    
    private static final String EVENT_DETAILS_LOCATION = "eventDetails";
    private static final String EVENT_LIST_LOCATION = "eventList";
    private static final String ADD_EVENT_LOCATION = "addEvent";
    private static final String EDIT_EVENT_LOCATION = "editEvent";
    private static final String REDIRECT_EVENTS = "redirect:/events";
    private static final String ATTRIBUTE_EVENT = "event";
    private static final String ATTRIBUTE_EVENTS = "events";
    private static final String ATTRIBUTE_LIKED_EVENTS = "likedEvents";
    private static final String ATTRIBUTE_EVENT_IMAGE = "eventImage";
    private static final String ATTRIBUTE_SEARCH = "searchModel";
    private static final String ATTRIBUTE_ID = "id";
    
    
    @Autowired
    public EventController(EventMapper mapper, EventService eventService, UserService userService) {
        this.mapper = mapper;
        this.eventService = eventService;
        this.userService = userService;
    }
    
    @GetMapping("/detail/{id}")
    public String displayEvent(@PathVariable(ATTRIBUTE_ID) Long id, Model model) {
        addLocalDateTime(model);
        EventModel event = mapper.eventDtoToEventModel(eventService.getEvent(id));
        model.addAttribute(ATTRIBUTE_EVENT, event);
        model.addAttribute(ATTRIBUTE_EVENT_IMAGE, ImageUtil.convertToBase64(event.getImage()));
        return EVENT_DETAILS_LOCATION;
    }
    
    @GetMapping
    public String findAllEvents(Model model,
                                @RequestParam(name = "page", defaultValue = "1", required = false) int page,
                                @RequestParam(name = "size", defaultValue = "10", required = false) int size,
                                @RequestParam(name = "sort", defaultValue = "eventDate", required = false) String sort,
                                @RequestParam Map<String, String> params) {
        params.entrySet().removeIf(e -> e.getValue().isEmpty()); //remove empty values from the set to avoid errors when parsing dates or bigDecimals
        addCategories(model);
        addLocalDateTime(model);
        model.addAttribute(ATTRIBUTE_SEARCH, createSearchModel(params));
        
        if ("UNSORTED".equals(sort)) {
            sort = "";
        }
        
        EventModelList eventModelList = mapper.eventDtoListToEventModelList(eventService.findAllEvents(params, page - 1, size < 1 ? 1 : size, sort));
        model.addAttribute(ATTRIBUTE_EVENTS, eventModelList);
        PaginationUtil.addPageNumbers(eventModelList, model, "pageNumbers");
        
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)) {
            Long id = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
            EventModelList likedEventModelList = mapper.eventDtoListToEventModelList(eventService.getEventsThatUserLiked(id, page - 1, size < 1 ? 1 : size));
            model.addAttribute(ATTRIBUTE_LIKED_EVENTS, likedEventModelList);
        }
        
        return EVENT_LIST_LOCATION;
    }
    
    @GetMapping("/new")
    public String displayAddEventForm(Model model) {
        model.addAttribute(ATTRIBUTE_EVENT, new EventModel());
        addCategories(model);
        return ADD_EVENT_LOCATION;
    }
    
    @PostMapping("/new")
    public String handleAddEventForm(@Valid @ModelAttribute(ATTRIBUTE_EVENT) EventModel event,
                                     BindingResult br,
                                     @RequestParam(ATTRIBUTE_EVENT_IMAGE) MultipartFile eventImage,
                                     Model model) throws IOException {
        if (br.hasErrors()) {
            addCategories(model);
            return ADD_EVENT_LOCATION;
        }
        event.setImage(eventImage.getBytes());
        eventService.addEvent(mapper.eventModelToEventDto(event));
        return REDIRECT_EVENTS;
    }
    
    @GetMapping("/{id}")
    public String displayEditEventForm(@PathVariable(ATTRIBUTE_ID) Long id, Model model) {
        EventModel event = mapper.eventDtoToEventModel(eventService.getEvent(id));
        if(event.getYoutubeId() != null && !event.getYoutubeId().contains("https://www.youtube.com/watch?v=")) {
            event.setYoutubeId("https://www.youtube.com/watch?v=" + event.getYoutubeId());
        }
        model.addAttribute(ATTRIBUTE_EVENT, event);
        model.addAttribute(ATTRIBUTE_EVENT_IMAGE, ImageUtil.convertToBase64(event.getImage()));
        addCategories(model);
        return EDIT_EVENT_LOCATION;
    }
    
    @PostMapping("/{id}")
    public String handleEditEventForm(@Valid @ModelAttribute(ATTRIBUTE_EVENT) EventModel event,
                                      BindingResult br,
                                      @RequestParam(ATTRIBUTE_EVENT_IMAGE) MultipartFile eventImage,
                                      Model model) throws IOException {
        EventDto eventDtoFromDb = eventService.getEvent(event.getId());
        EventModel eventFromDb = mapper.eventDtoToEventModel(eventDtoFromDb);
        if (br.hasErrors()) {
            addCategories(model);
            model.addAttribute(ATTRIBUTE_EVENT, event);
            model.addAttribute(ATTRIBUTE_EVENT_IMAGE, ImageUtil.convertToBase64(eventDtoFromDb.getImage()));
            return EDIT_EVENT_LOCATION;
        }
        event.setImage(eventImage.getBytes());
        eventService.editEvent(eventFromDb.getId(), mapper.eventModelToEventDto(event));
        return REDIRECT_EVENTS;
    }
    
    private SearchModel createSearchModel(Map<String, String> params) {
        return SearchModel.builder()
                .name(params.get("name"))
                .location(params.get("location"))
                .minPrice(params.get("minPrice"))
                .maxPrice(params.get("maxPrice"))
                .category(params.get("category"))
                .startDate(params.get("startDate"))
                .endDate(params.get("endDate"))
                .build();
    }
    
    
    @GetMapping("/delete/{id}")
    public String deleteEvent(@PathVariable(ATTRIBUTE_ID) Long id) {
        eventService.delete(id);
        return REDIRECT_EVENTS;
    }
    
    
    private void addCategories(Model model) {
        model.addAttribute("categoryList", eventService.getCategories());
    }
    
    private void addLocalDateTime(Model model) {
        model.addAttribute("now", LocalDateTime.now());
    }
}
