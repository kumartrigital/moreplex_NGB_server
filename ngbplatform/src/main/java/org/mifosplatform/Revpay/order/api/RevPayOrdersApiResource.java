package org.mifosplatform.Revpay.order.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.mifosplatform.Revpay.order.domain.RevpayOrder;
import org.mifosplatform.Revpay.order.service.RevPayOrderWritePlatformService;
import org.mifosplatform.billing.planprice.domain.PriceRepository;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.finance.officebalance.domain.OfficeBalanceRepository;
import org.mifosplatform.finance.payments.api.PaymentsApiResource;
import org.mifosplatform.finance.paymentsgateway.data.PaymentGatewayData;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGateway;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGatewayRepository;
import org.mifosplatform.finance.paymentsgateway.service.PaymentGatewayReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.agent.api.ItemSaleAPiResource;
import org.mifosplatform.logistics.agent.domain.ItemSale;
import org.mifosplatform.logistics.agent.domain.ItemSaleRepository;
import org.mifosplatform.logistics.itemdetails.domain.ItemDetailsRepository;
import org.mifosplatform.logistics.mrn.api.MRNDetailsApiResource;
import org.mifosplatform.organisation.officepayments.api.OfficePaymentsApiResource;
import org.mifosplatform.organisation.voucher.api.VoucherPinApiResource;
import org.mifosplatform.portfolio.activationprocess.api.ActivationProcessApiResource;
import org.mifosplatform.portfolio.activationprocess.domain.LeaseDetails;
import org.mifosplatform.portfolio.activationprocess.domain.LeaseDetailsRepository;
import org.mifosplatform.portfolio.contract.domain.ContractRepository;
import org.mifosplatform.portfolio.order.api.MultipleOrdersApiResource;
import org.mifosplatform.portfolio.order.api.OrdersApiResource;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderPriceRepository;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.order.service.OrderWritePlatformService;
import org.mifosplatform.portfolio.plan.domain.Plan;
import org.mifosplatform.portfolio.plan.domain.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Component
@Path("/revpay")
public class RevPayOrdersApiResource {

	private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(
			Arrays.asList("id", "provisionType", "action", "provisionigSystem", "isEnable"));
	private final DefaultToApiJsonSerializer<RevpayOrder> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final RevPayOrderWritePlatformService revPayOrderWritePlatformService;
	private final PaymentGatewayRepository paymentGatewayRepository;
	private final OfficeBalanceRepository officeBalanceRepository;
	private final PaymentsApiResource paymentsApiResource;
	private final OrdersApiResource ordersApiResource;
	private final MultipleOrdersApiResource multipleOrdersApiResource;
	private final ToApiJsonSerializer<PaymentGateway> apiJsonSerializerPaymentGateway;
	private final ToApiJsonSerializer<PaymentGatewayData> apiJsonSerializerPaymentGatewaydata;
	private final ItemDetailsRepository itemDetailsRepository;
	private final ItemSaleRepository itemSaleRepository;
	private final MRNDetailsApiResource mRNDetailsApiResource;
	private final OfficePaymentsApiResource officePaymentsApiResource;
	private final ActivationProcessApiResource activationProcessApiResource;
	private final LeaseDetailsRepository leaseDetailsRepository;
	private final OrderRepository orderRepository;
	private final OrderPriceRepository orderPriceRepository;
	private final PlanRepository planRepository;
	private final PriceRepository priceRepository;
	private final ContractRepository contractRepository;
	private final FromJsonHelper fromJsonHelper;
	private final PaymentGatewayReadPlatformService paymentGatewayReadPlatformService;
	final private PlatformSecurityContext context;
	private final VoucherPinApiResource voucherPinApiResource;

	private final static int RECONNECT_ORDER_STATUS = 3;
	private final static int RENEWAL_ORDER_STATUS = 1;
	private final static String RESOURCE_TYPE = "PAYMENTGATEWAY";

