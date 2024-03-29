﻿<!DOCTYPE HTML>
<html>
<head>
    <title>Listado de los jugadores de tus equipos del SuperManager</title>
    <#include "./include/header.vm">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<style media="screen" type="text/css">

body {
	background: #eee !important;
}

.wrapper {
	margin-top: 80px;
  margin-bottom: 80px;
}


.h2{

  color: #000 ! important;

}
.form-signin {
  max-width: 380px;
  padding: 15px 35px 45px;
  margin: 0 auto;
  background-color: #fff;
  border: 1px solid rgba(0,0,0,0.1);

  .form-signin-heading,
	.checkbox {
	  margin-bottom: 30px;
	}

	.checkbox {
	  font-weight: normal;
	}

	.form-control {
	  position: relative;
	  font-size: 16px;
	  height: auto;
	  padding: 10px;
		@include box-sizing(border-box);

		&:focus {
		  z-index: 2;
		}
	}

	input[type="text"] {
	  margin-bottom: -1px;
	  border-bottom-left-radius: 0;
	  border-bottom-right-radius: 0;
	}

	input[type="password"] {
	  margin-bottom: 20px;
	  border-top-left-radius: 0;
	  border-top-right-radius: 0;
	}
}
</style>
<body>


</form>
    <div class="wrapper">
      <form action="dologin.html" method="post" class="form-signin">
        <h2 class="form-signin-heading" style="color:#000" >Usuario y contraseña del supermanager</h2>
        <input type="text" class="form-control" name="login" placeholder="Usuario de supermanager" id="login" required="" autofocus="" />
        <input type="password" class="form-control" name="password" placeholder="Password" id="p" required=""/>
        <button class="btn btn-lg btn-primary btn-block" type="submit">Enviar</button>
      </form>
    </div>
</body>
</html>