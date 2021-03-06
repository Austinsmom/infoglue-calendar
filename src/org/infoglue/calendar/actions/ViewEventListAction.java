/* ===============================================================================
*
* Part of the InfoGlue Content Management Platform (www.infoglue.org)
*
* ===============================================================================
*
*  Copyright (C)
* 
* This program is free software; you can redistribute it and/or modify it under
* the terms of the GNU General Public License version 2, as published by the
* Free Software Foundation. See the file LICENSE.html for more information.
* 
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License along with
* this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
* Place, Suite 330 / Boston, MA 02111-1307 / USA.
*
* ===============================================================================
*/

package org.infoglue.calendar.actions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.infoglue.calendar.controllers.EventController;
import org.infoglue.calendar.controllers.ResourceController;
import org.infoglue.calendar.entities.Calendar;
import org.infoglue.calendar.entities.Event;
import org.infoglue.calendar.entities.EventCategory;
import org.infoglue.calendar.entities.EventTypeCategoryAttribute;
import org.infoglue.calendar.entities.EventVersion;
import org.infoglue.calendar.util.CalendarHelper;
import org.infoglue.common.util.RemoteCacheUpdater;
import org.infoglue.common.util.Timer;
import org.infoglue.common.util.VelocityTemplateProcessor;
import org.infoglue.common.util.VisualFormatter;
import org.infoglue.common.util.rss.RssHelper;
//import org.infoglue.common.util.Timer;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionContext;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * This action represents a Calendar Administration screen.
 * 
 * @author Mattias Bogeblad
 */

public class ViewEventListAction extends CalendarAbstractAction
{
	private static Log log = LogFactory.getLog(ViewEventListAction.class);

    private String calendarId 			= "";
    private String categories 			= "";
    private String categoryAttribute	= "";
    private String categoryNames 		= "";
    private String includedLanguages 	= "";
    private Calendar calendar;    
    private Set events;
    private List aggregatedEntries 		= null;
    private String message				= "";
    
    //This is for the new filtering list
    private String startDateTime 		= null;
    private String endDateTime 			= null;
    private java.util.Calendar startCalendar;
    private java.util.Calendar endCalendar;
    private String freeText 			= null;
    private String calendarMonth		= null;
    private java.util.Calendar calendarMonthCalendar;
    private String forwardMonthUrl		= null;
    private String backwardMonthUrl		= null;
    private String filterDescription	= null;
    private Integer numberOfItems 		= null;
    
    VisualFormatter vf = new VisualFormatter();
    
    /**
     * This is the entry point for the main listing.
     */
    
    public String execute() throws Exception 
    {
        String[] calendarIds = calendarId.split(",");
        //String[] categoryNamesArray = categoryNames.split(",");
        
        Session session = getSession(true);
    	        
        Map<String, String[]> categories = handleCategories();
        //this.events = EventController.getController().getEventList(calendarIds, categoryAttribute, categoryNamesArray, includedLanguages, null, null, null, session);
        this.events = EventController.getController().getEventList(calendarIds, categories, includedLanguages, null, null, null, session);
  
        log.info("Registering usage at least:" + calendarId + " for siteNodeId:" + this.getSiteNodeId());
        RemoteCacheUpdater.setUsage(this.getSiteNodeId(), calendarIds);
        
        String presentationTemplate = getPresentationTemplate();
        log.info("presentationTemplate:" + presentationTemplate);
        if(presentationTemplate != null && !presentationTemplate.equals(""))
        {
		    Map parameters = new HashMap();
		    parameters.put("events", this.events);
		    parameters.put("this", this);
		    
			StringWriter tempString = new StringWriter();
			PrintWriter pw = new PrintWriter(tempString);
			new VelocityTemplateProcessor().renderTemplate(parameters, pw, presentationTemplate);
			String renderedString = tempString.toString();
			setRenderedString(renderedString);
        }
        
        return Action.SUCCESS;
    } 

