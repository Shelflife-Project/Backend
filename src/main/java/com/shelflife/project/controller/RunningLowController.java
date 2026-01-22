package com.shelflife.project.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.dto.runninglow.CreateSettingRequest;
import com.shelflife.project.dto.runninglow.EditSettingRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.exception.RunningLowExistsException;
import com.shelflife.project.service.RunningLowService;

import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/storages/{storageId}/runningLowSettings")
public class RunningLowController {
    @Autowired
    private RunningLowService runningLowService;

    @GetMapping
    public ResponseEntity<?> getSettings(@PathVariable long storageId, Authentication auth) {
        try {
            return ResponseEntity.ok(runningLowService.getSettingsForStorage(storageId, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createSetting(@PathVariable long storageId,
            @Valid @RequestBody CreateSettingRequest request,
            Authentication auth) {
        try {
            return ResponseEntity.ok(runningLowService.createSetting(storageId, request, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (RunningLowExistsException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("productId", "A setting for this product already exists in this storage"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editSetting(@PathVariable long id, @Valid @RequestBody EditSettingRequest request,
            Authentication auth) {
        try {
            return ResponseEntity.ok(runningLowService.editSetting(id, request, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(e.getMessage(), "Running low should be a positive number"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSetting(@PathVariable long id, Authentication auth) {
        try {
            runningLowService.deleteSetting(id, auth);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
