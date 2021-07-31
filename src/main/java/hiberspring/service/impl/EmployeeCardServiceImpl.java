package hiberspring.service.impl;

import com.google.gson.Gson;
import hiberspring.domain.dtos.EmployCardSeedDto;
import hiberspring.domain.entities.EmployeeCard;
import hiberspring.repository.EmployeeCardRepository;
import hiberspring.service.EmployeeCardService;
import hiberspring.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class EmployeeCardServiceImpl implements EmployeeCardService {
    private final static String EMPLOYEECARD_FILE_PATH = "src/main/resources/files/employee-cards.json";
    private final EmployeeCardRepository employeeCardRepository;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    public EmployeeCardServiceImpl(EmployeeCardRepository employeeCardRepository, Gson gson, ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.employeeCardRepository = employeeCardRepository;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean employeeCardsAreImported() {
        return employeeCardRepository.count() > 0;
    }

    @Override
    public String readEmployeeCardsJsonFile() throws IOException {
        return Files.readString(Path.of(EMPLOYEECARD_FILE_PATH));
    }

    @Override
    public String importEmployeeCards(String employeeCardsFileContent) throws IOException {
        StringBuilder result = new StringBuilder();

        EmployCardSeedDto[] employCardSeedDtos = gson.fromJson(readEmployeeCardsJsonFile(), EmployCardSeedDto[].class);

        Arrays.stream(employCardSeedDtos).filter(employCardSeedDto -> {
            boolean isValid = validationUtil.isValid(employCardSeedDto)
                    && !existsByNumber(employCardSeedDto.getNumber());

            result
                    .append(isValid ? String.format("Successfully imported Employee Card %s.", employCardSeedDto.getNumber())
                            : "Invalid data")
                    .append(System.lineSeparator());
            return isValid;
        })
                .map(employCardSeedDto -> modelMapper.map(employCardSeedDto, EmployeeCard.class))
                .forEach(employeeCardRepository::save);
        return result.toString();
    }

    @Override
    public boolean existsByNumber(String number) {
        return employeeCardRepository.existsByNumber(number);
    }

    @Override
    public EmployeeCard findByNumber(String card) {
        return employeeCardRepository.findByNumber(card);
    }
}
