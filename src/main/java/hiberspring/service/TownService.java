package hiberspring.service;

import hiberspring.domain.entities.Town;

import java.io.IOException;

public interface TownService {

    Boolean townsAreImported();

    String readTownsJsonFile() throws IOException;

    String importTowns(String townsFileContent) throws IOException;

    boolean existsByName(String name);

    Town findTownByName(String town);
}
