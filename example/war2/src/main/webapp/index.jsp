<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title><%=request.getServletContext().getContextPath()%></title>
</head>
<body>
<h1><%=request.getServletContext().getContextPath()%></h1>
<table>
    <thead>
    <tr>
        <th>Key</th>
        <th>Value</th>
    </tr>
    </thead>
    <tr>
        <td>Remote user</td>
        <td><%= request.getRemoteUser() %></td>
    </tr>
    <tr>
        <td>User principal</td>
        <td><%= request.getUserPrincipal() %></td>
    </tr>
    <tr>
        <td>sd</td>
        <td>${something.foo}</td>
    </tr>
</table>
</body>
</html>
