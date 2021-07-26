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
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.finance.payments.api.PaymentsApiResource;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGateway;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGatewayRepository;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.logistics.agent.api.ItemSaleAPiResource;
import org.mifosplatform.logistics.agent.domain.ItemSale;
import org.mifosplatform.logistics.agent.domain.ItemSaleRepository;
import org.mifosplatform.logistics.itemdetails.domain.ItemDetailsRepository;
import org.mifosplatform.logistics.mrn.api.MRNDetailsApiResource;
import org.mifosplatform.organisation.officepayments.api.OfficePaymentsApiResource;
import org.mifosplatform.portfolio.activationprocess.api.ActivationProcessApiResource;
import org.mifosplatform.portfolio.activationprocess.domain.LeaseDetails;
import org.mifosplatform.portfolio.activationprocess.domain.LeaseDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
	private final PaymentsApiResource paymentsApiResource;
	private final ToApiJsonSerializer<PaymentGateway> apiJsonSerializerPaymentGateway;
	private final ItemDetailsRepository itemDetailsRepository;
	private final ItemSaleRepository itemSaleRepository;
	private final MRNDetailsApiResource mRNDetailsApiResource;
	private final OfficePaymentsApiResource officePaymentsApiResource;
	private final ActivationProcessApiResource activationProcessApiResource;
	private final LeaseDetailsRepository leaseDetailsRepository;

	@Autowired
	public RevPayOrdersApiResource(final DefaultToApiJsonSerializer<RevpayOrder> apiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final RevPayOrderWritePlatformService revPayOrderWritePlatformService,
			final FromJsonHelper fromApiJsonHelper, final PaymentGatewayRepository paymentGatewayRepository,
			final PaymentsApiResource paymentsApiResource,
			final ToApiJsonSerializer<PaymentGateway> apiJsonSerializerPaymentGateway,
			final ItemSaleAPiResource itemSaleAPiResource, final ItemDetailsRepository itemDetailsRepository,
			final ItemSaleRepository itemSaleRepository, final MRNDetailsApiResource mRNDetailsApiResource,
			final OfficePaymentsApiResource officePaymentsApiResource,
			final ActivationProcessApiResource activationProcessApiResource,
			final LeaseDetailsRepository leaseDetailsRepository) {

		this.toApiJsonSerializer = apiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.revPayOrderWritePlatformService = revPayOrderWritePlatformService;
		this.paymentGatewayRepository = paymentGatewayRepository;
		this.paymentsApiResource = paymentsApiResource;
		this.apiJsonSerializerPaymentGateway = apiJsonSerializerPaymentGateway;
		this.itemDetailsRepository = itemDetailsRepository;
		this.itemSaleRepository = itemSaleRepository;
		this.mRNDetailsApiResource = mRNDetailsApiResource;
		this.officePaymentsApiResource = officePaymentsApiResource;
		this.activationProcessApiResource = activationProcessApiResource;
		this.leaseDetailsRepository = leaseDetailsRepository;

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
	@Path("/status")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getRavePayStatus(@QueryParam("txid") String txid, @Context final UriInfo uriInfo) {

		PaymentGateway orderDetails = paymentGatewayRepository.findPaymentDetailsByPaymentId(txid);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.apiJsonSerializerPaymentGateway.serialize(settings, orderDetails, RESPONSE_DATA_PARAMETERS);

	}

	@GET
	@Path("/orderlock/{txref}/{flwref}")
	@SuppressWarnings("unchecked")
	public Response GetcllBackRavePayOrder(@PathParam("txref") Long txref, @PathParam("flwref") String flwref,
			@QueryParam("resp") String resp) throws JSONException {

		URI indexPath = null;
		String flwrefKey = null;
		String status = "successful";
		String result = null;

		// String revpayStatus =
		// revPayOrderWritePlatformService.revTransactionStatus(txref);
		PaymentGateway revpayOrder = paymentGatewayRepository.findPaymentDetailsByPaymentId(txref.toString());
		// System.out.println("RevPayOrdersApiResource.GetcllBackRavePayOrder()");
		// org.json.JSONObject json;

		/*
		 * try { json = new org.json.JSONObject(revpayStatus.toString());
		 * org.json.JSONObject data = json.getJSONObject("data"); flwrefKey =
		 * data.getString("flwref"); status = data.getString("status"); } catch
		 * (JSONException e1) { revpayOrder.setStatus("Transaction Id Not found");
		 * revpayOrder.setPartyId(flwrefKey); result = "Transaction Id Not found";
		 * paymentGatewayRepository.save(revpayOrder); e1.printStackTrace(); }
		 */

		String locale = "en";
		String dateFormat = "dd MMMM yyyy";

		if (status.equalsIgnoreCase("successful")) {

			revpayOrder.setStatus("Success");
			// revpayOrder.setPartyId(flwrefKey);
			revpayOrder.setPartyId("test");
			paymentGatewayRepository.save(revpayOrder);
			JSONObject paymentJson = new JSONObject();
			SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
			if (revpayOrder.getType().equalsIgnoreCase("LEASEVERIFICATION_Payment")) {
				LeaseDetails leaseDetails = leaseDetailsRepository
						.findLeaseDetailsByMobileNo(revpayOrder.getDeviceId());
				if (leaseDetails.getStatus().equalsIgnoreCase("Payment_pending")) {
					leaseDetails.setStatus("NIN_Pending");
					leaseDetailsRepository.save(leaseDetails);
				}else {
					leaseDetails.setStatus("something went wrong");
				}
				try {
					indexPath = new URI("https://52.22.65.59:8877/#/Registration/LeaseVerification/"+leaseDetails.getMobileNumber());
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
				ItemSale itemSale = itemSaleRepository.findOne(Long.parseLong(revpayOrder.getDeviceId()));
				itemSale.setStatus("PENDING");
				itemSaleRepository.save(itemSale);
				try {
					indexPath = new URI("https://billing.moreplextv.com/#/inventory/" + txref);
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
					indexPath = new URI("https://52.22.65.59:8877/#/DTH-OnlinePayment/" + txref);
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
					indexPath = new URI("https://billing.moreplextv.com/#/renewal-customer/" + txref);
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
