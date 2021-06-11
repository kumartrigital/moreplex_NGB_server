package org.mifosplatform.logistics.agent.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemSaleRepository extends JpaRepository<ItemSale, Long>,
JpaSpecificationExecutor<ItemSale>{
	
	@Query(value ="select office_id from m_appuser where username=:username and is_deleted!=1", nativeQuery = true)
	Long getOffice(@Param("username") String username);

}