    public String execute(Integer numberOfItems) throws Exception 
    {
        String[] calendarIds = calendarId.split(",");
        //String[] categoryNamesArray = categoryNames.split(",");
        
        Session session = getSession(true);
        
        Map<String, String[]> categories = handleCategories();

        //this.events = EventController.getController().getEventList(calendarIds,  getCategoryAttribute(), categoryNamesArray, getIncludedLanguages(), null, null, null, numberOfItems, session);
        this.events = EventController.getController().getEventList(calendarIds, categories, getIncludedLanguages(), null, null, null, numberOfItems, session);
        log.info("Registering usage at least:" + calendarId + " for siteNodeId:" + this.getSiteNodeId());
        RemoteCacheUpdater.setUsage(this.getSiteNodeId(), calendarIds);
        
        String presentationTemplate = getPresentationTemplate();
        log.info("presentationTemplate:" + presentationTemplate);
        if(presentationTemplate != null && !presentationTemplate.equals(""))
        {
		    Map parameters = new HashMap();
		    parameters.put("events", this.events);
		    parameters.put("this", this);
		    
			StringWriter tempString = new StringWriter();
			PrintWriter pw = new PrintWriter(tempString);
			new VelocityTemplateProcessor().renderTemplate(parameters, pw, presentationTemplate);
			String renderedString = tempString.toString();
			setRenderedString(renderedString);
        }

        return Action.SUCCESS;
    } 

    public String listAsRSS() throws Exception
    {
        execute(getNumberOfItems());
        
        return Action.SUCCESS + "RSS";
    }

    public String listAsAggregatedRSS() throws Exception
    {
        execute(getNumberOfItems());
        
        return Action.SUCCESS + "AggregatedRSS";
    }
    
    
    public String listGU() throws Exception
    {
        execute(getNumberOfItems());
        
        if(getRenderedString() != null && !getRenderedString().equals(""))
			return Action.SUCCESS + "RenderedTemplate";
        
        return Action.SUCCESS + "GU";
    }

	public String listCustom() throws Exception
    {
        execute(getNumberOfItems());
        
        if(getRenderedString() != null && !getRenderedString().equals(""))
			return Action.SUCCESS + "RenderedTemplate";

        return Action.SUCCESS + "Custom";
    }

    public String listAggregatedCustom() throws Exception
    {
		try
		{
	        execute();
	            		
			String externalRSSUrl = this.getStringAttributeValue("externalRSSUrl");
		
			String eventURL = this.getStringAttributeValue("detailUrl");
			if(eventURL == null)
				eventURL = "";
		
		    
			if(externalRSSUrl == null || externalRSSUrl.equalsIgnoreCase(""))
			{
				String defaultUrl = "http://aktuellt.slu.se/kalendarium_rss.cfm";
				log.error("You must send in an attribute called externalRSSUrl to this view. Defaulting feed \"" + defaultUrl + "\".");
				externalRSSUrl = defaultUrl;
			}
			
		    List entries = getExternalFeedEntries(externalRSSUrl);
			List internalEntries = getInternalFeedEntries(eventURL);	
		
			entries.addAll(internalEntries);
			
			sortEntries(entries);
		
			log.info("entries:" + entries.size());
			
			aggregatedEntries = entries;
			
	        String presentationTemplate = getPresentationTemplate();
	        log.info("presentationTemplate:" + presentationTemplate);
	        if(presentationTemplate != null && !presentationTemplate.equals(""))
	        {
			    Map parameters = new HashMap();
			    parameters.put("aggregatedEntries", aggregatedEntries);
			    parameters.put("this", this);
			    
				StringWriter tempString = new StringWriter();
				PrintWriter pw = new PrintWriter(tempString);
				new VelocityTemplateProcessor().renderTemplate(parameters, pw, presentationTemplate);
				String renderedString = tempString.toString();
				setRenderedString(renderedString);

				return Action.SUCCESS + "RenderedTemplate";
	        }

		}
		catch(Exception e)
		{
			setError(e.getMessage(), e);
			return Action.ERROR + "Custom";
		}

        return Action.SUCCESS + "AggregatedCustom";
    }

