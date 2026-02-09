package com.cloudia.backend.common.controller;

import com.cloudia.backend.common.model.CodeMaster;
import com.cloudia.backend.common.service.CodeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CodeMasterController {

    private final CodeMasterService codeMasterService;

    @GetMapping("/common-codes")
    public ResponseEntity<List<CodeMaster>> getCommonCodes(@RequestParam("group") String group) {
        List<CodeMaster> codes = codeMasterService.getCodesByType(group);
        return ResponseEntity.ok(codes);
    }
}