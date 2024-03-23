package br.com.elotech.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class DataNascimentoValidator implements ConstraintValidator<DataNascimento, LocalDate> {

    public static final LocalDate TODAY = LocalDate.now();

    @Override
    public boolean isValid(LocalDate dataNascimento, ConstraintValidatorContext constraintValidatorContext) {
        return dataNascimento != null && (dataNascimento.isEqual(TODAY) || dataNascimento.isBefore(TODAY));
    }

}
