package hiberspring.domain.dtos;

import com.google.gson.annotations.Expose;

import javax.validation.constraints.NotBlank;

public class EmployCardSeedDto {
    @Expose
    private String number;

    public EmployCardSeedDto() {
    }

    @NotBlank
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
