package com.cloudia.backend.CM_01_1008.mapper;

import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_01_1008.model.DeliveryAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface CM011008Mapper {
    User findUserByLoginId(String loginId);

    List<DeliveryAddress> findAddressesByMemberNumber(String memberNumber);

    int countAddressesByMemberNumber(String memberNumber);

    void insertAddress(DeliveryAddress address);

    void resetDefaultAddress(@Param("memberNumber") String memberNumber);

    void updateAddress(DeliveryAddress address);

    Optional<DeliveryAddress> findAddressById(Integer addressId);

    void deleteAddressSoft(Integer addressId);

    void updateAddressDefaultStatus(@Param("addressId") Integer addressId,
            @Param("isDefault") boolean isDefault,
            @Param("updatedBy") String updatedBy);
}
