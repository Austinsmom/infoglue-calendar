<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<c:set var="activeNavItem" value="Events" scope="page"/>
<c:set var="activeEventSubNavItem" value="NewEvent" scope="page"/>

<%@ include file="adminHeader.jsp" %>
<%@ include file="functionMenu.jsp" %>

<%@ include file="eventSubFunctionMenu.jsp" %>

<div class="mainCol">

    <div class="portlet_margin">
        <p class="instruction"><ww:property value="this.getLabel('labels.internal.application.chooseLanguageForEditIntro')"/></p>
    </div> 
    
    <div class="columnlabelarea row clearfix">
        <div class="columnLong"><p><ww:property value="this.getLabel('labels.internal.language.name')"/></p></div>
        <div class="columnMedium"><p><ww:property value="this.getLabel('labels.internal.language.isoCode')"/></p></div>
    </div>
    
    <ww:set name="skipLanguageTabs" value="skipLanguageTabs" scope="page"/>
    
    <ww:iterator value="availableLanguages" status="rowstatus">
        
        <ww:set name="systemLanguageId" value="id" scope="page"/>
        <ww:set name="eventId" value="eventId" scope="page"/>
        <portlet:renderURL var="editEventUrl">
            <portlet:param name="action" value="ViewEvent!edit"/>
            <portlet:param name="versionLanguageId" value='<%= pageContext.getAttribute("systemLanguageId").toString() %>'/>
            <portlet:param name="eventId" value='<%= pageContext.getAttribute("eventId").toString() %>'/>
            <calendar:evalParam name="skipLanguageTabs" value="${skipLanguageTabs}"/>
        </portlet:renderURL>
        
        <div class="row clearfix">

			<a href="<c:out value="${editEventUrl}"/>" title="<ww:property value="this.getVisualFormatter().escapeExtendedHTML(this.getParameterizedLabel('labels.internal.general.list.select.title', name))"/>">
				<div class="columnLong">
					<p class="portletHeadline">
						<ww:property value="name"/>
					</p>
				</div>
				<div class="columnMedium">
					<p><ww:property value="isoCode"/></p>
				</div>
			</a>
            <div class="columnEnd">
            </div>
        </div>
            
    </ww:iterator>
    
    <ww:if test="availableLanguages == null || availableLanguages.size() == 0">
        <div class="row clearfix">
            <div class="columnLong"><p class="portletHeadline"><ww:property value="this.getLabel('labels.internal.applicationNoItemsFound')"/></a></p></div>
            <div class="columnMedium"></div>
            <div class="columnEnd"></div>
        </div>
    </ww:if>
    
</div>

<%@ include file="adminFooter.jsp" %>
