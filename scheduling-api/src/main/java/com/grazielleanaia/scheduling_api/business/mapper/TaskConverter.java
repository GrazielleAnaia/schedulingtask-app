package com.grazielleanaia.scheduling_api.business.mapper;

import com.grazielleanaia.scheduling_api.business.dto.TaskRequestDTO;
import com.grazielleanaia.scheduling_api.business.dto.TaskResponseDTO;
import com.grazielleanaia.scheduling_api.business.dto.TaskUpdateDTO;
import com.grazielleanaia.scheduling_api.infrastructure.entity.TaskEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")

public interface TaskConverter {

    TaskResponseDTO toTaskResponseDTO(TaskEntity taskEntity);

    //Even if someone adds these fields in DTO layer, they will not map
    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "customerEmail", ignore = true)
    @Mapping(target = "notificationStatusEnum", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    TaskEntity toTaskEntity(TaskRequestDTO taskRequestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTask(@MappingTarget TaskEntity entity, TaskUpdateDTO update);

}