    public String listSlottedGU() throws Exception
    {
        execute();

        if(getRenderedString() != null && !getRenderedString().equals(""))
			return Action.SUCCESS + "RenderedTemplate";

        return Action.SUCCESS + "SlotGU";
    }

    protected Map<String, String[]> handleCategories()
	{
    	Map<String, String[]> categories = new HashMap<String, String[]>();
        if ((categoryNames != null && categoryNames.length() > 0) || (categoryAttribute != null && categoryAttribute.length() > 0))
        {
        	log.info("Request is using the old category parameter handling.");
        	if (log.isDebugEnabled())
        	{
        		log.debug("Request category information: categoryAttribute: '" + categoryAttribute + "', categoryNames: '" + categoryNames + "'");
        	}
        	String[] categoryNamesArray = categoryNames.split(",");
        	if (categoryAttribute != null && categoryAttribute.length() > 0)
        	{
        		categories.put(categoryAttribute, categoryNamesArray);
        	}
        	else
        	{
        		// Using the key/label is safe since in this case the user has not provided any key (i.e. no conflicts)
        		categories.put("", categoryNamesArray);
        	}
        }
        else
        {
	        Map parameters = ActionContext.getContext().getParameters();
	        for (Object parameter : parameters.keySet())
	        {
	        	if (parameter instanceof String && ((String)parameter).startsWith("category_"))
	        	{
	        		String parameterKey = (String)parameter;
	        		Object parameterValueObject = parameters.get(parameterKey);
	        		if (log.isDebugEnabled())
	            	{
	            		log.debug("Found category in request: " + parameterKey);
	            	}

	        		// substring removes the 'category_'-part of the key
	        		parameterKey = parameterKey.substring(9);
	        		if (parameterValueObject != null)
	        		{
	        			String[] parameterValueList = (String[])parameterValueObject;
	        			if (parameterValueList.length > 0 && parameterValueList[0] != null)
	        			{
	        				categories.put(parameterKey, parameterValueList[0].split(","));
	        			}
	        		}

	        		if (!categories.containsKey(parameterKey))
	        		{
	        			categories.put(parameterKey, null);
	        		}
	        	}
	        }
        }

        return categories;
	}

