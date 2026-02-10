package com.cloudia.backend.common.mapper;

import com.cloudia.backend.common.model.Country;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CountryMapper {
    List<Country> findAll();
}
