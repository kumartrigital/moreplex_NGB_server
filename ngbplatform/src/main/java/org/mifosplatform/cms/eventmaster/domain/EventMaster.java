package org.mifosplatform.cms.eventmaster.domain;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.mifosplatform.annotation.ComparableFields;
import org.mifosplatform.cms.eventprice.domain.EventPrice;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * Domian for {@link EventMaster} 
 * @author pavani
 *
 */
@ComparableFields(on={"eventName", "eventDescription", "status", "eventStartDate", "eventStartTime", "eventEndDate", "eventDuration", "networkSystemCode", "eventValidity", "eventCategory"})
@Entity
@Table(name = "b_mod_master")
public class EventMaster extends AbstractPersistable<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Column(name = "event_name")
	private String eventName;
	
	@Column(name = "event_description")
	private String eventDescription;
	
	@Column(name = "status")
	private Integer status;
	
	@Column(name = "event_start_date")
	private Date eventStartDate;
	
	@Column(name = "event_start_time")
	private Date eventStartTime;
	
	@Column(name = "event_end_date")
	private Date eventEndDate;
	
	@Column(name = "event_duration")
	private Integer eventDuration;
	
	@Column(name = "network_system_code")
	private String networkSystemCode;
		
	@Column(name = "event_validity")
	private Date eventValidity;
	
	@Column(name = "createdby_id")
	private Long createdbyId;
	
	@Column(name = "created_date")
	private Date createdDate;
	
	@Column(name = "charge_code")
	private String chargeCode;
	
	@Column(name = "event_category")
	private String eventCategory;
	
	
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL , mappedBy = "event" , orphanRemoval = true)
	private List<EventDetails> details = new ArrayList<EventDetails>();
	
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL , mappedBy = "eventId" , orphanRemoval = true)
	private List<EventPrice> eventPricings = new ArrayList<EventPrice>();
    
	
	public static EventMaster fromJsom(final JsonCommand command) throws ParseException {
		
		final String eventName = command.stringValueOfParameterNamed("eventName");
		final String eventDescription = command.stringValueOfParameterNamed("eventDescription");
		final Integer status = command.integerValueOfParameterNamed("status");
		final String startDate = command.stringValueOfParameterNamed("eventStartDate"); 
		final String startTime = command.stringValueOfParameterNamed("eventStartTime"); 
		final String endDate = command.stringValueOfParameterNamed("eventEndDate"); 
		final Integer eventDuration = command.integerValueOfParameterNamed("eventDuration");
		final String networkSystemCode = command.stringValueOfParameterNamed("networkSystemCode"); 
		Date eventStartDate = null;
		Date eventStartTime = null;
		Date eventEndDate = null;
		final String chargeCode = command.stringValueOfParameterNamed("chargeCode");
		final String eventCategory = command.stringValueOfParameterNamed("eventCategory");
		DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
		DateFormat dateFormat2 = new SimpleDateFormat("dd MMMM yyyy HH:mm");
		
		if("Live Event".equalsIgnoreCase(eventCategory)){
			dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");
		}
        
		if(startDate.equalsIgnoreCase("")){
			eventStartDate = null;
		}else{
			eventStartDate = dateFormat.parse(startDate);
		}
		if(endDate.equalsIgnoreCase("")){
			eventEndDate = null;
		}else{
			eventEndDate = dateFormat.parse(endDate);
		}
		if(startTime.equalsIgnoreCase("")){
			eventStartTime = null;
		}else{
			eventStartTime = dateFormat2.parse(startTime);
		}
		LocalDateTime eventValidity = command.localDateTimeValueOfParameterNamed("eventValidity");
		eventValidity = (new LocalDateTime(eventStartTime)).plusMinutes(eventDuration);
		
		return new EventMaster(eventName, eventDescription, status, eventStartDate, eventStartTime, eventEndDate, eventDuration, networkSystemCode, eventValidity, eventCategory, chargeCode);
	}
	
	public EventMaster (final String eventName, final String eventDescription, final Integer status, final Date eventStartDate, final Date eventStartTime,
			final Date eventEndDate, final Integer eventDuration, final String networkSystemCode, final LocalDateTime eventValidity, final String eventCategory, final String chargeCode) {

		this.eventName = eventName;
		this.eventDescription = eventDescription;
		this.status = status;
		this.eventStartDate = eventStartDate;
		this.eventEndDate = eventEndDate != null ? eventEndDate : null;
		this.eventStartTime = eventStartTime;
		this.eventDuration = eventDuration;
		this.networkSystemCode = networkSystemCode;
		this.eventValidity = eventValidity.toDate();
		this.createdDate = DateUtils.getDateOfTenant();
		this.eventCategory = eventCategory;
		this.chargeCode = chargeCode;
	}
	
	public EventMaster() {
		
	}
	
	
	public List<EventDetails> getDetails() {
		return details;
	}

	public void addMediaDetails(final EventDetails details){
		details.update(this);
		this.details.add(details);
	}
	
	public void delete() {
		this.eventEndDate = DateUtils.getDateOfTenant();
	}
	
	
	
	public String getEventCategory() {
		return eventCategory;
	}

	/**
	 * @return the eventName
	 */
	public String getEventName() {
		return eventName;
	}
	
	public List<EventPrice> getEventPricings() {
		return eventPricings;
	}

	/**
	 * @param eventName the eventName to set
	 */
	public void setEventName(final String eventName) {
		this.eventName = eventName;
	}

	/**
	 * @return the eventDescription
	 */
	public String getEventDescription() {
		return eventDescription;
	}

	/**
	 * @param eventDescription the eventDescription to set
	 */
	public void setEventDescription(final String eventDescription) {
		this.eventDescription = eventDescription;
	}

	/**
	 * @return the status
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(final Integer status) {
		this.status = status;
	}

	/**
	 * @return the eventStartDate
	 */
	public Date getEventStartDate() {
		return eventStartDate;
	}

	/**
	 * @param eventStartDate the eventStartDate to set
	 */
	public void setEventStartDate(final Date eventStartDate) {
		this.eventStartDate = eventStartDate;
	}

	/**
	 * @return the eventEndDate
	 */
	public Date getEventEndDate() {
		return eventEndDate;
	}

	/**
	 * @param eventEndDate the eventEndDate to set
	 */
	public void setEventEndDate(final Date eventEndDate) {
		this.eventEndDate = eventEndDate;
	}

	/**
	 * @return the eventValidity
	 */
	public Date getEventValidity() {
		return eventValidity;
	}

	/**
	 * @param eventValidity the eventValidity to set
	 */
	public void setEventValidity(final Date eventValidity) {
		this.eventValidity = eventValidity;
	}

	/**
	 * @return the details
	 */
	public List<EventDetails> getEventDetails() {
		return details;
	}

	/**
	 * @param details the details to set
	 */
	public void setDetails(final List<EventDetails> details) {
		this.details = details;
	}

	/**
	 * @return the createdbyId
	 */
	public Long getCreatedbyId() {
		return createdbyId;
	}

	/**
	 * @param createdbyId the createdbyId to set
	 */
	public void setCreatedbyId(final Long createdbyId) {
		this.createdbyId = createdbyId;
	}

	/**
	 * @return the createdDate
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * @param createdDate the createdDate to set
	 */
	public void setCreatedDate(final Date createdDate) {
		this.createdDate = createdDate;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getChargeCode() {
		return chargeCode;
	}
	
	public Date getEventStartTime() {
		return eventStartTime;
	}

	public void setEventStartTime(Date eventStartTime) {
		this.eventStartTime = eventStartTime;
	}

	public Integer getEventDuration() {
		return eventDuration;
	}

	public void setEventDuration(Integer eventDuration) {
		this.eventDuration = eventDuration;
	}

	public String getNetworkSystemCode() {
		return networkSystemCode;
	}

	public void setNetworkSystemCode(String networkSystemCode) {
		this.networkSystemCode = networkSystemCode;
	}

	public java.util.Map<String, Object> updateEventDetails(final JsonCommand command) throws ParseException {

		final LinkedHashMap<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		String eventNameNamedParamName = "eventName";
		String eventDescriptionNamedParamName = "eventDescription"; 
		String statusNamedParamName = "status";  
		String eventStartDateNamedParamName = "eventStartDate";
		String eventEndDateNamedParamName = "eventEndDate";
		String eventValidityNamedParamName = "eventValidity";
		String eventCategoryNamedParamName = "eventCategory";
		String chargeCodeNamedParamName = "chargeCode";
		String eventStartTimeNamedParamName = "eventStartTime";
		String eventDurationNamedParamName = "eventDuration";
		String networkSystemCodeNamedParamName = "networkSystemCode";
		DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
		DateFormat dateFormat2 = new SimpleDateFormat("dd MMMM yyyy HH:mm"); 
		
		if(command.isChangeInStringParameterNamed(eventCategoryNamedParamName, this.eventCategory)){
				final String newEventCategoryValue = command.stringValueOfParameterNamed(eventCategoryNamedParamName);
				actualChanges.put(eventCategoryNamedParamName, newEventCategoryValue);
				this.eventCategory = StringUtils.defaultIfEmpty(newEventCategoryValue, null);
		}
		if("Live Event".equalsIgnoreCase(this.eventCategory)){
				dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss"); 
		}
		if(command.isChangeInStringParameterNamed(eventNameNamedParamName, this.eventName)){
			final String newEventNameValue = command.stringValueOfParameterNamed(eventNameNamedParamName);
			actualChanges.put(eventNameNamedParamName, newEventNameValue);
			this.eventName = StringUtils.defaultIfEmpty(newEventNameValue, null);
		}
		if(command.isChangeInStringParameterNamed(eventDescriptionNamedParamName, this.eventDescription)){
			final String newEventDescriptionValue = command.stringValueOfParameterNamed(eventDescriptionNamedParamName);
			actualChanges.put(eventDescriptionNamedParamName, newEventDescriptionValue);
			this.eventDescription = StringUtils.defaultIfEmpty(newEventDescriptionValue, null);
		}
		if(command.isChangeInIntegerParameterNamed(statusNamedParamName, this.status)){
			
			final Integer newStatusValue= command.integerValueOfParameterNamed(statusNamedParamName);
			actualChanges.put(statusNamedParamName, newStatusValue);
			this.status=newStatusValue;
		}
		if(command.isChangeInStringParameterNamed(eventStartDateNamedParamName, this.eventStartDate.toString())){
			final String startDate = command.stringValueOfParameterNamed("eventStartDate"); 
			if(startDate.equalsIgnoreCase("")){
				this.eventStartDate = null;
			}else{
				this.eventStartDate = dateFormat.parse(startDate);
			}
			actualChanges.put(eventStartDateNamedParamName, startDate);
		}
		
		final String endDate = command.stringValueOfParameterNamed("eventEndDate");
		if(endDate != null){
			this.eventEndDate = dateFormat.parse(endDate);
		}else{
			this.eventEndDate = null;
		}
		actualChanges.put(eventEndDateNamedParamName, endDate);	
		
		if(command.isChangeInStringParameterNamed(eventStartTimeNamedParamName, this.eventStartTime.toString())){
			final String startTime = command.stringValueOfParameterNamed(eventStartTimeNamedParamName); 
			if(startTime.equalsIgnoreCase("")){
				this.eventStartTime = null;
			}else{
				this.eventStartTime = dateFormat2.parse(startTime);
			}
			actualChanges.put(eventStartTimeNamedParamName, startTime);
		}
		
		if(command.isChangeInIntegerParameterNamed(eventDurationNamedParamName, this.eventDuration)){	
			final Integer newEventDuration= command.integerValueOfParameterNamed(eventDurationNamedParamName);
			actualChanges.put(eventDurationNamedParamName, newEventDuration);
			this.eventDuration=newEventDuration;
		}
		
		LocalDateTime newEventValidityValue = command.localDateTimeValueOfParameterNamed(eventValidityNamedParamName);
		newEventValidityValue = new LocalDateTime(this.eventStartTime).plusMinutes(this.eventDuration);
		actualChanges.put(eventValidityNamedParamName, newEventValidityValue);
		this.eventValidity = newEventValidityValue.toDate();
			
		if(command.isChangeInStringParameterNamed(chargeCodeNamedParamName, this.chargeCode)){
			final String newChargeCodeValue = command.stringValueOfParameterNamed(chargeCodeNamedParamName);
			actualChanges.put(chargeCodeNamedParamName, newChargeCodeValue);
			this.chargeCode = StringUtils.defaultIfEmpty(newChargeCodeValue,null);
		}
		
		if(command.isChangeInStringParameterNamed(networkSystemCodeNamedParamName, this.networkSystemCode)){
			final String newNetworkSystemCode = command.stringValueOfParameterNamed(networkSystemCodeNamedParamName);
			actualChanges.put(networkSystemCodeNamedParamName, newNetworkSystemCode);
			this.networkSystemCode = StringUtils.defaultIfEmpty(newNetworkSystemCode,null);
		}
		
		return actualChanges;
	}
}
