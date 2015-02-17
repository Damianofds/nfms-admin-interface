<%@page import="it.geosolutions.unredd.services.data.ModelDomainNames"%>
<%@page import="it.geosolutions.unredd.services.data.utils.ResourceDecorator"%>
<%@page import="it.geosolutions.unredd.services.data.ResourcePOJO"%>
<%@page import="it.geosolutions.unredd.geostore.model.UNREDDChartScript"%>
<%@page import="java.util.ArrayList"%>
<%@page import="it.geosolutions.unredd.geostore.model.UNREDDStatsDef"%>
<%@page import="java.util.List"%><%@
page contentType="text/html" pageEncoding="UTF-8"%><!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link href="admin-style.css" rel="stylesheet" type="text/css" />
        <title>ChartScript</title>
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <h1>ChartScript</h1>
        
        <% ResourcePOJO res = (ResourcePOJO)request.getAttribute("resource"); %>
        
        <table class="edit">
            <tr>
                <th>
                    Name
                </th>
                <td>
                    <%= res.getName() %>
                </td>
            </tr>
            <tr>
                <th>
                    StatsDef
                </th>
                <td>
                    <%
                    List<String> relatedStatDefs;
                    if (res != null) {
                        ResourceDecorator chartScript = new ResourceDecorator(res);
                        relatedStatDefs = chartScript.getAttributeValues(ModelDomainNames.CHARTSCRIPT_STATDEF);
                    } else {
                        relatedStatDefs = new ArrayList();
                    }
                    for (String statsDef : relatedStatDefs) { %>
                        <a href="StatsDefShow?name=<%= statsDef %>"><%= statsDef %></a><br>
                    <% } %>
                </td>
            </tr>
            <tr>
                <th>
                    Script path
                </th>
                <td>
                    <%= res == null ? "" : new ResourceDecorator(res).getFirstAttributeValue(ModelDomainNames.CHARTSCRIPT_SCRIPTPATH)  %>
                </td>
            </tr>
        </table>
        <div id="tools">
            <a href="ChartScriptEditForm?id=<%= res.getId() %>">[edit]</a>
        </div>
    </body>
</html>
