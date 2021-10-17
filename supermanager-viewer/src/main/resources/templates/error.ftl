<!DOCTYPE HTML>
<html>
<head>
    <title>SuperManager</title>
    <#include "./include/header.vm">
</head>
<body>
<h4>Error</h1>
<p>${message}</p>
<p>${errorMessage!""}</p>
<p>${stacktrace!""}</p>
</body>
</html>