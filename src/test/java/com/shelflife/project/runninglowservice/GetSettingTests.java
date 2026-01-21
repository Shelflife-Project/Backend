package com.shelflife.project.runninglowservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.RunningLowSetting;
import com.shelflife.project.repository.RunningLowRepository;
import com.shelflife.project.service.RunningLowService;

@ExtendWith(MockitoExtension.class)
public class GetSettingTests {
    @Mock
    private RunningLowRepository repository;

    @InjectMocks
    private RunningLowService service;

    @Test
    void successfulGet() {
        RunningLowSetting setting = new RunningLowSetting();

        doReturn(Optional.of(setting)).when(repository).findById(1L);

        assertDoesNotThrow(() -> service.getSetting(1));
        assertEquals(setting, service.getSetting(1));
    }

    @Test
    void throwsNotFound() {
        doReturn(Optional.empty()).when(repository).findById(1L);

        assertThrows(ItemNotFoundException.class, () -> service.getSetting(1));
    }
}
