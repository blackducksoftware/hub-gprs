package com.blackduck.integration.scm.dao;

import java.util.Collections;
import java.util.List;

import com.blackduck.integration.scm.entity.FileContent;
import com.google.common.collect.ImmutableList;

public class FileDao {
	private final IFileContentRepository fileContentRepository;

	public FileDao(IFileContentRepository fileContentRepository) {
		this.fileContentRepository = fileContentRepository;
	}

	/**
	 * Stores a new or existing fileContent and returns the updated/stored instance.
	 * 
	 * @param fileContent
	 * @return
	 */
	public FileContent create(FileContent fileContent) {
		return fileContentRepository.save(fileContent);
	}

	/**
	 * @return All uploaded files (FileContent), or, if none are found, an empty list.
	 */
	public List<FileContent> listFileContents() {
		Iterable<FileContent> fileContents = fileContentRepository.findAll();
		if (fileContents == null) {
			return Collections.emptyList();
		}
		if (fileContents instanceof List)
			return Collections.unmodifiableList((List<FileContent>) fileContents);
		else
			return ImmutableList.copyOf(fileContents);
	}

	public void deleteFileContent(long id) {
		fileContentRepository.delete(id);
	}

	public FileContent findById(long id) {
		return fileContentRepository.findOne(id);
	}

}
