package com.cloudia.backend.CM_01_1010.controller;

import com.cloudia.backend.CM_01_1003.constants.CM011003MessageConstant;
import com.cloudia.backend.CM_01_1010.constants.CM011010MessageConstant;
import com.cloudia.backend.CM_01_1010.model.Unsubscribe;
import com.cloudia.backend.CM_01_1010.service.CM011010Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Slf4j
@RequiredArgsConstructor
public class CM011010Controller {

    private final CM011010Service cm011010Service;

    @PostMapping("/unsubscribe")
    public ResponseEntity<Map<String, Object>> unsubscribe(@RequestBody Unsubscribe request) {
        log.info(CM011010MessageConstant.UNSUBSCRIBE_REQUEST_START);
        ResponseEntity<Map<String, Object>> response = cm011010Service.unsubscribe(request);
        log.info(CM011010MessageConstant.UNSUBSCRIBE_REQUEST_END);
        return response;
    }
}
