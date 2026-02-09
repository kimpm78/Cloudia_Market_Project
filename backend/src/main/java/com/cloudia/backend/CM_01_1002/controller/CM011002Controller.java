package com.cloudia.backend.CM_01_1002.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cloudia.backend.CM_01_1002.constants.CM011002MesaageConstant;
import com.cloudia.backend.CM_01_1002.service.CM011002Service;
import com.cloudia.backend.common.model.ResponseModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController("CM_01_1002")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/guest")
public class CM011002Controller {

    private final CM011002Service CM_01_1002Serivce;

    /**
     * ID検索 認証コード送信
     */
    @PostMapping("/find-id/send-code")
    public ResponseEntity<ResponseModel<Map<String, String>>> sendCode(@RequestBody Map<String, String> request) {
        log.info(CM011002MesaageConstant.FIND_ID_CODE_SEND_START);

        // Serviceの戻り値型に合わせてResponseModelでラップして返却
        ResponseEntity<ResponseModel<Map<String, String>>> response = CM_01_1002Serivce
                .sendVerificationCodeForFindId(request.get("email"));

        log.info(CM011002MesaageConstant.FIND_ID_CODE_SEND_END);
        return response;
    }

    /**
     * ID検索 認証およびID返却
     */
    @PostMapping("/find-id/verify")
    public ResponseEntity<ResponseModel<Map<String, Object>>> verifyAndFindId(
            @RequestBody Map<String, String> request) {
        log.info(CM011002MesaageConstant.FIND_ID_VERIFY_START);

        String email = request.get("email");
        String code = request.get("code");

        ResponseEntity<ResponseModel<Map<String, Object>>> response = CM_01_1002Serivce.verifyAndFindId(email, code);

        log.info(CM011002MesaageConstant.FIND_ID_VERIFY_END);
        return response;
    }
}