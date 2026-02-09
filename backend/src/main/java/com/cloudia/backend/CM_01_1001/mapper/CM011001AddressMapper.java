package com.cloudia.backend.CM_01_1001.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.cloudia.backend.CM_01_1001.model.Address;

@Mapper
public interface CM011001AddressMapper {
    /**
     * 새로운 주소 정보 저장 (회원가입)
     * 
     * @param address 저장할 주소 정보 객체
     */
    void insertAddress(Address address);
}
