package org.mifosplatform.Revpay.order.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class OrderNotCreatedException extends AbstractPlatformDomainRuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OrderNotCreatedException() {
		super("error.msg.order.not.created ", "order not created ");
	}

}