	private final OrderWritePlatformService orderWritePlatformService;

	@Autowired
	public RevPayOrdersApiResource(final DefaultToApiJsonSerializer<RevpayOrder> apiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper, final PlatformSecurityContext context,
			final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final RevPayOrderWritePlatformService revPayOrderWritePlatformService,
			final PaymentGatewayReadPlatformService paymentGatewayReadPlatformService,
			final FromJsonHelper fromApiJsonHelper, final PaymentGatewayRepository paymentGatewayRepository,
			final OfficeBalanceRepository officeBalanceRepository, final PaymentsApiResource paymentsApiResource,
			final OrdersApiResource ordersApiResource, final MultipleOrdersApiResource multipleOrdersApiResource,
			final ToApiJsonSerializer<PaymentGateway> apiJsonSerializerPaymentGateway,
			final ToApiJsonSerializer<PaymentGatewayData> apiJsonSerializerPaymentGatewaydata,
			final ItemSaleAPiResource itemSaleAPiResource, final ItemDetailsRepository itemDetailsRepository,
			final ItemSaleRepository itemSaleRepository, final MRNDetailsApiResource mRNDetailsApiResource,
			final OfficePaymentsApiResource officePaymentsApiResource,
			final ActivationProcessApiResource activationProcessApiResource,
			final LeaseDetailsRepository leaseDetailsRepository, final OrderRepository orderRepository,
			final OrderWritePlatformService orderWritePlatformService, final OrderPriceRepository orderPriceRepository,
			final PlanRepository planRepository, final PriceRepository priceRepository,
			final ContractRepository contractRepository, final FromJsonHelper fromJsonHelper,
			final VoucherPinApiResource voucherPinApiResource) {

		this.toApiJsonSerializer = apiJsonSerializer;
		this.context = context;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.apiJsonSerializerPaymentGatewaydata = apiJsonSerializerPaymentGatewaydata;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.revPayOrderWritePlatformService = revPayOrderWritePlatformService;
		this.paymentGatewayReadPlatformService = paymentGatewayReadPlatformService;
		this.paymentGatewayRepository = paymentGatewayRepository;
		this.officeBalanceRepository = officeBalanceRepository;
		this.paymentsApiResource = paymentsApiResource;
		this.ordersApiResource = ordersApiResource;
		this.multipleOrdersApiResource = multipleOrdersApiResource;
		this.apiJsonSerializerPaymentGateway = apiJsonSerializerPaymentGateway;
		this.itemDetailsRepository = itemDetailsRepository;
		this.itemSaleRepository = itemSaleRepository;
		this.mRNDetailsApiResource = mRNDetailsApiResource;
		this.officePaymentsApiResource = officePaymentsApiResource;
		this.activationProcessApiResource = activationProcessApiResource;
		this.leaseDetailsRepository = leaseDetailsRepository;
		this.orderRepository = orderRepository;
		this.orderWritePlatformService = orderWritePlatformService;
		this.orderPriceRepository = orderPriceRepository;
		this.planRepository = planRepository;
		this.priceRepository = priceRepository;
		this.contractRepository = contractRepository;
		this.fromJsonHelper = fromApiJsonHelper;
		this.voucherPinApiResource = voucherPinApiResource;

	}

