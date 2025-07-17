package ru.practicum.ewm.event.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "locations")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Latitude must not be null")
    private Float lat;

    @NotNull(message = "Longitude must not be null")
    private Float lon;
}
