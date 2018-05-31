<html xmlns="http://www.w3.org/1999/html">
<head>
    <style>
        .oneRow {
            background-color: #eee;
        }

        .twoRow {
            background-color: #fff;
        }

        .threeRow {
            background-color: #ddd;
        }

        .fourRow {
            background-color: #fff;
        }

    </style>
</head>
<body>
<h2>Hello ${login}</h2>
<h3>Please select role</h3>
<#list roles>
    <table border=1>
        <tr>
            <th>
            <th>ID
            <th>Name
            <th>Description
        </tr>
        <#items as role>
            <tr class="${role?item_cycle('one','two','three','four')}Row">
                <td> <form action="/login" method="post">
                    <button  name="roleId" value="${role.id}">Select</button>
                </form>
                </td>
                <td>${role.id}
                <td>${role.name}
                <td>${role.description}
            </tr>
        </#items>
    </table>
<#else>
  <p>You have no roles
</#list>
<#include "footer.html">
</body>
</html>