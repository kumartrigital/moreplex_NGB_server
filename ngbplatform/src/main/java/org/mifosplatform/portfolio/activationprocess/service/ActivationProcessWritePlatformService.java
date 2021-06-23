/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.activationprocess.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.http.ResponseEntity;

public interface ActivationProcessWritePlatformService {

	CommandProcessingResult activationProcess(JsonCommand command);

	CommandProcessingResult selfRegistrationProcess(JsonCommand command);

	CommandProcessingResult createSimpleActivation(JsonCommand command);

	CommandProcessingResult createLeaseDetails(JsonCommand command);

	CommandProcessingResult createClientSimpleActivation(JsonCommand command, Long clientId);

	CommandProcessingResult createCustomerActivation(JsonCommand command);

	CommandProcessingResult createServiceActivationWithoutDevice(JsonCommand command, Long clientId);

	CommandProcessingResult validateKey_NIN(JsonCommand command, Long NINId);

	CommandProcessingResult validateMobileAndNIN(JsonCommand command);

	CommandProcessingResult ResendOtpMessage(JsonCommand command);

	String saveImage(String imageBase64Encoder);

	Boolean photoVerification(String nin, String path);

	

}