	@POST
	@Path("/createorder")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createOrder(String apiRequestBodyAsJson) {

		final CommandWrapper commandWrapper = new CommandWrapperBuilder().createRevOrder()
				.withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandWrapper);
		return this.toApiJsonSerializer.serialize(result);

	}

	@GET
	@Path("/status/{txref}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getRavePayStatusnew(@PathParam("txref") Long txref, @Context final UriInfo uriInfo) {

		this.context.authenticatedUser().validateHasReadPermission(this.RESOURCE_TYPE);
		// PaymentGateway orderDetails =
		// paymentGatewayRepository.findPaymentDetailsByPaymentId(txref);
		PaymentGatewayData paymentGateway = this.paymentGatewayReadPlatformService.retrievePaymentgatewaydatanew(txref);

		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.apiJsonSerializerPaymentGatewaydata.serialize(settings, paymentGateway, RESPONSE_DATA_PARAMETERS);

	}

	@GET
	@Path("/orderlock/{txref}/{flwref}")
	@SuppressWarnings("unchecked")
	public Response GetcllBackRavePayOrder(@PathParam("txref") Long txref, @PathParam("flwref") String flwref,
			@QueryParam("resp") String resp) throws JSONException {

		//String callBackUrl = "https://billing.moreplextv.com";
		String callBackUrl = "https://52.22.65.59:8877";
		//String callBackUrl = "https://localhost:8877";

		URI indexPath = null;
		String flwrefKey = null;
		String status = "successful";
		//String result = null;

		//String revpayStatus = revPayOrderWritePlatformService.revTransactionStatus(txref);
		PaymentGateway revpayOrder = paymentGatewayRepository.findPaymentDetailsByPaymentId(txref.toString());

		flwrefKey=flwref;
		revpayOrder.setPartyId(flwrefKey); 
		String locale = "en";
		String dateFormat = "dd MMMM yyyy";

		if (status.equalsIgnoreCase("successful")) {

			revpayOrder.setStatus("Success");
			revpayOrder.setPartyId(flwrefKey);
			paymentGatewayRepository.save(revpayOrder);
			JSONObject paymentJson = new JSONObject();
			SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

			if (revpayOrder.getType().equalsIgnoreCase("LEASEVERIFICATION_Payment")) {
				LeaseDetails leaseDetails = leaseDetailsRepository
						.findLeaseDetailsByMobileNo(revpayOrder.getDeviceId());
				if (leaseDetails.getStatus().equalsIgnoreCase("Payment_pending")) {
					leaseDetails.setStatus("Registration_Pending");
					leaseDetailsRepository.save(leaseDetails);
				} else {
					leaseDetails.setStatus("something went wrong");
				}
				try {
					indexPath = new URI(
							callBackUrl + "/#/Registration/LeaseVerification/" + leaseDetails.getMobileNumber());
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}

			}

			else if (revpayOrder.getType().equalsIgnoreCase("SETUPBOX_Payment")) {
				paymentJson.put("paymentCode", 23);
				paymentJson.put("amountPaid", revpayOrder.getAmountPaid());
				paymentJson.put("receiptNo", revpayOrder.getReceiptNo());
				paymentJson.put("remarks", "STB PAYMENT");
				paymentJson.put("locale", "en");
				paymentJson.put("dateFormat", "dd MMMM yyyy");
				paymentJson.put("paymentDate", formatter.format(revpayOrder.getPaymentDate()));
				paymentJson.put("collectionBy", 2);
				paymentJson.put("collectorName", "MOREPLEX");
				officePaymentsApiResource.createOfficePayment(revpayOrder.getObsId(), paymentJson.toString());
				ItemSale itemSale = itemSaleRepository.findOne(Long.parseLong(revpayOrder.getReffernceId()));
				itemSale.setStatus("PENDING");
				itemSaleRepository.save(itemSale);
				try {
					indexPath = new URI(callBackUrl + "/#/inventory/" + txref);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}

			} else if (revpayOrder.getType().equalsIgnoreCase("Voucher_Payment")) {
				paymentJson.put("paymentCode", 23);
				paymentJson.put("amountPaid", revpayOrder.getAmountPaid());
				paymentJson.put("receiptNo", revpayOrder.getReceiptNo());
				paymentJson.put("remarks", "STB PAYMENT");
				paymentJson.put("locale", "en");
				paymentJson.put("dateFormat", "dd MMMM yyyy");
				paymentJson.put("paymentDate", formatter.format(revpayOrder.getPaymentDate()));
				paymentJson.put("collectionBy", 2);
				paymentJson.put("collectorName", "MOREPLEX");
				officePaymentsApiResource.createOfficePayment(revpayOrder.getObsId(), paymentJson.toString());
				
				ItemSale itemSale = itemSaleRepository.findOne(Long.parseLong(revpayOrder.getReffernceId()));
				

				itemSale.setStatus("PENDING");
				itemSaleRepository.save(itemSale);
				
				JSONObject moveVouchers = new JSONObject();
				moveVouchers.put("type", "sale");
				moveVouchers.put("saleRefNo", itemSale.getItemId());
				moveVouchers.put("quantity", itemSale.getOrderQuantity());
				moveVouchers.put("toOffice", itemSale.getPurchaseFrom());
				moveVouchers.put("fromOffice", itemSale.getPurchaseBy());
				moveVouchers.put("dateFormat", "dd MMMM yyyy");
				moveVouchers.put("paymentDate", formatter.format(revpayOrder.getPaymentDate()));
				voucherPinApiResource.moveVoucher(itemSale.getPurchaseBy(), moveVouchers.toString());
	
				try {
					indexPath = new URI(callBackUrl + "/#/inventory/" + txref);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}

			}

			else if (revpayOrder.getType().equalsIgnoreCase("Activation_Payment")) {
				String activationResponse = activationProcessApiResource
						.createSimpleActivation(revpayOrder.getReProcessDetail());
				org.json.JSONObject jsonResult = new org.json.JSONObject(activationResponse.toString());

				revpayOrder.setReffernceId(jsonResult.getString("clientId"));

				paymentJson.put("clientId", jsonResult.getLong("clientId"));
				paymentJson.put("isSubscriptionPayment", "false");
				paymentJson.put("isChequeSelected", "No");
				paymentJson.put("paymentCode", 27);
				paymentJson.put("receiptNo", revpayOrder.getReceiptNo());
				paymentJson.put("remarks", "nothing");
				paymentJson.put("amountPaid", revpayOrder.getAmountPaid());
				paymentJson.put("paymentType", "Online Payment");
				paymentJson.put("locale", locale);
				paymentJson.put("dateFormat", dateFormat);
				paymentJson.put("paymentSource", null);
				paymentJson.put("paymentDate", formatter.format(revpayOrder.getPaymentDate()));

				paymentsApiResource.createPayment(jsonResult.getLong("clientId"), paymentJson.toString());

				try {
					indexPath = new URI(callBackUrl + "/#/DTH-OnlinePayment/" + txref);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}

			else if (revpayOrder.getType().equalsIgnoreCase("account_topup")) {

				paymentJson.put("paymentCode", 23);
				paymentJson.put("amountPaid", revpayOrder.getAmountPaid());
				paymentJson.put("receiptNo", revpayOrder.getReceiptNo());
				paymentJson.put("remarks", "Toup_PAYMENT");
				paymentJson.put("locale", "en");
				paymentJson.put("dateFormat", "dd MMMM yyyy");
				paymentJson.put("paymentDate", formatter.format(revpayOrder.getPaymentDate()));
				paymentJson.put("collectionBy", revpayOrder.getObsId());
				paymentJson.put("collectorName", revpayOrder.getObsId());
				officePaymentsApiResource.createOfficePayment(9l, paymentJson.toString());

				try {
					indexPath = new URI(callBackUrl + "/#/DTH-OnlinePayment/" + txref);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			} else if (revpayOrder.getType().equalsIgnoreCase("subscription_renewal")) {

				Order order = this.orderRepository.findOne(Long.parseLong(revpayOrder.getReffernceId()));

				if (order.getStatus() == RECONNECT_ORDER_STATUS) {
					this.orderWritePlatformService.reconnectOrder(Long.parseLong(revpayOrder.getReffernceId()),
							"Revpay");
				} else if (order.getStatus() == RENEWAL_ORDER_STATUS) {
					this.ordersApiResource.renewalOrder(Long.parseLong(revpayOrder.getReffernceId()),
							revpayOrder.getReProcessDetail());
				}

			} else if (revpayOrder.getType().equalsIgnoreCase("subscription_add")) {

				final Order order = this.orderRepository.findOne(Long.parseLong(revpayOrder.getReffernceId()));
				final Plan plan = this.planRepository.findOne(order.getPlanId());

				final JsonObject orderJson = new JsonObject();

				orderJson.addProperty("planid", order.getPlanId());
				orderJson.addProperty("planCode", plan.getPlanCode());
				orderJson.addProperty("plandescription", plan.getDescription());
				orderJson.addProperty("planpoid", plan.getPlanPoid());
				orderJson.addProperty("dealpoid", plan.getPlanPoid());
				orderJson.addProperty("paytermCode", plan.getPlanPoid());
				orderJson.addProperty("contractperiod", order.getContarctPeriod());
				orderJson.addProperty("clientserviceid", order.getClientServiceId());
				orderJson.addProperty("billAlign", order.getbillAlign());
				orderJson.addProperty("clientId", order.getClientId());
				orderJson.addProperty("locale", "en");
				orderJson.addProperty("dateFormat", "dd MMMM yyyy");
				orderJson.addProperty("startdate", formatter.format(order.getStartDate()));

				multipleOrdersApiResource.createMultipleOrder(revpayOrder.getObsId(), orderJson.toString());

				try {
					indexPath = new URI(callBackUrl + "/#/DTH-OnlinePayment/" + txref);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}

			else if (revpayOrder.getType().equalsIgnoreCase("selfcare_registration")) {

				String activationResponse = activationProcessApiResource
						.createSelfRegistration(revpayOrder.getReProcessDetail());
				org.json.JSONObject jsonResult = new org.json.JSONObject(activationResponse.toString());

				revpayOrder.setReffernceId(jsonResult.getString("clientId"));

				paymentJson.put("clientId", jsonResult.getLong("clientId"));
				paymentJson.put("isSubscriptionPayment", "false");
				paymentJson.put("isChequeSelected", "No");
				paymentJson.put("paymentCode", 27);
				paymentJson.put("receiptNo", revpayOrder.getReceiptNo());
				paymentJson.put("remarks", "nothing");
				paymentJson.put("amountPaid", revpayOrder.getAmountPaid());
				paymentJson.put("paymentType", "Online Payment");
				paymentJson.put("locale", locale);
				paymentJson.put("dateFormat", dateFormat);
				paymentJson.put("paymentSource", null);
				paymentJson.put("paymentDate", formatter.format(revpayOrder.getPaymentDate()));

				paymentsApiResource.createPayment(jsonResult.getLong("clientId"), paymentJson.toString());

				try {
					indexPath = new URI(callBackUrl + "/#/DTH-OnlinePayment/" + txref);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}

			else {
				paymentJson.put("clientId", revpayOrder.getReffernceId());
				paymentJson.put("isSubscriptionPayment", "false");
				paymentJson.put("isChequeSelected", "No");
				paymentJson.put("paymentCode", 27);
				paymentJson.put("receiptNo", revpayOrder.getReceiptNo());// need to
				paymentJson.put("remarks", "nothing");
				paymentJson.put("amountPaid", revpayOrder.getAmountPaid());// need to
				paymentJson.put("paymentType", "Online Payment");
				paymentJson.put("locale", locale);
				paymentJson.put("dateFormat", dateFormat);
				paymentJson.put("paymentSource", null);

				paymentJson.put("paymentDate", formatter.format(revpayOrder.getPaymentDate()));
				paymentsApiResource.createPayment(Long.parseLong(revpayOrder.getReffernceId()), paymentJson.toString());

				try {
					indexPath = new URI(callBackUrl + "/#/renewal-customer/" + txref);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}

		} else {

			revpayOrder.setStatus("Failed");
			paymentGatewayRepository.save(revpayOrder);

		}

		return Response.temporaryRedirect(indexPath).build();
	}

}
