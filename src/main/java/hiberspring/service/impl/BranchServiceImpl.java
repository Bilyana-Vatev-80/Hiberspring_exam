package hiberspring.service.impl;

import com.google.gson.Gson;
import hiberspring.domain.dtos.BranchSeedDto;
import hiberspring.domain.entities.Branch;
import hiberspring.repository.BranchRepository;
import hiberspring.service.BranchService;
import hiberspring.service.TownService;
import hiberspring.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class BranchServiceImpl implements BranchService {
    private static final String BRANCH_FILE_PATH = "src/main/resources/files/branches.json";
    private final BranchRepository branchRepository;
    private final TownService townService;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    public BranchServiceImpl(BranchRepository branchRepository, TownService townService, Gson gson, ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.branchRepository = branchRepository;
        this.townService = townService;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean branchesAreImported() {
        return branchRepository.count() > 0;
    }

    @Override
    public String readBranchesJsonFile() throws IOException {
        return Files.readString(Path.of(BRANCH_FILE_PATH));
    }

    @Override
    public String importBranches(String branchesFileContent) throws IOException {
        StringBuilder result = new StringBuilder();

        BranchSeedDto[] branchSeedDtos = gson.fromJson(readBranchesJsonFile(), BranchSeedDto[].class);
        Arrays.stream(branchSeedDtos).filter(branchSeedDto -> {
            boolean isValid = validationUtil.isValid(branchSeedDto)
                    && !existsByName(branchSeedDto.getName())
                    && townService.existsByName(branchSeedDto.getTown());

            result
                    .append(isValid ? String.format("Successfully imported Branch %s.", branchSeedDto.getName())
                            : "invalid data")
                    .append(System.lineSeparator());
            return isValid;
        })
                .map(branchSeedDto -> {
                    Branch branch = modelMapper.map(branchSeedDto, Branch.class);
                    branch.setTown(townService.findTownByName(branchSeedDto.getTown()));

                    return branch;
                })
                .forEach(branchRepository::save);

        return result.toString();
    }

    @Override
    public boolean existsByName(String name) {
        return branchRepository.existsByName(name);
    }

    @Override
    public Branch findBranchByName(String branch) {
        return branchRepository.findBranchByName(branch);
    }
}
