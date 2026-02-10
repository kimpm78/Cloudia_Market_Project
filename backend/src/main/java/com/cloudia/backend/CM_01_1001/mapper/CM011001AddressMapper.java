package com.cloudia.backend.CM_01_1001.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.cloudia.backend.CM_01_1001.model.Address;

@Mapper
public interface CM011001AddressMapper {
    /**
     * 新しい住所情報を保存（会員登録）
     *
     * @param address 保存する住所情報オブジェクト
     */
    void insertAddress(Address address);
}
