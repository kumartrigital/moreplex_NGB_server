package org.mifosplatform.portfolio.activationprocess.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LeaseDetailsRepository extends JpaRepository<LeaseDetails, Long>, JpaSpecificationExecutor<LeaseDetails> {

	
	
	
}
