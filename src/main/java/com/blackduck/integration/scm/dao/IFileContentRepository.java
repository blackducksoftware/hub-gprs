package com.blackduck.integration.scm.dao;

import org.springframework.data.repository.CrudRepository;

import com.blackduck.integration.scm.entity.FileContent;


public interface IFileContentRepository extends CrudRepository<FileContent, Long> {

}
