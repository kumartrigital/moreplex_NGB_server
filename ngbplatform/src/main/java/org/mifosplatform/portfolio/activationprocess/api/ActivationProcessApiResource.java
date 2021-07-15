package org.mifosplatform.portfolio.activationprocess.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.json.JSONException;
import org.json.JSONObject;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.logistics.itemdetails.service.ItemDetailsReadPlatformService;
import org.mifosplatform.portfolio.activationprocess.domain.LeaseDetails;
import org.mifosplatform.portfolio.activationprocess.domain.LeaseDetailsRepository;
import org.mifosplatform.portfolio.activationprocess.exception.LeaseDetailsNotFoundException;
import org.mifosplatform.portfolio.activationprocess.service.ActivationProcessWritePlatformService;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.html.CssApplier;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

@Path("/activationprocess")
@Component
@Scope("singleton")
public class ActivationProcessApiResource {
	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(
			Arrays.asList("id", "officeId", "salutation", "fistName", "lastName", "email", "mobileNumber", "NIN",
					"city", "state", "country", "Status", "otp"));
	private final static Logger logger = LoggerFactory.getLogger(ActivationProcessApiResource.class);

	private final ToApiJsonSerializer<ClientData> toApiJsonSerializer;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final ItemDetailsReadPlatformService itemDetailsReadPlatformService;
	private final LeaseDetailsRepository leaseDetailsRepository;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final ActivationProcessWritePlatformService activationProcessWritePlatformService;

