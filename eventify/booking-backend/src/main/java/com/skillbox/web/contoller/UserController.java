package com.skillbox.web.contoller;

import com.skillbox.security.AppUserDetails;
import com.skillbox.service.UserService;
import com.skillbox.web.dto.user.NotificationSettingsResponse;
import com.skillbox.web.dto.user.UpdateNotificationSettingsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/notifications")
    public ResponseEntity<NotificationSettingsResponse> userNotifications(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(userService.userNotifications(userDetails.getUserId()));
    }

    @PutMapping("/notifications")
    public ResponseEntity<NotificationSettingsResponse> updateUserNotifications(
            @RequestBody UpdateNotificationSettingsRequest request
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(userService.updateUserNotifications(userDetails.getUserId(),request));
    }

    @DeleteMapping("/notifications")
    public ResponseEntity<Void> deleteNotification(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        userService.deleteNotifications(userDetails.getUserId());

        return ResponseEntity.noContent().build();
    }
}
