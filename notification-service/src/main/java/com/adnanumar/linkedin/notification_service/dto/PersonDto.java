package com.adnanumar.linkedin.notification_service.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PersonDto {

    Long id;

    Long userId;

    String name;

}
