<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="java.util.*" %>
<%@ page import="java.lang.management.*" %>

<%
    /*
     * AWS Elastic Beanstalk checks your application's health by periodically
     * sending an HTTP HEAD request to a resource in your application. By
     * default, this is the root or default resource in your application,
     * but can be configured for each environment.
     *
     * Here, we report success as long as the app server is up, but skip
     * generating the whole page since this is a HEAD request only. You
     * can employ more sophisticated health checks in your application.
     */
    if (request.getMethod().equals("HEAD")) return;
%>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <title>Hello AWS Web World!</title>
    <link rel="stylesheet" href="styles/styles.css" type="text/css" media="screen">
</head>
<body>
    <div id="content">
        <div class="s3">
            <h2>Cruxly API server health check</h2>
			<table style='border: 1px solid black;'>
				<tr><td colspan="2" align="center"><strong>Server details</strong></td></tr>
				<tr><td>Server time</td><td><%= new java.util.Date() %></td></tr>
				<tr><td>Server Java version</td><td><%= System.getProperty("java.version") %></td></tr>
				<tr><td>Server OS</td><td><%= System.getProperty("os.name") %></td></tr>
				<% Runtime runtime = Runtime.getRuntime(); %>
				<% int mb = 1024*1024; %>
				<tr><td>Used memory</td><td><%= (runtime.totalMemory() - runtime.freeMemory()) / mb %> mb</td></tr>
				<tr><td>Free memory</td><td><%= runtime.freeMemory() / mb %> mb</td></tr>
				<tr><td>Total memory</td><td><%= runtime.totalMemory() / mb %> mb</td></tr>
				<tr><td>Maximum available memory</td><td><%= runtime.maxMemory() / mb %> mb</td></tr>
				<tr><td>Available processors</td><td><%= runtime.availableProcessors() %></td></tr>
				
				
				<tr><td colspan="2" align="center"><strong>Memory MXBean</strong></td></tr>
				<tr><td>Server OS</td><td><%= System.getProperty("os.name") %></td></tr>
				<tr><td>Heap Memory Usage</td><td><%= ManagementFactory.getMemoryMXBean().getHeapMemoryUsage() %></td></tr>
				<tr><td>Non-Heap Memory Usage</td><td><%= ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage() %></td></tr>
				
				<tr><td colspan="2" align="center"><strong>Memory Pool MXBeans</strong></td></tr>
				<%
					Iterator iter = ManagementFactory.getMemoryPoolMXBeans().iterator();
					while (iter.hasNext()) {
					MemoryPoolMXBean item = (MemoryPoolMXBean) iter.next();
				%>

				<tr>
				<td colspan="2" align="center"><strong><%= item.getName() %></strong></td>
				</tr>
				 
				<tr>
				<td width="200">Type</td>
				<td><%= item.getType() %></td>
				</tr>
				 
				<tr>
				<td>Usage</td>
				<td><%= item.getUsage() %></td>
				</tr>
				 
				<tr>
				<td>Peak Usage</td>
				<td><%= item.getPeakUsage() %></td>
				</tr>
				 
				<tr>
				<td>Collection Usage</td>
				<td><%= item.getCollectionUsage() %></td>
				</tr>
				
				<%
				}
				%>
				
				<tr><td colspan="2" align="center"><strong>HTTP Request Headers</strong></td></tr>

				<%
					Enumeration enumeration = request.getHeaderNames();
					while (enumeration.hasMoreElements()) {
						String name = (String) enumeration.nextElement();
						String value = request.getHeader(name);
				%>
				<tr>
					<td><%=name%></td>
					<td><%=value%></td>
				</tr>
				<%
					}
				%>
			</table>
		</div>
    </div>
</body>
</html>