    public String listFilteredGU() throws Exception
    {
    	if(startDateTime == null)
    		startDateTime = getStartDateTime();
    	
    	if(endDateTime == null)
    		endDateTime = getEndDateTime();

    	if(freeText == null)
    		freeText = getFreeText();

    	if(categoryAttribute == null)
    		categoryAttribute = getCategoryAttribute();

    	if(categoryNames == null)
    		categoryNames = getCategoryNames();

    	if(calendarMonth == null)
    		calendarMonth = getCalendarMonth();

    	if(numberOfItems == null)
    		numberOfItems = getNumberOfItemsNoFallback();
    	System.out.println("numberOfItems:" + numberOfItems);
    	
    	log.info("freeText:" + freeText);
    	log.info("startDateTime:" + startDateTime);
    	log.info("endDateTime:" + endDateTime);
    	log.info("categoryAttribute:" + categoryAttribute);
    	log.info("categoryNames:" + categoryNames);
    	log.info("calendarMonth:" + calendarMonth);
    	log.info("numberOfItems:" + numberOfItems);
    	
    	if(startDateTime != null && startDateTime.length() > 0)
            startCalendar = getCalendar(startDateTime, "yyyy-MM-dd", true); 
        else
        {
            startCalendar = java.util.Calendar.getInstance();
            startCalendar.set(java.util.Calendar.DAY_OF_MONTH, 1); 
        }

        if(endDateTime != null && endDateTime.length() > 0 && getDaysInTheFuture() == null) {
        	endCalendar = getCalendar(endDateTime, "yyyy-MM-dd", true); 

        }
        else
        {
        	endCalendar = java.util.Calendar.getInstance();

            if (getDaysInTheFuture() != null && getDaysInTheFuture() != -1) {

            	endCalendar.add(startCalendar.DATE, getDaysInTheFuture());
          
            } else {
            	int lastDate = endCalendar.getActualMaximum(java.util.Calendar.DATE);
            	endCalendar.set(java.util.Calendar.DAY_OF_MONTH, lastDate); 
            }
        }

        if(calendarMonth != null && calendarMonth.length() > 0)
        {
        	calendarMonthCalendar = getCalendar(calendarMonth, "yyyy-MM", true); 
        	if(startDateTime == null || startDateTime.length() == 0)
	    	{
	    		startCalendar.setTime(calendarMonthCalendar.getTime());
	    		startCalendar.set(java.util.Calendar.DAY_OF_MONTH, 1);
	    	}
	    	if(endDateTime == null || endDateTime.length() == 0)
	    	{
	    		endCalendar.setTime(calendarMonthCalendar.getTime());
	    		int lastDate = endCalendar.getActualMaximum(java.util.Calendar.DATE);
	            endCalendar.set(java.util.Calendar.DAY_OF_MONTH, lastDate); 
	    	}
        }
        else
        {
        	calendarMonthCalendar = java.util.Calendar.getInstance();
        	calendarMonthCalendar.set(java.util.Calendar.DAY_OF_MONTH, 1); 
        }

        if(freeText != null)
        {
        	filterDescription = "Matching \"" + freeText + "\"";
        }
        else
        {
	        if(startCalendar.getTimeInMillis() == endCalendar.getTimeInMillis())
	        	filterDescription = vf.formatDate(startCalendar.getTime(), getLocale(), "d MMMM");
	        else
	        	filterDescription = vf.formatDate(startCalendar.getTime(), getLocale(), "d MMMM") + " - " + vf.formatDate(endCalendar.getTime(), getLocale(), "d MMMM");
        }   
        
        if(categoryNames != null && categoryNames.length() > 0 && categoryNames.indexOf(",") == -1)
        {
        	filterDescription = filterDescription + " (" + (categoryNames.length() > 40 ? categoryNames.substring(0, 40) + "..." : categoryNames) + ")";
        }
        
        startCalendar.set(java.util.Calendar.HOUR_OF_DAY, 1);
        endCalendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
        log.info("startCalendar:" + startCalendar.getTime());
        log.info("endCalendar:" + endCalendar.getTime());
        
        String[] calendarIds = calendarId.split(",");
        Map<String, String[]> categories = handleCategories();
        
        Session session = getSession(true);

        //this.events = EventController.getController().getEventList(calendarIds, categoryAttribute, categoryNamesArray, includedLanguages, startCalendar, endCalendar, freeText, session);
        this.events = EventController.getController().getEventList(calendarIds, categories, includedLanguages, startCalendar, endCalendar, freeText, numberOfItems, session);

        log.info("Registering usage at least:" + calendarId + " for siteNodeId:" + this.getSiteNodeId());

        RemoteCacheUpdater.setUsage(this.getSiteNodeId(), calendarIds);
        
        String presentationTemplate = getPresentationTemplate();
        log.info("presentationTemplate:" + presentationTemplate);
        if(presentationTemplate != null && !presentationTemplate.equals(""))
        {
		    Map parameters = new HashMap();
		    parameters.put("events", this.events);
		    parameters.put("this", this);
		    
			StringWriter tempString = new StringWriter();
			PrintWriter pw = new PrintWriter(tempString);
			new VelocityTemplateProcessor().renderTemplate(parameters, pw, presentationTemplate);
			String renderedString = tempString.toString();
			setRenderedString(renderedString);
			
			return Action.SUCCESS + "RenderedTemplate";
        }

        return Action.SUCCESS + "FilteredGU";
    }

