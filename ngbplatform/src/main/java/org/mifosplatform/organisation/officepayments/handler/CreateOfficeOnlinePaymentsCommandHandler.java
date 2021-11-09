package org.mifosplatform.organisation.officepayments.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.organisation.officepayments.service.OfficePaymentsWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateOfficeOnlinePaymentsCommandHandler implements NewCommandSourceHandler {

	private final OfficePaymentsWritePlatformService writePlatformService;

	@Autowired
	public CreateOfficeOnlinePaymentsCommandHandler(OfficePaymentsWritePlatformService writePlatformService) {
		this.writePlatformService = writePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {

		return this.writePlatformService.createOfficeOnlinePayment(command);
	}

}
