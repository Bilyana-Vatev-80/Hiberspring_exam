package hiberspring.domain.dtos;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.xml.bind.annotation.*;

@XmlRootElement(name = "product")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductSeedDto {

    @XmlAttribute
    private String name;
    @XmlAttribute
    private Integer clients;
    @XmlElement
    private String branch;

    public ProductSeedDto() {
    }

    @NotBlank
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Positive
    public Integer getClients() {
        return clients;
    }

    public void setClients(Integer clients) {
        this.clients = clients;
    }

    @NotBlank
    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