    public String listFilteredGraphicalCalendarGU() throws Exception
    {
    	listFilteredGU();

        if(getRenderedString() != null && !getRenderedString().equals(""))
			return Action.SUCCESS + "RenderedTemplate";

        return Action.SUCCESS + "FilteredGraphicalCalendarGU";
    }

    
    public String shortListGU() throws Exception
    {
        execute(getNumberOfItems());

        if(getRenderedString() != null && !getRenderedString().equals(""))
			return Action.SUCCESS + "RenderedTemplate";

        return Action.SUCCESS + "ShortGU";
    }

    public String listSlottedCustom() throws Exception
    {
        execute();
        
        if(getRenderedString() != null && !getRenderedString().equals(""))
			return Action.SUCCESS + "RenderedTemplate";

        return Action.SUCCESS + "SlotCustom";
    }

    public String shortListCustom() throws Exception
    {
        execute(getNumberOfItems());

        if(getRenderedString() != null && !getRenderedString().equals(""))
			return Action.SUCCESS + "RenderedTemplate";

        return Action.SUCCESS + "ShortCustom";
    }

    public String shortListAggregatedCustom() throws Exception
    {
        execute();

        if(getRenderedString() != null && !getRenderedString().equals(""))
			return Action.SUCCESS + "RenderedTemplate";

        return Action.SUCCESS + "ShortAggregatedCustom";
    }

    public String getCalendarId()
    {
        return calendarId;
    }
    
    public void setCalendarId(String calendarId)
    {
        this.calendarId = calendarId;
    }
    
    public Set getEvents()
    {
        return events;
    }

    public void setCategories(String categories)
    {
        this.categories = categories;
    }
    
    public String getIncludedLanguages()
    {
    	String includedLanguages = (String)ServletActionContext.getRequest().getAttribute("includedLanguages");
       	if(includedLanguages == null || includedLanguages.equals("")) {
       		includedLanguages = (String)ServletActionContext.getRequest().getParameter("includedLanguages");
       	}
    	if(includedLanguages == null || includedLanguages.equals("")) {
    		includedLanguages = "*";
    	}
    	
        return includedLanguages;
    }
    
    public String getEndDateTime()
    {
    	String endDateTime = (String)ServletActionContext.getRequest().getAttribute("endDateTime");
       	if(endDateTime == null || endDateTime.equals("")) {
       		endDateTime = (String)ServletActionContext.getRequest().getParameter("endDateTime");
       	}
    	
        return endDateTime;
    }
    
    public String getStartDateTime()
    {

    	String startDateTime = (String)ServletActionContext.getRequest().getAttribute("startDateTime");
       	if(startDateTime == null || startDateTime.equals("")) {
       		startDateTime = (String)ServletActionContext.getRequest().getParameter("startDateTime");
       	}
    	
        return startDateTime;
    }
    
    public Integer getDaysInTheFuture()
    {
    	Integer daysInTheFutureInt = null;
    	String daysInTheFuture = (String)ServletActionContext.getRequest().getAttribute("daysInTheFuture");
       	if(daysInTheFuture == null || daysInTheFuture.equals("")) {
       		daysInTheFuture = (String)ServletActionContext.getRequest().getParameter("daysInTheFuture");
       	}
       	if (daysInTheFuture != null && !daysInTheFuture.equals("")) {
	       	try {
	       		daysInTheFutureInt = Integer.parseInt(daysInTheFuture);
	   
	       	} catch (NumberFormatException numberFormatException) {
	       		log.warn("The property daysInTheFuture could not be parsed since it was:" + daysInTheFuture);
	       		return -1;
	       	}
       	}
        return daysInTheFutureInt;
    }
    
    
    public Integer getNumberOfItems()
    {
        Object o = ServletActionContext.getRequest().getAttribute("numberOfItems");
        if(o != null && o.toString().length() > 0 && !o.toString().equalsIgnoreCase("undefined"))
            return new Integer((String)o);
        else
            return new Integer(10);
    }

