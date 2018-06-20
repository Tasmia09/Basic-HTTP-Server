#!/bin/bash
echo "Content-type: text/html"
cat <<EOT
<html>
<head>
        <title>Welcome</title>
</head>
<body>
        <p>Hello!</p>
        
<h2>
	<p>Current date and time is:</p>
	<p id="date"></p>
	<script>document.getElementById("date").innerHTML = Date();</script>
</h2>
</body>
</html>
EOT
