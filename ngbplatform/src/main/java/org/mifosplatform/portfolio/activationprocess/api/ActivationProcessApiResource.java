package org.mifosplatform.portfolio.activationprocess.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.logistics.itemdetails.service.ItemDetailsReadPlatformService;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/activationprocess")
@Component
@Scope("singleton")
public class ActivationProcessApiResource {

    private final ToApiJsonSerializer<ClientData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ItemDetailsReadPlatformService itemDetailsReadPlatformService;
    private final static Logger logger = LoggerFactory.getLogger(ActivationProcessApiResource.class);

    @Autowired
    public ActivationProcessApiResource(final ToApiJsonSerializer<ClientData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ItemDetailsReadPlatformService itemDetailsReadPlatformService) {
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.itemDetailsReadPlatformService = itemDetailsReadPlatformService;
  
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
    	
    	logger.info("selfregistration: "+apiRequestBodyAsJson);
    	

        final CommandWrapper commandRequest = new CommandWrapperBuilder().selfRegistrationProcess().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        logger.info("result: "+result);
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
    public String createSimpleActivation(final String apiRequestBodyAsJson,@PathParam("clientId") final Long clientId) {

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
    public String createHardwarePlanActivation(final String apiRequestBodyAsJson,@PathParam("clientId") final Long clientId) {

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
    public String createServiceActivationWithoutDevice(final String apiRequestBodyAsJson,@PathParam("clientId") final Long clientId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createServiceActivationWithoutDevice(clientId) //
                .withClientId(clientId).withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
    
}
