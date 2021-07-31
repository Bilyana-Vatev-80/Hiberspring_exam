package hiberspring.service.impl;

import com.google.gson.Gson;
import hiberspring.domain.dtos.TownSeedDto;
import hiberspring.domain.entities.Town;
import hiberspring.repository.TownRepository;
import hiberspring.service.TownService;
import hiberspring.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class TownServiceImpl implements TownService {
    private static String TOWN_FILE_PATH = "src/main/resources/files/towns.json";
    private final TownRepository townRepository;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    public TownServiceImpl(TownRepository townRepository, Gson gson, ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.townRepository = townRepository;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean townsAreImported() {
        return townRepository.count() > 0;
    }

    @Override
    public String readTownsJsonFile() throws IOException {
        return Files.readString(Path.of(TOWN_FILE_PATH));
    }

    @Override
    public String importTowns(String townsFileContent) throws IOException {
        StringBuilder result = new StringBuilder();

        TownSeedDto[] townSeedDtos = gson.fromJson(readTownsJsonFile(), TownSeedDto[].class);

        Arrays.stream(townSeedDtos).filter(townSeedDto -> {
            boolean isValid = validationUtil.isValid(townSeedDto)
                    && !existsByName(townSeedDto.getName());

            result
                    .append(isValid ? String.format("Successfully imported Town %s.", townSeedDto.getName())
                            : "Invalid town")
                    .append(System.lineSeparator());

            return isValid;
        })
                .map(townSeedDto -> modelMapper.map(townSeedDto, Town.class))
                .forEach(townRepository::save);
        return result.toString();
    }

    @Override
    public boolean existsByName(String name) {
        return townRepository.existsByName(name);
    }

    @Override
    public Town findTownByName(String town) {
        return townRepository.findTownByName(town);
    }
}
