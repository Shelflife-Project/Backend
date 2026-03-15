package com.shelflife.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.dto.runninglow.RunningLowNotification;
import com.shelflife.project.model.User;
import com.shelflife.project.service.RunningLowService;
import com.shelflife.project.service.UserService;

@RestController
@RequestMapping("/api")
public class RunningLowController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private RunningLowService runningLowService;

    @GetMapping("/storages/{storageId}/runninglow")
    public ResponseEntity<List<RunningLowNotification>> getRunningLow(@PathVariable long storageId,
            Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(runningLowService.getRunningLowInStorage(storageId, user));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/runninglow")
    public String getAggregatedRunningLow(Authentication auth) {
        return new String();
    }
    
}