	@Autowired
	public ActivationProcessApiResource(final ToApiJsonSerializer<ClientData> toApiJsonSerializer,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final ItemDetailsReadPlatformService itemDetailsReadPlatformService,
			final LeaseDetailsRepository leaseDetailsRepository,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final ActivationProcessWritePlatformService activationProcessWritePlatformService) {
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.itemDetailsReadPlatformService = itemDetailsReadPlatformService;
		this.leaseDetailsRepository = leaseDetailsRepository;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.activationProcessWritePlatformService = activationProcessWritePlatformService;

	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String create(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder() //
				.activateProcess() //
				.withJson(apiRequestBodyAsJson) //
				.build(); //

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

	@POST
	@Path("selfregistration")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createSelfRegistration(final String apiRequestBodyAsJson) {

		logger.info("selfregistration: " + apiRequestBodyAsJson);

		final CommandWrapper commandRequest = new CommandWrapperBuilder().selfRegistrationProcess()
				.withJson(apiRequestBodyAsJson).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		logger.info("result: " + result);
		return this.toApiJsonSerializer.serialize(result);
	}

	@POST
	@Path("simpleactivation")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createSimpleActivation(final String apiRequestBodyAsJson) {

		logger.info("Simple Activation starting");
		final CommandWrapper commandRequest = new CommandWrapperBuilder() //
				.createSimpleActivation() //
				.withJson(apiRequestBodyAsJson) //
				.build(); //

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * this api is used to add device,add plan and active that plan to customer
	 */
	@POST
	@Path("simpleactivation/{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createSimpleActivation(final String apiRequestBodyAsJson,
			@PathParam("clientId") final Long clientId) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder() //
				.createClientSimpleActivation(clientId) //
				.withClientId(clientId).withJson(apiRequestBodyAsJson) //
				.build(); //

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * this api is used to add device,add plan and active that plan to customer
	 */
	@POST
	@Path("hardwareplanactivation/{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createHardwarePlanActivation(final String apiRequestBodyAsJson,
			@PathParam("clientId") final Long clientId) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder() //
				.createClientHardwarePlanActivation(clientId) //
				.withJson(apiRequestBodyAsJson) //
				.build(); //

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

	@POST
	@Path("customeractivation")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createCActivation(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder() //
				.createCustomerActivation() //
				.withJson(apiRequestBodyAsJson) //
				.build(); //

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * this api is used to add device,add plan and active that plan to customer
	 */
	@POST
	@Path("serviceactivationwod/{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createServiceActivationWithoutDevice(final String apiRequestBodyAsJson,
			@PathParam("clientId") final Long clientId) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder() //
				.createServiceActivationWithoutDevice(clientId) //
				.withClientId(clientId).withJson(apiRequestBodyAsJson) //
				.build(); //

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * this api is used to add device,add plan and active that plan to customer
	 */
	@POST
	@Path("verifyNIN/{NINID}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String verifyNIN(final String apiRequestBodyAsJson, @PathParam("NINID") final Long NINID) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder() //
				.verifyNIN(NINID) //
				.withClientId(NINID).withJson(apiRequestBodyAsJson) //
				.build(); //

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

	@POST
	@Path("lease")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response LeaseActivations(final String apiRequestBodyAsJson) {
		LeaseDetails leaseDetails = new LeaseDetails();
		HashMap<String, String> response = new HashMap<String, String>();

		try {

			JSONObject requestPayload = new JSONObject(apiRequestBodyAsJson);
			String mobile = requestPayload.getString("mobile");
			LeaseDetails leaseDetailscheck = leaseDetailsRepository.findLeaseDetailsByMobileNo(mobile);

			if (leaseDetailscheck != null) {
				response.put("message", "Mobile Number Alredy present");
				return Response.status(400).entity(response).build();
			}
			if (mobile.length() != 10) {
				response.put("message", "Mobile Number less than 10 Digits");
				return Response.status(400).entity(response).build();
			}

			leaseDetails.setOfficeId(requestPayload.getLong("officeId"));
			leaseDetails.setFirstName(requestPayload.getString("forename"));
			leaseDetails.setLastName(requestPayload.getString("surname"));
			leaseDetails.setEmail(requestPayload.getString("email"));
			leaseDetails.setMobileNumber(requestPayload.getString("mobile"));

			leaseDetails.setNIN(requestPayload.getString("NIN"));
			// leaseDetails.setBVN(requestPayload.getString("BVN"));
			leaseDetails.setCity(requestPayload.getString("city"));
			leaseDetails.setState(requestPayload.getString("state"));
			leaseDetails.setCountry(requestPayload.getString("country"));

			String otp = new DecimalFormat("000000").format(new Random().nextInt(999999));
			leaseDetails.setStatus("Otp_Pending");
			leaseDetails.setOtp(otp);
			String ImageBase64Encoder = requestPayload.getString("image");
			if (ImageBase64Encoder == null) {
				response.put("message", "Image is Required");
				return Response.status(400).entity(response).build();
			}
			String path = activationProcessWritePlatformService.saveImage(ImageBase64Encoder);
			leaseDetails.setImagePath(path);
			try {
				ResponseEntity<String> result = activationProcessWritePlatformService
						.OTP_MESSAGE(leaseDetails.getMobileNumber(), otp);

				if (!result.getStatusCode().equals(HttpStatus.OK)) {

					return Response.status(400).entity(result.getBody()).build();
				}
			} catch (Exception e) {
				response.put("message", "Please Check Your Mobile Number");
				return Response.status(400).entity(response).build();
			}
			leaseDetailsRepository.saveAndFlush(leaseDetails);

		} catch (JSONException e) {
			e.printStackTrace();
			response.put("message", "Mobile Number less than 10 Digits");

			return Response.status(500).entity(response).build();
		} catch (Exception e) {
			e.printStackTrace();
			response.put("message", "Mobile Number less than 10 Digits");

			return Response.status(500).entity(response).build();
		}
		return Response.status(200).entity(leaseDetails).build();

	}

	@POST
	@Path("leasevalidation")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String leasevalidation(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().leaseValidation()
				.withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@GET
	@Path("lease/{mobileNo}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveLeaseDetails(@Context final UriInfo uriInfo, @PathParam("mobileNo") final String mobileNo) {
		// this.context.authenticatedUser().validateHasReadPermission(this.RESOURCE_TYPE);

		LeaseDetails leaseDetails = leaseDetailsRepository.findLeaseDetailsByMobileNo(mobileNo);
		if (leaseDetails == null) {
			throw new LeaseDetailsNotFoundException("leasedetails not found");
		}
		return this.toApiJsonSerializer.serialize(leaseDetails);

	}

	@POST
	@Path("/resendotp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String resend_Otp_Message(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().resendOtpMessage()
				.withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	public StringTemplate getStringTemplate() {
		final StringTemplateGroup group = new StringTemplateGroup("Generators");
		return group.getInstanceOf("agreement");
	}

	@GET
	@Path("generatePDF/{mobileNo}")
	public void exportToPDF(final HttpServletRequest request, final HttpServletResponse response,
			@PathParam("mobileNo") String mobileNo) throws IOException {

		OutputStream os = null;
		try {

			LeaseDetails leaseDetails = leaseDetailsRepository.findLeaseDetailsByMobileNo(mobileNo);

			StringTemplate page = getStringTemplate();
			page.setAttribute("data", leaseDetails);

			String content = page.toString();

			final HtmlCleaner htmlCleaner = new HtmlCleaner();
			final TagNode tagNode = htmlCleaner.clean(content);
			content = htmlCleaner.getInnerHtml(tagNode);

			os = response.getOutputStream();

			Document document = null;

			document = new Document(PageSize.A4);

			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final PdfWriter writer = PdfWriter.getInstance(document, baos);
			document.open();
			

			final HtmlPipelineContext htmlContext = new HtmlPipelineContext(null);

			htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());

			final CSSResolver cssResolver = XMLWorkerHelper.getInstance().getDefaultCssResolver(true);
			final Pipeline<?> pipeline = new CssResolverPipeline(cssResolver,
					new HtmlPipeline(htmlContext, new PdfWriterPipeline(document, writer)));
			final XMLWorker worker = new XMLWorker(pipeline, true);
			final XMLParser parser = new XMLParser(worker);

			try {
				parser.parse(new StringReader(content));
			} catch (final Exception e) {
				e.printStackTrace();

			}

			document.close();
			response.setContentType("Content-Type: text/html; charset=UTF-8");
			response.addHeader("Content-Disposition", "attachment; filename=agreement.pdf");
			response.setContentLength(baos.size());
			baos.writeTo(os);
		} catch (final IOException | DocumentException e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null) {
					os.flush();
					os.close();
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

}
