//服务层
app.service('contentService',function($http){
	    	
	//根据分类id查询
	this.findByCategoryId=function(categoryId){
		return $http.post('content/findByCategoryId.do?categoryId='+categoryId);
	}
	
	
});
