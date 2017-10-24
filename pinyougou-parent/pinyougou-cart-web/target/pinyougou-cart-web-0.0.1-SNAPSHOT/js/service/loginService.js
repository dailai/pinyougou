//服务层
app.service('loginService',function($http){
	    	
	//得到用户名 
	this.getName=function(){
		return  $http.get('../login/name.do');
	}
	
});
