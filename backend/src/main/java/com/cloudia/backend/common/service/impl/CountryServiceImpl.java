package com.cloudia.backend.common.service.impl;

import com.cloudia.backend.common.mapper.CountryMapper;
import com.cloudia.backend.common.model.Country;
import com.cloudia.backend.common.service.CountryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {

    private final CountryMapper countryMapper;

    @Override
    public List<Country> getCountries() {
        return countryMapper.findAll();
    }
}
