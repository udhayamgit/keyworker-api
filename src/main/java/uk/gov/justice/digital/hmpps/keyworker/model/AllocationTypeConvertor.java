package uk.gov.justice.digital.hmpps.keyworker.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

// JPA Attribute Convertor
@Converter
public class AllocationTypeConvertor implements AttributeConverter<AllocationType,String> {
    @Override
    public String convertToDatabaseColumn(final AllocationType attribute) {
        return (attribute != null) ? attribute.getTypeCode() : null;
    }

    @Override
    public AllocationType convertToEntityAttribute(final String dbData) {
        return AllocationType.get(dbData);
    }
}
