package com.blackduck.integration.scm.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.blackduck.integration.scm.dao.FileDao;
import com.blackduck.integration.scm.entity.Build;
import com.blackduck.integration.scm.entity.FileContent;
import com.blackduck.integration.scm.entity.FileInjection;

@Controller
@RequestMapping("/files")
public class FileController {
	private final FileDao fileDao;

	public FileController(FileDao fileDao) {
		this.fileDao = fileDao;
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ModelAndView handleConstraintViolationException(HttpServletRequest req, DataIntegrityViolationException ex) {
		return errorDisplay(ex.getMostSpecificCause().getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ModelAndView handleFileError(HttpServletRequest req, Exception e) {
		return errorDisplay(e.getMessage());
	}

	private ModelAndView errorDisplay(String error) {
		ModelAndView errorDisplay = new ModelAndView("/files");
		errorDisplay.addObject("message", error);
		errorDisplay.addObject("files", fileDao.listFileContents());
		return errorDisplay;
	}

	@PostMapping
	public String handleFileUpload(@RequestParam MultipartFile file, @RequestParam String name, Model model)
			throws IOException {

		FileContent fileContent = new FileContent();
		fileContent.setContent(file.getBytes());
		fileContent.setName(name);
		Date date = new Date();
		fileContent.setDateCreated(date);
		fileContent.setDateUpdated(date);
		try {
			fileDao.create(fileContent);
			model.addAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");
		} catch (DataIntegrityViolationException e) {
			model.addAttribute("message", "Error: file " + name + " already exists.");
		}

		return listFiles(model);
	}

	@GetMapping
	public String listFiles(Model model) {
		List<FileContent> files = fileDao.listFileContents();
		model.addAttribute("files", files);
		return "files";
	}

	@DeleteMapping("/{id}")
	@Transactional
	public String deleteById(@PathVariable long id, Model model) {
		FileContent fileContent = fileDao.findById(id);
		
		// Ensure content is not used
		if (!fileContent.getInjections().isEmpty()) {
			String usedBy = fileContent.getInjections().stream().map(FileInjection::getBuild).map(Build::getName)
					.collect(Collectors.joining(", "));
			model.addAttribute("message",
					"Unable to delete file " + fileContent.getName() + " as it is in use by: " + usedBy);
			return listFiles(model);
		}

		fileDao.deleteFileContent(id);
		model.addAttribute("message", "File deleted successfully.");
		return listFiles(model);
	}

}
