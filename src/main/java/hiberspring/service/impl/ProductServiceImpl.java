package hiberspring.service.impl;

import hiberspring.domain.dtos.ProductSeedRootDto;
import hiberspring.domain.entities.Product;
import hiberspring.repository.ProductRepository;
import hiberspring.service.BranchService;
import hiberspring.service.ProductService;
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
public class ProductServiceImpl implements ProductService {
    private static final String PRODUCT_FILE_PATH = "src/main/resources/files/products.xml";
    private final ProductRepository productRepository;
    private final BranchService branchService;
    private final XmlParser xmlParser;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    public ProductServiceImpl(ProductRepository productRepository, BranchService branchService, XmlParser xmlParser, ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.branchService = branchService;
        this.xmlParser = xmlParser;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean productsAreImported() {
        return productRepository.count() > 0;
    }

    @Override
    public String readProductsXmlFile() throws IOException {
        return Files.readString(Path.of(PRODUCT_FILE_PATH));
    }

    @Override
    public String importProducts() throws JAXBException, FileNotFoundException {
        StringBuilder result = new StringBuilder();

        ProductSeedRootDto productSeedRootDto = xmlParser.fromFile(PRODUCT_FILE_PATH, ProductSeedRootDto.class);

        productSeedRootDto.getProducts()
                .stream()
                .filter(productSeedDto -> {
                    boolean isValid = validationUtil.isValid(productSeedDto)
                            && !existsByName(productSeedDto.getName())
                            && branchService.existsByName(productSeedDto.getBranch());
                    result
                            .append(isValid ? String.format("Successfully imported Product %s.", productSeedDto.getName())
                                    : "Invalid data")
                            .append(System.lineSeparator());

                    return isValid;
                })
                .map(productSeedDto -> {
                    Product product = modelMapper.map(productSeedDto, Product.class);
                    product.setBranch(branchService.findBranchByName(productSeedDto.getBranch()));

                    return product;
                })
                .forEach(productRepository::save);
        return result.toString();
    }

    @Override
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }
}
