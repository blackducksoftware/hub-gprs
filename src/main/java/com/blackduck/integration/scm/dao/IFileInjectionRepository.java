package com.blackduck.integration.scm.dao;

import org.springframework.data.repository.CrudRepository;

import com.blackduck.integration.scm.entity.FileInjection;

public interface IFileInjectionRepository extends CrudRepository<FileInjection, Long>{

}
