package org.mifosplatform.Revpay.order.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.mifosplatform.Revpay.order.domain.RevPayOrderRepository;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGateway;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGatewayRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.logistics.agent.api.ItemSaleAPiResource;
import org.mifosplatform.logistics.item.exception.ItemNotFoundException;
import org.mifosplatform.logistics.itemdetails.domain.ItemDetails;
import org.mifosplatform.logistics.itemdetails.domain.ItemDetailsRepository;
import org.mifosplatform.portfolio.order.service.OrderWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RevPayOrderWritePlatformServiceImpl implements RevPayOrderWritePlatformService {

	private final RevPayOrderRepository revPayOrderRepo;

	private final PaymentGatewayRepository paymentGatewayRepository;

	private final OrderWritePlatformService orderWritePlatformService;

	private final FromJsonHelper fromApiJsonHelper;

	private HttpServletResponse httpServletResponse;

	private final ItemSaleAPiResource itemSaleAPiResource;

	private final ItemDetailsRepository itemDetailsRepository;

	@Autowired
	public RevPayOrderWritePlatformServiceImpl(final RevPayOrderRepository revPayOrderRepo,
			OrderWritePlatformService orderWritePlatformService, FromJsonHelper fromApiJsonHelper,
			PaymentGatewayRepository paymentGatewayRepository, final ItemSaleAPiResource itemSaleAPiResource,
			final ItemDetailsRepository itemDetailsRepository) {
		this.revPayOrderRepo = revPayOrderRepo;
		this.orderWritePlatformService = orderWritePlatformService;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.paymentGatewayRepository = paymentGatewayRepository;
		this.itemSaleAPiResource = itemSaleAPiResource;
		this.itemDetailsRepository = itemDetailsRepository;
	}

	@Override
	public CommandProcessingResult createOrder(JsonCommand command) {
		JSONObject revorder = null;
		try {

			String type = command.stringValueOfParameterName("type");
			PaymentGateway PaymentGateway = new PaymentGateway();

			if (type.equalsIgnoreCase("SETUPBOX_Payment")) {
				PaymentGateway.setObsId(command.longValueOfParameterNamed("toOffice"));
				PaymentGateway.setAmountPaid(new BigDecimal(command.stringValueOfParameterName("amount")));
				PaymentGateway.setPaymentId(getTxid());
				PaymentGateway.setPartyId(PaymentGateway.getPaymentId());
				PaymentGateway.setReceiptNo("RAVE_STB_" + PaymentGateway.getPaymentId());
				PaymentGateway.setStatus("intiated");
				PaymentGateway.setPaymentDate(new Date());
				PaymentGateway.setSource("REVPAY");
				PaymentGateway.setRemarks("NOTHING");
				PaymentGateway.setType(type);
				PaymentGateway.setDeviceId(command.stringValueOfParameterName("itemsaleId"));
				paymentGatewayRepository.save(PaymentGateway);
				revorder = new JSONObject();
				revorder.put("txid", PaymentGateway.getPaymentId());
				revorder.put("revorder", "order created sucussfully");
				revorder.put("callbackUrl", "https://52.22.65.59:8877/ngbplatform/api/v1/revpay/orderlock/"
						+ PaymentGateway.getPaymentId() + "/");
			}

			else if (type.equalsIgnoreCase("Activation_Payment")) {
				revorder = new JSONObject();

				PaymentGateway.setObsId(command.longValueOfParameterNamed("OfficeId"));
				PaymentGateway.setAmountPaid(new BigDecimal(command.stringValueOfParameterName("amount")));
				PaymentGateway.setPaymentId(getTxid());
				PaymentGateway.setPartyId(PaymentGateway.getPaymentId());
				PaymentGateway.setReceiptNo("RAVE_STB_Activation" + PaymentGateway.getPaymentId());
				PaymentGateway.setStatus("intiated");
				PaymentGateway.setPaymentDate(new Date());
				PaymentGateway.setSource("REVPAY");
				PaymentGateway.setRemarks("NOTHING");
				PaymentGateway.setType(type);
				org.json.simple.JSONArray address = new org.json.simple.JSONArray();
				org.json.simple.JSONArray devices = new org.json.simple.JSONArray();
				org.json.simple.JSONArray plans = new org.json.simple.JSONArray();

				JSONObject activation = new JSONObject();
				JSONObject addressjsonBilling = new JSONObject();
				JSONObject addressjson = new JSONObject();
				JSONObject devicejson = new JSONObject();
				JSONObject purchasePlanjson = new JSONObject();
				JSONObject basePlanjson = new JSONObject();


				activation.put("forename", command.stringValueOfParameterNamed("forename"));
				activation.put("surname", command.stringValueOfParameterNamed("surname"));
				activation.put("gender", "male");
				activation.put("email", command.stringValueOfParameterNamed("email"));
				activation.put("mobile", command.stringValueOfParameterNamed("mobile"));
				activation.put("dob", 12101990);
				activation.put("officeId", 2);

				addressjson.put("addressNo", command.stringValueOfParameterNamed("addressNo"));
				addressjson.put("street", "");
				addressjson.put("city", command.stringValueOfParameterName("city"));
				addressjson.put("state", command.stringValueOfParameterName("state"));
				addressjson.put("country", command.stringValueOfParameterName("country"));
				addressjson.put("district", command.stringValueOfParameterName("district"));
				addressjson.put("zipCode", "900013");
				addressjson.put("addressType", "PRIMARY");
				address.add(addressjson);

				addressjsonBilling.put("addressNo", command.stringValueOfParameterName("addressNo"));
				addressjsonBilling.put("street", "");
				addressjsonBilling.put("city", command.stringValueOfParameterName("city"));
				addressjsonBilling.put("state", command.stringValueOfParameterName("state"));
				addressjsonBilling.put("country", command.stringValueOfParameterName("country"));
				addressjsonBilling.put("district", command.stringValueOfParameterName("district"));
				addressjsonBilling.put("zipCode", "900013");
				addressjsonBilling.put("addressType", "BILLING");
				address.add(addressjsonBilling);

				ItemDetails itemDetails = itemDetailsRepository.getAvilableBox(2l);

				if (itemDetails == null) {
					throw new ItemNotFoundException("stock not found");
				}

				devicejson.put("deviceId", itemDetails.getSerialNumber());
				devices.add(devicejson);

				basePlanjson.put("planCode", "BASEPACK");
				purchasePlanjson.put("planCode", "PURCHASE");
				plans.add(basePlanjson);
				plans.add(purchasePlanjson);
			

				activation.put("salutation", "Mr.");
				activation.put("address", address);
				activation.put("devices", devices);
				activation.put("plans", plans);

				PaymentGateway.setType(type);
				PaymentGateway.setReProcessDetail(activation.toString());
				PaymentGateway.setDeviceId(itemDetails.getSerialNumber());
				System.out.println("RevPayOrderWritePlatformServiceImpl.createOrder()"+activation.toString());
				paymentGatewayRepository.save(PaymentGateway);
				devices.clear();
				plans.clear();
				address.clear();
				revorder.put("txid", PaymentGateway.getPaymentId());
				revorder.put("revorder", "order created sucussfully");
				revorder.put("callbackUrl", "https://52.22.65.59:8877/ngbplatform/api/v1/revpay/orderlock/"
						+ PaymentGateway.getPaymentId() + "/");

			}

			else {
				PaymentGateway.setDeviceId(command.stringValueOfParameterName("stbNo"));
				PaymentGateway.setAmountPaid(new BigDecimal(command.stringValueOfParameterName("amount")));
				PaymentGateway.setPaymentId(getTxid());
				PaymentGateway.setPartyId(PaymentGateway.getPaymentId());
				PaymentGateway.setReceiptNo("RAVE_" + PaymentGateway.getPaymentId());
				PaymentGateway.setStatus("intiated");
				PaymentGateway.setPaymentDate(new Date());
				PaymentGateway.setSource("REVPAY");
				PaymentGateway.setReffernceId(command.stringValueOfParameterName("clientId"));
				PaymentGateway.setRemarks("NOTHING");
				PaymentGateway.setType(type);
				paymentGatewayRepository.save(PaymentGateway);
				revorder = new JSONObject();
				revorder.put("txid", PaymentGateway.getPaymentId());
				paymentGatewayRepository.save(PaymentGateway);
				revorder.put("revorder", "order created sucussfully");
				revorder.put("callbackUrl", "https://52.22.65.59:8877/ngbplatform/api/v1/revpay/orderlock/"
						+ PaymentGateway.getPaymentId() + "/");
			}
			return new CommandProcessingResult(revorder);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new CommandProcessingResult(revorder);

	}

	public String getTxid() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println(timestamp);
		return String.valueOf(timestamp.getTime());
	}

	public String revTransactionStatus(Long txid) {
		String status = null;
		String revResponse = null;
		try {
			RestTemplate rest = new RestTemplate();

			String VERIFY_ENDPOINT = "https://api.ravepay.co/flwv3-pug/getpaidx/api/v2/verify";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			JSONObject revRequest = new JSONObject();
			revRequest.put("txref", txid);
			// FLWPUBK-acb0630ea1c150dabf363efa007d3a0b-X
			 revRequest.put("SECKEY", "FLWSECK_TEST-09b25bed4e4027011c8d5613fc73945a-X");
			//revRequest.put("SECKEY", "FLWSECK-05da041a69e8b6ac206ae74f8f7c4bd8-X");

			HttpEntity<String> request = new HttpEntity<>(revRequest.toString(), headers);
			revResponse = rest.postForObject(VERIFY_ENDPOINT, request, String.class);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return revResponse.toString();

	}

	private void handleCodeDataIntegrityIssues(JsonCommand command, Exception dve) {
		throw new PlatformDataIntegrityException("error.msg.office.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource.");
	}

}
