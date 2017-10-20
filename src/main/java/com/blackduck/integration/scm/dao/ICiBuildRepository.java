package com.blackduck.integration.scm.dao;

import org.springframework.data.repository.CrudRepository;

import com.blackduck.integration.scm.entity.CiBuild;

public interface ICiBuildRepository extends CrudRepository<CiBuild, Long> {
	
}
