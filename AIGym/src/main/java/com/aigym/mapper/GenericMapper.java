package com.aigym.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GenericMapper {

    private final ModelMapper modelMapper;

    public GenericMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D, T> D mapToDto(T entity, Class<D> outClass) {
        if (entity == null) {
            return null;
        }
        return modelMapper.map(entity, outClass);
    }

    public <D, T> T mapToEntity(D dto, Class<T> outClass) {
        if (dto == null) {
            return null;
        }
        return modelMapper.map(dto, outClass);
    }

    public <D, T> List<D> mapListToDto(List<T> entityList, Class<D> outClass) {
        if (entityList == null) {
            return null;
        }
        return entityList.stream()
                .map(entity -> mapToDto(entity, outClass))
                .collect(Collectors.toList());
    }

    public <D, T> List<T> mapListToEntity(List<D> dtoList, Class<T> outClass) {
        if (dtoList == null) {
            return null;
        }
        return dtoList.stream()
                .map(dto -> mapToEntity(dto, outClass))
                .collect(Collectors.toList());
    }
}