    public Integer getNumberOfItemsNoFallback()
    {
        Object o = ServletActionContext.getRequest().getAttribute("numberOfItems");
        if(o != null && o.toString().length() > 0 && !o.toString().equalsIgnoreCase("undefined"))
            return new Integer((String)o);
        else
            return null;
    }

    public List getEventCategories(String eventString, EventTypeCategoryAttribute categoryAttribute)
    {
        Object object = findOnValueStack(eventString);
        Event event = (Event)object;
        
        List categories = new ArrayList();
        
        Iterator i = event.getEventCategories().iterator();
        while(i.hasNext())
        {
            EventCategory eventCategory = (EventCategory)i.next();
            if(eventCategory.getEventTypeCategoryAttribute().getId().equals(categoryAttribute.getId()))
                categories.add(eventCategory.getCategory());
        }

        return categories;
    }

    public String getRSSXML()
    {
    	String rssXML = null;
    	
    	try
    	{
    		String eventURL = this.getStringAttributeValue("detailUrl");
    		if(eventURL == null)
    			eventURL = "";
    		
	    	SyndFeed feed = new SyndFeedImpl();
	        feed.setFeedType("atom_1.0");
    		String feedTypeString = this.getStringAttributeValue("feedType");
    		if(feedTypeString != null && !feedTypeString.equals(""))
    			feed.setFeedType(feedTypeString);
    					
	        feed.setTitle(this.getStringAttributeValue("feedTitle"));
	        feed.setLink(this.getStringAttributeValue("feedLink"));
	        feed.setDescription(this.getStringAttributeValue("feedDescription"));
	        
	        List entries = getInternalFeedEntries(eventURL);
	        
	    	feed.setEntries(entries);
	    	RssHelper rssHelper = new RssHelper();
	    	rssXML = rssHelper.render(feed);
    	}
    	catch(Throwable t)
    	{
    		t.printStackTrace();
    	}
    	
        return rssXML;
    }
    
    public String getAggregatedRSSXML()
    {
    	String rssXML = null;
    	
    	try
    	{
	    	SyndFeed feed = new SyndFeedImpl();
	        feed.setFeedType("atom_1.0");
    		String feedTypeString = this.getStringAttributeValue("feedType");
    		if(feedTypeString != null && !feedTypeString.equals(""))
    			feed.setFeedType(feedTypeString);
	
	        feed.setTitle(this.getStringAttributeValue("feedTitle"));
	        feed.setLink(this.getStringAttributeValue("feedLink"));
	        feed.setDescription(this.getStringAttributeValue("feedDescription"));

    		String eventURL = this.getStringAttributeValue("detailUrl");
    		if(eventURL == null)
    			eventURL = "";

	        String externalRSSUrl = this.getStringAttributeValue("externalRSSUrl");
    		if(externalRSSUrl == null || externalRSSUrl.equalsIgnoreCase(""))
    		{
    			String defaultUrl = "http://aktuellt.slu.se/kalendarium_rss.cfm";
    			log.error("You must send in an attribute called externalRSSUrl to this view. Defaulting to feed \"" + defaultUrl + "\".");
    			externalRSSUrl = defaultUrl;
    		}
    		
            List entries = getExternalFeedEntries(externalRSSUrl);
    		List internalEntries = getInternalFeedEntries(eventURL);	

    		entries.addAll(internalEntries);
    		
    		sortEntries(entries);
    		
	    	feed.setEntries(entries);
	    	RssHelper rssHelper = new RssHelper();
	    	rssXML = rssHelper.render(feed);
    	}
    	catch(Throwable t)
    	{
    		t.printStackTrace();
    	}
    	
        return rssXML;
    }

    public List getAggregatedEntries()
    {
        return aggregatedEntries;
    }


