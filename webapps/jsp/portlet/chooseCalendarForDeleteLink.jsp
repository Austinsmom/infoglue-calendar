<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<c:set var="activeNavItem" value="Events" scope="page"/>
<c:set var="activeEventSubNavItem" value="NewEvent" scope="page"/>

<%@ include file="adminHeader.jsp" %>
<%@ include file="functionMenu.jsp" %>

<%@ include file="eventSubFunctionMenu.jsp" %>

<portlet:renderURL var="viewListUrl">
	<portlet:param name="action" value="ViewLinkedPublishedEventList"/>
</portlet:renderURL>

<portlet:renderURL var="confirmUrl">
	<portlet:param name="action" value="Confirm"/>
</portlet:renderURL>

<script type="text/javascript">
	function submitDelete(okUrl, confirmMessage)
	{
		//alert("okUrl:" + okUrl);
		document.confirmForm.okUrl.value = okUrl;
		document.confirmForm.confirmMessage.value = confirmMessage;
		document.confirmForm.submit();
	}
</script>
<form name="confirmForm" action="<c:out value="${confirmUrl}"/>" method="post">
	<input type="hidden" name="confirmTitle" value="<ww:property value="this.htmlEncodeValue(this.getLabel('labels.internal.general.list.deleteLink.confirm.header'))" />"/>
	<input type="hidden" name="confirmMessage" value="Fixa detta"/>
	<input type="hidden" name="okUrl" value=""/>
	<input type="hidden" name="cancelUrl" value="<c:out value="${viewListUrl}"/>"/>	
</form>

<div class="mainCol">
    <div class="portlet_margin">
        <p class="instruction"><ww:property value="this.getLabel('labels.internal.application.chooseCalendarForLinkDeleteIntro')"/></p>
    </div>
    
    <div class="columnlabelarea row clearfix">
        <div class="columnLong"><p><ww:property value="this.getLabel('labels.internal.calendar.name')"/></p></div>
        <div class="columnMedium"><p><ww:property value="this.getLabel('labels.internal.calendar.description')"/></p></div>
    </div>
    
    <ww:set name="eventId" value="eventId" scope="page"/>
    
    <ww:iterator value="calendars" status="rowstatus">
        
        <ww:set name="calendarId" value="id" scope="page"/>
    
        <portlet:actionURL var="deleteUrl">
            <portlet:param name="action" value="DeleteEvent!linkedPublished"/>
            <calendar:evalParam name="eventId" value="${eventId}"/>
            <calendar:evalParam name="calendarId" value="${calendarId}"/>
        </portlet:actionURL>
        
		<div class="row clearfix">
            <ww:set name="deleteConfirm" value="this.getVisualFormatter().escapeExtendedHTML(this.getParameterizedLabel('labels.internal.general.list.deleteLink.confirm', name))" />
			<a href="javascript:submitDelete('<c:out value="${deleteUrl}"/>', '<ww:property value="#deleteConfirm"/>');" title="<ww:property value="this.getVisualFormatter().escapeExtendedHTML(this.getParameterizedLabel('labels.internal.general.list.select.title', name))"/>">
				<div class="columnLong">
					<p class="portletHeadline">
					<ww:property value="name"/>
					</p>
				</div>
				<div class="columnMedium">
					<p><ww:property value="description"/></p>
				</div>
			</a>
            <div class="columnEnd">
            </div>
        </div>
            
    </ww:iterator>
    
    <ww:if test="calendars == null || calendars.size() == 0">
        <div class="row clearfix">
            <div class="columnLong"><p class="portletHeadline"><ww:property value="this.getLabel('labels.internal.applicationNoItemsFound')"/></a></p></div>
            <div class="columnMedium"></div>
            <div class="columnEnd"></div>
        </div>
    </ww:if>
</div>

<%@ include file="adminFooter.jsp" %>
