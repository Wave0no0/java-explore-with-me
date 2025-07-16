package ru.practicum.ewm.event.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.enums.EventStatus;
import ru.practicum.ewm.user.model.User;
import ru.practicum.util.DateTimeUtil;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"category", "initiator", "location"})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Event annotation must not be blank")
    private String annotation;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "confirmed_requests")
    private Integer confirmedRequests;

    @NotBlank(message = "Event description must not be blank")
    private String description;

    @Column(name = "event_date")
    @JsonFormat(pattern = DateTimeUtil.DATE_PATTERN)
    private LocalDateTime eventDate;

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiator;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "location_id")
    private Location location;

    private Boolean paid;

    @Column(name = "participant_limit")
    private Integer participantLimit;

    @Column(name = "created_on")
    @JsonFormat(pattern = DateTimeUtil.DATE_PATTERN)
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    @JsonFormat(pattern = DateTimeUtil.DATE_PATTERN)
    private LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    private Boolean requestModeration;

    @Enumerated(value = EnumType.STRING)
    private EventStatus status;

    @NotBlank(message = "Event title must not be blank")
    private String title;

    private Long views;
}
