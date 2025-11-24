package com.skillbox.mapper;

import com.skillbox.entity.NotificationSettings;
import com.skillbox.web.dto.user.NotificationSettingsResponse;
import com.skillbox.web.dto.user.UpdateNotificationSettingsRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationSettingsResponse toDto(NotificationSettings entity);
    NotificationSettings toEntity(UpdateNotificationSettingsRequest request);
}