    private List getInternalFeedEntries(String eventURL)
    {
        List entries = new ArrayList();
        SyndEntry entry;
        SyndContent description;
        
        if(events != null)
        {
	    	Iterator eventsIterator = events.iterator();
	    	while(eventsIterator.hasNext())
	    	{
	    		Event event = (Event)eventsIterator.next();
	    		EventVersion eventVersion = this.getEventVersion(event);
	    		
	    		entry = new SyndEntryImpl();
	    		entry.setTitle(eventVersion.getName());
				entry.setLink(eventURL.replaceAll("\\{eventId\\}", "" + event.getId()));
	    		entry.setPublishedDate(event.getStartDateTime().getTime());
	    		entry.setUri(eventURL.replaceAll("\\{eventId\\}", "" + event.getId()));
	    		
	    		List categories = new ArrayList();
	    		Iterator eventCategoriesIterator = event.getEventCategories().iterator();
	    		while(eventCategoriesIterator.hasNext())
	    		{
	    			EventCategory eventCategory = (EventCategory)eventCategoriesIterator.next();
	    			SyndCategory syndCategory = new SyndCategoryImpl();
	    			syndCategory.setTaxonomyUri(eventCategory.getEventTypeCategoryAttribute().getInternalName());
	    			syndCategory.setName(eventCategory.getCategory().getLocalizedName(this.getLanguageCode(), "sv"));
	    			categories.add(syndCategory);
	    		}
	
	    		//--------------------------------------------
	    		// Add an extra category to internal entries, 
	    		// so that we can identify them later.
	    		//--------------------------------------------
	    		
				SyndCategory syndCategory = new SyndCategoryImpl();
				syndCategory.setTaxonomyUri("isInfoGlueLink");
				syndCategory.setName("true");
				categories.add(syndCategory);
	
	    		entry.setCategories(categories);
	    		
	    		description = new SyndContentImpl();
	    		description.setType("text/html");
	    		description.setValue(eventVersion.getShortDescription());
	    		entry.setDescription(description);
	
	    		List contents = new ArrayList();
	
	    		SyndContent metaData = new SyndContentImpl();
	
	    		StringBuffer xml = new StringBuffer("<![CDATA[<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	    		xml.append("<metadata>");
	    		xml.append("<startDateTime>" + this.formatDate(event.getStartDateTime().getTime(), "yyyy-MM-dd HH:mm") + "</startDateTime>");
	    		xml.append("<endDateTime>" + this.formatDate(event.getEndDateTime().getTime(), "yyyy-MM-dd HH:mm") + "</endDateTime>");
	    		xml.append("</metadata>]]>");
	
	    		metaData.setType("text/xml");
	    		metaData.setValue(xml.toString());
	    		
	    		contents.add(metaData);
	
	    		entry.setContents(contents);
	    		
	    		entries.add(entry);
	    	}
        }
        
    	return entries;
    }
    
    public List getExternalFeedEntries(String externalRSSUrl) throws Exception
    {    	
    	try
    	{
	    	URL url = new URL(externalRSSUrl);
	        URLConnection urlConn = url.openConnection();
	        urlConn.setConnectTimeout(5000);
	        urlConn.setReadTimeout(10000);
	        
	        SyndFeedInput input = new SyndFeedInput();
	        SyndFeed inputFeed = input.build(new XmlReader(urlConn));
	        
	        List entries = inputFeed.getEntries();
	        Iterator entriesIterator = entries.iterator();
	        while(entriesIterator.hasNext())
	        {
	        	SyndEntry entry = (SyndEntry)entriesIterator.next();
	        	Iterator contentIterator = entry.getContents().iterator();
	        	while(contentIterator.hasNext())
	        	{
	        		SyndContent content = (SyndContent)contentIterator.next();
	        		log.info("content:" + content.getValue());
	        		if(content.getType().equalsIgnoreCase("text/xml"))
	        			content.setValue("<![CDATA[" + content.getValue() + "]]>");
	        	}
	        }

	        return entries;    	
    	}
    	catch (Exception e) 
    	{
    		throw new Exception(getParameterizedLabel("labels.internal.event.error.couldNotConnectToRSS", externalRSSUrl));
    	}
    }

    private void sortEntries(List entries)
    {
    	Collections.sort(entries, Collections.reverseOrder(new OrderByDate()));
    	//Collections.sort(entries, Collections.reverseOrder(new OrderByEventDate(this.getLanguageCode())));
    }

    public Map getDaysEventHash(String eventsString)
    {
    	Map daysEvents = new HashMap();
    	
    	try
    	{
	        Set events = (Set)findOnValueStack(eventsString);
	
	    	Iterator eventsIterator = events.iterator();
	    	while(eventsIterator.hasNext())
	    	{
	    		Event event = (Event)eventsIterator.next();
	    		
	    		java.util.Calendar startDateCalendar = event.getStartDateTime();
	    		java.util.Calendar endDateCalendar = event.getEndDateTime();
	    		while(startDateCalendar.get(java.util.Calendar.DAY_OF_MONTH) <= endDateCalendar.get(java.util.Calendar.DAY_OF_MONTH))
	    		{
	    			int dayOfMonth = startDateCalendar.get(java.util.Calendar.DAY_OF_MONTH);
	    			List<Event> dayEvents = (List<Event>)daysEvents.get("day_" + dayOfMonth);
	    			if(dayEvents == null)
	    			{
	    				dayEvents = new ArrayList<Event>();
	    				daysEvents.put("day_" + dayOfMonth, dayEvents);
	    			}
	    			
	    			dayEvents.add(event);
	    			startDateCalendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
	    		}
	    	}
    	}
    	catch (Exception e) 
    	{
    		e.printStackTrace();
		}
    	
    	return daysEvents;
    }
    
    public List getDates(String entryString)
    {
    	List dates = new ArrayList();
    	
    	try
		{
			
	        Object object = findOnValueStack(entryString);
	        SyndEntry entry = (SyndEntry)object;
	        if(object != null)
	        	dates = CalendarHelper.getDates(entry, this.getLanguageCode());
	        else
	        	log.info("entryString:" + entryString);
		} 
    	catch (Exception e)
		{
    		e.printStackTrace();
		}
        
        if(dates.size() < 2)
        {
	        dates.add(new Date());
	    	dates.add(new Date());
        }	        
        return dates;
    }

    @Override
	public String getResourceUrl(Event event, String assetKey) throws Exception
	{
		if (event == null)
		{
			log.debug("Event was null. Cannot return resource url.");
			return "";
		}
		return ResourceController.getController().getResourceUrl(event.getId(), assetKey, getSession());
	}

    public void setCategoryAttribute(String categoryAttribute)
    {
        this.categoryAttribute = categoryAttribute;
    }
    
    public void setCategoryNames(String categoryNames)
    {
        this.categoryNames = categoryNames;
    }
    
    public void setIncludedLanguages(String includedLanguages)
    {
    	this.includedLanguages = includedLanguages;
    }
    
    public String getMessage()
    {
    	return this.message;
    }
     
    public String getFilterDescription()
    {
    	return this.filterDescription;
    }


    /*
    public List getCategoriesList()
    {
        return categoriesList;
    }
    */
    
    public void setError(String message, Exception e)
    {
        String context = ActionContext.getContext().getName();
        ActionContext.getContext().getValueStack().getContext().put("message", message);
        ActionContext.getContext().getValueStack().getContext().put("error", e);
    }

}

//--------------------------------------------------
//Inner class for sorting entries by date.
//--------------------------------------------------

final class OrderByDate implements Comparator
{
	public int compare(final Object aObj, final Object bObj)
	{
		final Date aDate = ((SyndEntry)aObj).getPublishedDate();
		final Date bDate = ((SyndEntry)bObj).getPublishedDate();
		return (aDate == null) ? 1 : (bDate == null) ? -1 : bDate.compareTo(aDate);
	}
}

