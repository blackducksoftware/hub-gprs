package com.blackduck.integration.scm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.blackduck.integration.scm.BuildMonitor;

/**
 * Controller to display debugging/troubleshooting information
 * 
 * @author ybronshteyn
 *
 */
@Controller
public class DebugController {
	private final BuildMonitor buildMonitor;

	public DebugController(BuildMonitor buildMonitor) {
		this.buildMonitor = buildMonitor;
	}

	@GetMapping(path = "/debug")
	public String getDebugInfo(Model model) {
		model.addAttribute("debugInfo",
				buildMonitor.getDebugInfo());
		return "debug";
	}

}
