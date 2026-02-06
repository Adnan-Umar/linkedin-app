package com.adnanumar.linkedin.connections_service.service;

import com.adnanumar.linkedin.connections_service.entity.Person;
import com.adnanumar.linkedin.connections_service.repository.PersonRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class ConnectionsService {

    final PersonRepository personRepository;

    public List<Person> getFirstDegreeConnections(Long userId) {
        log.info("Getting first degree connections for user {}", userId);
        return personRepository.getFirstDegreeConnections(userId);
    }

}
