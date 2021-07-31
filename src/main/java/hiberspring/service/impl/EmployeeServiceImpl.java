package hiberspring.service.impl;

import hiberspring.domain.dtos.EmployeeSeedRootDto;
import hiberspring.domain.entities.Employee;
import hiberspring.repository.EmployeeRepository;
import hiberspring.service.BranchService;
import hiberspring.service.EmployeeCardService;
import hiberspring.service.EmployeeService;
import hiberspring.util.ValidationUtil;
import hiberspring.util.XmlParser;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private static final String EMPLOYEE_FILE_PATH = "src/main/resources/files/employees.xml";
    private final EmployeeRepository employeeRepository;
    private final XmlParser xmlParser;
    private final BranchService branchService;
    private final EmployeeCardService employeeCardService;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, XmlParser xmlParser, BranchService branchService, EmployeeCardService employeeCardService, ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.employeeRepository = employeeRepository;
        this.xmlParser = xmlParser;
        this.branchService = branchService;
        this.employeeCardService = employeeCardService;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean employeesAreImported() {
        return employeeRepository.count() > 0;
    }

    @Override
    public String readEmployeesXmlFile() throws IOException {
        return Files.readString(Path.of(EMPLOYEE_FILE_PATH));
    }

    @Override
    public String importEmployees() throws JAXBException, FileNotFoundException {
        StringBuilder result = new StringBuilder();

        EmployeeSeedRootDto employeeSeedRootDto = xmlParser.fromFile(EMPLOYEE_FILE_PATH, EmployeeSeedRootDto.class);

        employeeSeedRootDto.getEmployees()
                .stream()
                .filter(employeeSeedDto -> {
                    boolean isValid = validationUtil.isValid(employeeSeedDto)
                            && employeeCardService.existsByNumber(employeeSeedDto.getCard())
                            && branchService.existsByName(employeeSeedDto.getBranch());

                    result
                            .append(isValid ? String.format("Successfully imported Employee %s %s.", employeeSeedDto.getFirstName(),employeeSeedDto.getLastName())
                                    : "Error: Invalid data.")
                            .append(System.lineSeparator());
                    return isValid;
                })
                .map(employeeSeedDto -> {
                    Employee employee = modelMapper.map(employeeSeedDto, Employee.class);
                    employee.setBranch(branchService.findBranchByName(employeeSeedDto.getBranch()));
                    employee.setCard(employeeCardService.findByNumber(employeeSeedDto.getCard()));

                    return employee;
                })
                .forEach(employeeRepository::save);
        return result.toString();
    }

    @Override
    public String exportProductiveEmployees() {
        StringBuilder result = new StringBuilder();

        employeeRepository.findAllByBranchWithProduct()
                .forEach(employee -> {
                    result
                            .append(String.format("Name: %s\n" +
                                    "Position: %s\n" +
                                    "Card Number: %s" +
                                    "-------------------------\n",
                                    employee.getFirstName() + " " + employee.getLastName(),
                                    employee.getPosition(),
                                    employee.getCard().getNumber()));
                });
        return result.toString();
    }
}
