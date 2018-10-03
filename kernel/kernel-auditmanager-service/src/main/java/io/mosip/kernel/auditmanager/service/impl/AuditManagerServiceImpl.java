package io.mosip.kernel.auditmanager.service.impl;

import io.mosip.kernel.core.spi.auditmanager.AuditHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.auditmanager.dto.AuditResponseDto;
import io.mosip.kernel.auditmanager.entity.Audit;
import io.mosip.kernel.auditmanager.request.AuditRequestDto;
import io.mosip.kernel.auditmanager.service.AuditManagerService;

/**
 * AuditManager service implementation with function to add new {@link Audit}
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Service
@Transactional
public class AuditManagerServiceImpl implements AuditManagerService {

	/**
	 * Field for audit handler
	 */
	@Autowired
	private AuditHandler<AuditRequestDto> auditHandler;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.audit.service.impl.AuditManagerService#addAudit(org.
	 * mosip.kernel.core.audit.dto.AuditRequestDto)
	 */
	@Override
	public AuditResponseDto addAudit(AuditRequestDto auditRequestDto) {
		AuditResponseDto auditResponseDto = new AuditResponseDto();
		auditResponseDto.setStatus(auditHandler.writeAudit(auditRequestDto));
		return auditResponseDto;
	}
}
