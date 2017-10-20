package com.blackduck.integration.scm.dao;

import java.util.Collections;
import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.stereotype.Component;

import com.blackduck.integration.scm.entity.CiBuild;

@Component
public class CiBuildDao {

	@Inject
	private ICiBuildRepository ciBuildRepository;

	public Optional<CiBuild> findById(long id) {
		return Optional.ofNullable(ciBuildRepository.findOne(id));
	}

	
	public Iterable<CiBuild> findByIds(Iterable<Long> ids) {
		Iterable<CiBuild> found = ciBuildRepository.findAll(ids);
		if (found ==null) found = Collections.emptyList();
		return found;
		
	}
	
	public void markViolation(long ciBuildId) {
		CiBuild ciBuild = findOrScaffold(ciBuildId);
		ciBuild.setViolation(true);
		ciBuildRepository.save(ciBuild);
	}

	public void markFailure(long ciBuildId) {
		CiBuild ciBuild = findOrScaffold(ciBuildId);
		ciBuild.setFailure(true);
		ciBuildRepository.save(ciBuild);
	}

	public void markSuccess(long ciBuildId) {
		CiBuild ciBuild = findOrScaffold(ciBuildId);
		ciBuild.setSuccess(true);
		ciBuildRepository.save(ciBuild);
	}

	/**
	 * Records that the build has completed without recording any status
	 * information. Does nothing if markViolation, markFailure, or markSuccess has
	 * already been called.
	 * 
	 * @param ciBuildId
	 */
	@Transactional
	public void recordCompletion(long ciBuildId) {
		CiBuild ciBuild = ciBuildRepository.findOne(ciBuildId);
		if (ciBuild == null) {
			ciBuild = new CiBuild();
			ciBuild.setId(ciBuildId);
			ciBuildRepository.save(ciBuild);
		}
	}

	/**
	 * Returns an existing ciBuild by ID or creates a new one but does not persist
	 * it
	 * 
	 * @param ciBuildId
	 * @return
	 */
	@Transactional
	private CiBuild findOrScaffold(long ciBuildId) {
		CiBuild ciBuild = ciBuildRepository.findOne(ciBuildId);
		if (ciBuild == null) {
			ciBuild = new CiBuild();
			ciBuild.setId(ciBuildId);
		}
		return ciBuild;
	}
}
